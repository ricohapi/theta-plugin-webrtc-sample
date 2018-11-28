/**
 * Copyright 2018 Ricoh Company, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.theta360.pluginapplication.webrtc.sample.network.WebRTCLibExtends;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraEnumerationAndroid.CaptureFormat;
import org.webrtc.Logging;
import org.webrtc.NV21Buffer;
import org.webrtc.Size;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.TextureBufferImpl;
import org.webrtc.VideoFrame;

@SuppressWarnings("deprecation")
class ThetaSession {
    private static final String TAG = "ThetaSession";

    enum FailureType { ERROR, DISCONNECTED }

    // Callbacks are fired on the camera thread.
    interface CreateSessionCallback {
        void onDone(ThetaSession session);
        void onFailure(FailureType failureType, String error);
    }

    // Events are fired on the camera thread.
    interface Events {
        void onCameraOpening();
        void onCameraError(ThetaSession session, String error);
        void onCameraDisconnected(ThetaSession session);
        void onCameraClosed(ThetaSession session);
        void onFrameCaptured(ThetaSession session, VideoFrame frame);
    }

    private static final int NUMBER_OF_CAPTURE_BUFFERS = 3;

    private static enum SessionState { RUNNING, STOPPED }

    private final Handler cameraThreadHandler;
    private final Events events;
    private final boolean captureToTexture;
    private final Context applicationContext;
    private final SurfaceTextureHelper surfaceTextureHelper;
    private final int cameraId;
    private final android.hardware.Camera camera;
    private final android.hardware.Camera.CameraInfo info;
    private final CaptureFormat captureFormat;
    // Used only for stats. Only used on the camera thread.
    private final long constructionTimeNs; // Construction time of this class.

    private SessionState state;

    // TODO(titovartem) make correct fix during webrtc:9175
    @SuppressWarnings("ByteBufferBackingArray")
    public static void create(final CreateSessionCallback callback, final Events events,
                              final boolean captureToTexture, final Context applicationContext,
                              final SurfaceTextureHelper surfaceTextureHelper, final int cameraId, final int width,
                              final int height, final int framerate) {
        final long constructionTimeNs = System.nanoTime();
        Logging.d(TAG, "Open camera " + cameraId);
        events.onCameraOpening();

        final android.hardware.Camera camera;
        try {
            camera = android.hardware.Camera.open(cameraId);
        } catch (RuntimeException e) {
            callback.onFailure(FailureType.ERROR, e.getMessage());
            return;
        }

        if (camera == null) {
            callback.onFailure(FailureType.ERROR,
                    "android.hardware.Camera.open returned null for camera id = " + cameraId);
            return;
        }

        try {
            camera.setPreviewTexture(surfaceTextureHelper.getSurfaceTexture());
        } catch (IOException | RuntimeException e) {
            camera.release();
            callback.onFailure(FailureType.ERROR, e.getMessage());
            return;
        }

        final android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);

        final CaptureFormat captureFormat;
        try {
            final android.hardware.Camera.Parameters parameters = camera.getParameters();
            captureFormat = findClosestCaptureFormat(parameters, width, height, framerate);
            final Size pictureSize = findClosestPictureSize(parameters, width, height);
            updateCameraParameters(camera, parameters, captureFormat, pictureSize, captureToTexture);
        } catch (RuntimeException e) {
            camera.release();
            callback.onFailure(FailureType.ERROR, e.getMessage());
            return;
        }

        if (!captureToTexture) {
            final int frameSize = captureFormat.frameSize();
            for (int i = 0; i < NUMBER_OF_CAPTURE_BUFFERS; ++i) {
                final ByteBuffer buffer = ByteBuffer.allocateDirect(frameSize);
                camera.addCallbackBuffer(buffer.array());
            }
        }

        // Calculate orientation manually and send it as CVO insted.
        camera.setDisplayOrientation(0 /* degrees */);

        callback.onDone(new ThetaSession(events, captureToTexture, applicationContext,
                surfaceTextureHelper, cameraId, camera, info, captureFormat, constructionTimeNs));
    }

    private static void updateCameraParameters(android.hardware.Camera camera,
                                               android.hardware.Camera.Parameters parameters, CaptureFormat captureFormat, Size pictureSize,
                                               boolean captureToTexture) {
        final List<String> focusModes = parameters.getSupportedFocusModes();

        // <THETA> Preview mode and size setting
        String paramValue = "RicMoviePreview" + captureFormat.width;
        parameters.set("RIC_SHOOTING_MODE", paramValue);

//        parameters.setPreviewFpsRange(captureFormat.framerate.min, captureFormat.framerate.max);
        parameters.setPreviewFpsRange(captureFormat.framerate.max, captureFormat.framerate.max);    // <THETA> Force max framerate
        parameters.setPreviewSize(captureFormat.width, captureFormat.height);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        if (!captureToTexture) {
            parameters.setPreviewFormat(captureFormat.imageFormat);
        }

        if (parameters.isVideoStabilizationSupported()) {
            parameters.setVideoStabilization(true);
        }
        if (focusModes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        camera.setParameters(parameters);
    }

    private static CaptureFormat findClosestCaptureFormat(
            android.hardware.Camera.Parameters parameters, int width, int height, int framerate) {
        // Find closest supported format for |width| x |height| @ |framerate|.
        final List<CaptureFormat.FramerateRange> supportedFramerates =
                ThetaEnumerator.convertFramerates(parameters.getSupportedPreviewFpsRange());
        Logging.d(TAG, "Available fps ranges: " + supportedFramerates);

        final CaptureFormat.FramerateRange fpsRange =
                CameraEnumerationAndroid.getClosestSupportedFramerateRange(supportedFramerates, framerate);

        final Size previewSize = CameraEnumerationAndroid.getClosestSupportedSize(
                ThetaEnumerator.convertSizes(parameters.getSupportedPreviewSizes()), width, height);

        return new CaptureFormat(previewSize.width, previewSize.height, fpsRange);
    }

    private static Size findClosestPictureSize(
            android.hardware.Camera.Parameters parameters, int width, int height) {
        return CameraEnumerationAndroid.getClosestSupportedSize(
                ThetaEnumerator.convertSizes(parameters.getSupportedPictureSizes()), width, height);
    }

    private ThetaSession(Events events, boolean captureToTexture, Context applicationContext,
                         SurfaceTextureHelper surfaceTextureHelper, int cameraId, android.hardware.Camera camera,
                         android.hardware.Camera.CameraInfo info, CaptureFormat captureFormat,
                         long constructionTimeNs) {
        Logging.d(TAG, "Create new camera1 session on camera " + cameraId);

        this.cameraThreadHandler = new Handler();
        this.events = events;
        this.captureToTexture = captureToTexture;
        this.applicationContext = applicationContext;
        this.surfaceTextureHelper = surfaceTextureHelper;
        this.cameraId = cameraId;
        this.camera = camera;
        this.info = info;
        this.captureFormat = captureFormat;
        this.constructionTimeNs = constructionTimeNs;

        surfaceTextureHelper.setTextureSize(captureFormat.width, captureFormat.height);

        startCapturing();
    }

    protected void stop() {
        Logging.d(TAG, "Stop camera1 session on camera " + cameraId);
        checkIsOnCameraThread();
        if (state != SessionState.STOPPED) {
            final long stopStartTime = System.nanoTime();
            stopInternal();
        }
    }

    @Nullable
    public android.hardware.Camera getCamera() {
        return this.camera;
    }

    private void startCapturing() {
        Logging.d(TAG, "Start capturing");
        checkIsOnCameraThread();

        state = SessionState.RUNNING;

        camera.setErrorCallback(new android.hardware.Camera.ErrorCallback() {
            @Override
            public void onError(int error, android.hardware.Camera camera) {
                String errorMessage;
                if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED) {
                    errorMessage = "Camera server died!";
                } else {
                    errorMessage = "Camera error: " + error;
                }
                Logging.e(TAG, errorMessage);
                stopInternal();
                if (error == android.hardware.Camera.CAMERA_ERROR_EVICTED) {
                    events.onCameraDisconnected(ThetaSession.this);
                } else {
                    events.onCameraError(ThetaSession.this, errorMessage);
                }
            }
        });

        if (captureToTexture) {
            listenForTextureFrames();
        } else {
            listenForBytebufferFrames();
        }
        try {
            camera.startPreview();
        } catch (RuntimeException e) {
            stopInternal();
            events.onCameraError(this, e.getMessage());
        }
    }

    private void stopInternal() {
        Logging.d(TAG, "Stop internal");
        checkIsOnCameraThread();
        if (state == SessionState.STOPPED) {
            Logging.d(TAG, "Camera is already stopped");
            return;
        }

        state = SessionState.STOPPED;
        surfaceTextureHelper.stopListening();
        // Note: stopPreview or other driver code might deadlock. Deadlock in
        // Camera._stopPreview(Native Method) has been observed on
        // Nexus 5 (hammerhead), OS version LMY48I.
        camera.stopPreview();
        camera.release();
        events.onCameraClosed(this);
        Logging.d(TAG, "Stop done");
    }

    private void listenForTextureFrames() {
        surfaceTextureHelper.startListening((VideoFrame frame) -> {
            checkIsOnCameraThread();

            if (state != SessionState.RUNNING) {
                Logging.d(TAG, "Texture frame captured but camera is no longer running.");
                return;
            }

            // Undo the mirror that the OS "helps" us with.
            // http://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
            final VideoFrame modifiedFrame = new VideoFrame(
                    ThetaSession.createTextureBufferWithModifiedTransformMatrix(
                            (TextureBufferImpl)frame.getBuffer(),
                            /* mirror= */ info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT,
                            /* rotation= */ 0),
                    /* rotation= */ getFrameOrientation(),
                    frame.getTimestampNs());
            events.onFrameCaptured(ThetaSession.this, modifiedFrame);
            modifiedFrame.release();
        });
    }

    private void listenForBytebufferFrames() {
        camera.setPreviewCallbackWithBuffer(new android.hardware.Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] data, android.hardware.Camera callbackCamera) {
                checkIsOnCameraThread();

                if (callbackCamera != camera) {
                    Logging.e(TAG, "Callback from a different camera. This should never happen.");
                    return;
                }

                if (state != SessionState.RUNNING) {
                    Logging.d(TAG, "Bytebuffer frame captured but camera is no longer running.");
                    return;
                }

                final long captureTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());

                VideoFrame.Buffer frameBuffer = new NV21Buffer(
                        data, captureFormat.width, captureFormat.height, () -> cameraThreadHandler.post(() -> {
                            if (state == SessionState.RUNNING) {
                                camera.addCallbackBuffer(data);
                            }
                        }));
                final VideoFrame frame = new VideoFrame(frameBuffer, getFrameOrientation(), captureTimeNs);
                events.onFrameCaptured(ThetaSession.this, frame);
                frame.release();
            }
        });
    }

    private int getFrameOrientation() {
        return 0;   // <THETA> Ignore device orientation
/* <THETA> Ignore device orientation
        int rotation = ThetaSession.getDeviceOrientation(applicationContext);
        if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
            rotation = 360 - rotation;
        }
        return (info.orientation + rotation) % 360;
*/
    }

    private void checkIsOnCameraThread() {
        if (Thread.currentThread() != cameraThreadHandler.getLooper().getThread()) {
            throw new IllegalStateException("Wrong thread");
        }
    }

/* <THETA> Ignore device orientation
    private static int getDeviceOrientation(Context context) {
        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        switch (wm.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_0:
            default:
                return 0;
        }
    }
*/

    private static VideoFrame.TextureBuffer createTextureBufferWithModifiedTransformMatrix(
            TextureBufferImpl buffer, boolean mirror, int rotation) {
        final Matrix transformMatrix = new Matrix();
        // Perform mirror and rotation around (0.5, 0.5) since that is the center of the texture.
        transformMatrix.preTranslate(/* dx= */ 0.5f, /* dy= */ 0.5f);
        if (mirror) {
            transformMatrix.preScale(/* sx= */ -1f, /* sy= */ 1f);
        }
        transformMatrix.preRotate(rotation);
        transformMatrix.preTranslate(/* dx= */ -0.5f, /* dy= */ -0.5f);

        // The width and height are not affected by rotation since Camera2Session has set them to the
        // value they should be after undoing the rotation.
        return buffer.applyTransformMatrix(transformMatrix, buffer.getWidth(), buffer.getHeight());
    }
}