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
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.Logging;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoFrame;

import java.util.Arrays;

@SuppressWarnings("deprecation")
public class ThetaCapturer implements CameraVideoCapturer {
    enum SwitchState {
        IDLE, // No switch requested.
        PENDING, // Waiting for previous capture session to open.
        IN_PROGRESS, // Waiting for new switched capture session to start.
    }

    private static final String TAG = "ThetaCapturer";
    private final static int MAX_OPEN_CAMERA_ATTEMPTS = 3;
    private final static int OPEN_CAMERA_DELAY_MS = 500;
    private final static int OPEN_CAMERA_TIMEOUT = 10000;
    private final CameraEnumerator cameraEnumerator;
    @Nullable
    private final CameraEventsHandler eventsHandler;
    private final Handler uiThreadHandler;

    private Camera thetaCamera = null;

    @Nullable
    private final ThetaSession.CreateSessionCallback createSessionCallback = new ThetaSession.CreateSessionCallback() {
        @Override
        public void onDone(ThetaSession session) {
            checkIsOnCameraThread();
            Logging.d(TAG, "Create session done. Switch state: " + switchState);
            uiThreadHandler.removeCallbacks(openCameraTimeoutRunnable);
            synchronized (stateLock) {
                capturerObserver.onCapturerStarted(true /* success */);
                sessionOpening = false;
                currentSession = session;
                cameraStatistics = new CameraStatistics(surfaceHelper, eventsHandler);
                firstFrameObserved = false;
                stateLock.notifyAll();

                if (switchState == SwitchState.IN_PROGRESS) {
                    switchState = SwitchState.IDLE;
                    if (switchEventsHandler != null) {
                        switchEventsHandler.onCameraSwitchDone(cameraEnumerator.isFrontFacing(cameraName));
                        switchEventsHandler = null;
                    }
                } else if (switchState == SwitchState.PENDING) {
                    switchState = SwitchState.IDLE;
                    switchCameraInternal(switchEventsHandler);
                }

                // <THETA> Holding camera object
                thetaCamera = session.getCamera();
            }
        }

        @Override
        public void onFailure(ThetaSession.FailureType failureType, String error) {
            checkIsOnCameraThread();
            uiThreadHandler.removeCallbacks(openCameraTimeoutRunnable);
            synchronized (stateLock) {
                capturerObserver.onCapturerStarted(false /* success */);
                openAttemptsRemaining--;

                if (openAttemptsRemaining <= 0) {
                    Logging.w(TAG, "Opening camera failed, passing: " + error);
                    sessionOpening = false;
                    stateLock.notifyAll();
                    if (switchState != SwitchState.IDLE) {
                        if (switchEventsHandler != null) {
                            switchEventsHandler.onCameraSwitchError(error);
                            switchEventsHandler = null;
                        }
                        switchState = SwitchState.IDLE;
                    }

                    if (failureType == ThetaSession.FailureType.DISCONNECTED) {
                        eventsHandler.onCameraDisconnected();
                    } else {
                        eventsHandler.onCameraError(error);
                    }
                } else {
                    Logging.w(TAG, "Opening camera failed, retry: " + error);
                    createSessionInternal(OPEN_CAMERA_DELAY_MS);
                }
            }
        }
    };

    @Nullable
    private final ThetaSession.Events cameraSessionEventsHandler = new ThetaSession.Events() {
        @Override
        public void onCameraOpening() {
            checkIsOnCameraThread();
            synchronized (stateLock) {
                if (currentSession != null) {
                    Logging.w(TAG, "onCameraOpening while session was open.");
                    return;
                }
                eventsHandler.onCameraOpening(cameraName);
            }
        }

        @Override
        public void onCameraError(ThetaSession session, String error) {
            checkIsOnCameraThread();
            synchronized (stateLock) {
                if (session != currentSession) {
                    Logging.w(TAG, "onCameraError from another session: " + error);
                    return;
                }
                eventsHandler.onCameraError(error);
                stopCapture();
            }
        }

        @Override
        public void onCameraDisconnected(ThetaSession session) {
            checkIsOnCameraThread();
            synchronized (stateLock) {
                if (session != currentSession) {
                    Logging.w(TAG, "onCameraDisconnected from another session.");
                    return;
                }
                eventsHandler.onCameraDisconnected();
                stopCapture();
            }
        }

        @Override
        public void onCameraClosed(ThetaSession session) {
            checkIsOnCameraThread();
            synchronized (stateLock) {
                if (session != currentSession && currentSession != null) {
                    Logging.d(TAG, "onCameraClosed from another session.");
                    return;
                }
                eventsHandler.onCameraClosed();
            }
        }

        @Override
        public void onFrameCaptured(ThetaSession session, VideoFrame frame) {
            checkIsOnCameraThread();
            synchronized (stateLock) {
                if (session != currentSession) {
                    Logging.w(TAG, "onFrameCaptured from another session.");
                    return;
                }
                if (!firstFrameObserved) {
                    eventsHandler.onFirstFrameAvailable();
                    firstFrameObserved = true;
                }
                cameraStatistics.addFrame();
                capturerObserver.onFrameCaptured(frame);
            }
        }
    };

    private final Runnable openCameraTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            eventsHandler.onCameraError("Camera failed to start within timeout.");
        }
    };

    // Initialized on initialize
    // -------------------------
    @Nullable private Handler cameraThreadHandler;
    private Context applicationContext;
    private org.webrtc.CapturerObserver capturerObserver;
    @Nullable private SurfaceTextureHelper surfaceHelper;

    private final Object stateLock = new Object();
    private boolean sessionOpening; /* guarded by stateLock */
    @Nullable private ThetaSession currentSession; /* guarded by stateLock */
    private String cameraName; /* guarded by stateLock */
    private int width; /* guarded by stateLock */
    private int height; /* guarded by stateLock */
    private int framerate; /* guarded by stateLock */
    private int openAttemptsRemaining; /* guarded by stateLock */
    private SwitchState switchState = SwitchState.IDLE; /* guarded by stateLock */
    @Nullable private CameraSwitchHandler switchEventsHandler; /* guarded by stateLock */
    // Valid from onDone call until stopCapture, otherwise null.
    @Nullable private CameraStatistics cameraStatistics; /* guarded by stateLock */
    private boolean firstFrameObserved; /* guarded by stateLock */
    private final boolean captureToTexture;

    public ThetaCapturer(String cameraName, CameraEventsHandler eventsHandler, boolean captureToTexture) {
        if (eventsHandler == null) {
            eventsHandler = new CameraEventsHandler() {
                @Override
                public void onCameraError(String errorDescription) {}
                @Override
                public void onCameraDisconnected() {}
                @Override
                public void onCameraFreezed(String errorDescription) {}
                @Override
                public void onCameraOpening(String cameraName) {}
                @Override
                public void onFirstFrameAvailable() {}
                @Override
                public void onCameraClosed() {}
            };
        }

        this.eventsHandler = eventsHandler;
        this.cameraEnumerator = new ThetaEnumerator(captureToTexture);
        this.cameraName = cameraName;
        uiThreadHandler = new Handler(Looper.getMainLooper());

        final String[] deviceNames = cameraEnumerator.getDeviceNames();

        if (deviceNames.length == 0) {
            throw new RuntimeException("No cameras attached.");
        }
        if (!Arrays.asList(deviceNames).contains(this.cameraName)) {
            throw new IllegalArgumentException(
                    "Camera name " + this.cameraName + " does not match any known camera device.");
        }

        this.captureToTexture = captureToTexture;
    }

    @Override
    public void initialize(@Nullable SurfaceTextureHelper surfaceTextureHelper, Context applicationContext, org.webrtc.CapturerObserver capturerObserver) {
        this.applicationContext = applicationContext;
        this.capturerObserver = capturerObserver;
        this.surfaceHelper = surfaceTextureHelper;
        this.cameraThreadHandler =
                (surfaceTextureHelper == null) ? null : surfaceTextureHelper.getHandler();
    }

    @Override
    public void startCapture(int width, int height, int framerate) {
        Logging.d(TAG, "startCapture: " + width + "x" + height + "@" + framerate);
        if (applicationContext == null) {
            throw new RuntimeException("CameraCapturer must be initialized before calling startCapture.");
        }

        synchronized (stateLock) {
            if (sessionOpening || currentSession != null) {
                Logging.w(TAG, "Session already open");
                return;
            }

            this.width = width;
            this.height = height;
            this.framerate = framerate;

            sessionOpening = true;
            openAttemptsRemaining = MAX_OPEN_CAMERA_ATTEMPTS;
            createSessionInternal(0);
        }
    }

    private void createSessionInternal(int delayMs) {
        uiThreadHandler.postDelayed(openCameraTimeoutRunnable, delayMs + OPEN_CAMERA_TIMEOUT);
        cameraThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                createCameraSession(createSessionCallback, cameraSessionEventsHandler, applicationContext,
                        surfaceHelper, cameraName, width, height, framerate);
            }
        }, delayMs);
    }

    @Override
    public void stopCapture() {
        Logging.d(TAG, "Stop capture");

        synchronized (stateLock) {
            while (sessionOpening) {
                Logging.d(TAG, "Stop capture: Waiting for session to open");
                try {
                    stateLock.wait();
                } catch (InterruptedException e) {
                    Logging.w(TAG, "Stop capture interrupted while waiting for the session to open.");
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (currentSession != null) {
                Logging.d(TAG, "Stop capture: Nulling session");
                cameraStatistics.release();
                cameraStatistics = null;
                final ThetaSession oldSession = currentSession;
                cameraThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        oldSession.stop();
                    }
                });
                currentSession = null;
                capturerObserver.onCapturerStopped();
            } else {
                Logging.d(TAG, "Stop capture: No session open");
            }
        }

        Logging.d(TAG, "Stop capture done");
    }

    @Override
    public void changeCaptureFormat(int width, int height, int framerate) {
        Logging.d(TAG, "changeCaptureFormat: " + width + "x" + height + "@" + framerate);
        synchronized (stateLock) {
            stopCapture();
            startCapture(width, height, framerate);
        }
    }

    @Override
    public void dispose() {
        Logging.d(TAG, "dispose");
        stopCapture();
    }

    @Override
    public void switchCamera(final CameraSwitchHandler switchEventsHandler) {
        Logging.d(TAG, "switchCamera");
        cameraThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                switchCameraInternal(switchEventsHandler);
            }
        });
    }

    @Override
    public boolean isScreencast() {
        return false;
    }

    public void printStackTrace() {
        Thread cameraThread = null;
        if (cameraThreadHandler != null) {
            cameraThread = cameraThreadHandler.getLooper().getThread();
        }
        if (cameraThread != null) {
            StackTraceElement[] cameraStackTrace = cameraThread.getStackTrace();
            if (cameraStackTrace.length > 0) {
                Logging.d(TAG, "CameraCapturer stack trace:");
                for (StackTraceElement traceElem : cameraStackTrace) {
                    Logging.d(TAG, traceElem.toString());
                }
            }
        }
    }

    // <THETA> Return holding camera object
    @Nullable
    public Camera getThetaCamera() {
        return this.thetaCamera;
    }

    private void reportCameraSwitchError(
            String error, @Nullable CameraSwitchHandler switchEventsHandler) {
        Logging.e(TAG, error);
        if (switchEventsHandler != null) {
            switchEventsHandler.onCameraSwitchError(error);
        }
    }

    private void switchCameraInternal(@Nullable final CameraSwitchHandler switchEventsHandler) {
        Logging.d(TAG, "switchCamera internal");

        final String[] deviceNames = cameraEnumerator.getDeviceNames();

        if (deviceNames.length < 2) {
            if (switchEventsHandler != null) {
                switchEventsHandler.onCameraSwitchError("No camera to switch to.");
            }
            return;
        }

        synchronized (stateLock) {
            if (switchState != SwitchState.IDLE) {
                reportCameraSwitchError("Camera switch already in progress.", switchEventsHandler);
                return;
            }
            if (!sessionOpening && currentSession == null) {
                reportCameraSwitchError("switchCamera: camera is not running.", switchEventsHandler);
                return;
            }

            this.switchEventsHandler = switchEventsHandler;
            if (sessionOpening) {
                switchState = SwitchState.PENDING;
                return;
            } else {
                switchState = SwitchState.IN_PROGRESS;
            }

            Logging.d(TAG, "switchCamera: Stopping session");
            cameraStatistics.release();
            cameraStatistics = null;
            final ThetaSession oldSession = currentSession;
            cameraThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    oldSession.stop();
                }
            });
            currentSession = null;

            int cameraNameIndex = Arrays.asList(deviceNames).indexOf(cameraName);
            cameraName = deviceNames[(cameraNameIndex + 1) % deviceNames.length];

            sessionOpening = true;
            openAttemptsRemaining = 1;
            createSessionInternal(0);
        }
        Logging.d(TAG, "switchCamera done");
    }

    private void checkIsOnCameraThread() {
        if (Thread.currentThread() != cameraThreadHandler.getLooper().getThread()) {
            Logging.e(TAG, "Check is on camera thread failed.");
            throw new RuntimeException("Not on camera thread.");
        }
    }

    protected String getCameraName() {
        synchronized (stateLock) {
            return cameraName;
        }
    }

    protected void createCameraSession(
            ThetaSession.CreateSessionCallback createSessionCallback, ThetaSession.Events events,
            Context applicationContext, SurfaceTextureHelper surfaceTextureHelper, String cameraName,
            int width, int height, int framerate) {
        ThetaSession.create(createSessionCallback, events, captureToTexture, applicationContext,
                surfaceTextureHelper, ThetaEnumerator.getCameraIndex(cameraName), width, height,
                framerate);
    }
}