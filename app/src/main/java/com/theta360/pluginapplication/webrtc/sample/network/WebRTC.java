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

package com.theta360.pluginapplication.webrtc.sample.network;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.theta360.pluginapplication.webrtc.sample.network.WebRTCLibExtends.*;

import org.webrtc.*;

import java.util.Collections;
import java.util.List;

/**
 * WerRTC class
 */
public class WebRTC implements PeerConnection.Observer {
    private static final String TAG = "WebRTC";

    /**
     * Interface of notification callback function from WebRTC library
     */
    public static interface WebRTCCallbacks {
        void onCreateLocalSdp(String sdp);
        void onStartCapture(Camera thetaCamera);
        void didReceiveRemoteStream();
    }

    /*
     * Implementation class of SdpObserver interface.
     * (Override method as necessary)
     */
    private static abstract class SkeletalSdpObserver implements SdpObserver {
        private static final String TAG = "SkeletalSdpObserver";

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {}
        @Override
        public void onSetSuccess() {}
        @Override
        public void onCreateFailure(String s) {}
        @Override
        public void onSetFailure(String s) {}
    }

    private static final long WAITING_TIME_MS = 2000;
    private Handler mHandler;

    private static final int BASE_CAPTURE_WIDTH = 1920;
    private static final int BASE_CAPTURE_FPS = 30;
    private int mCaptureWidth;
    private int mCaptureHight;
    private int mCaptureFps;
    private String mStunUri = "stun:192.168.1.1:8888";

    private final Activity mActivity;
    private WebRTCCallbacks mCallbacks;
    private PeerConnectionFactory mFactory;
    private PeerConnection mPeerConnection;
    private MediaStream mLocalStream;
    private VideoCapturer mVideoCapturer;

    /**
     * Event handler for camera event from WebRTC library
     */
    private final CameraVideoCapturer.CameraEventsHandler mCameraEventHandler = new CameraVideoCapturer.CameraEventsHandler() {
        public void onCameraError(String errorDescription) {}
        public void onCameraDisconnected() {}
        public void onCameraFreezed(String errorDescription) {}
        public void onCameraOpening(String cameraName) {}
        public void onCameraOpened(Camera camera) {
            // Don't use
        }
        public void onFirstFrameAvailable() {
            if (mCallbacks != null) {
                mCallbacks.onStartCapture(((ThetaCapturer)mVideoCapturer).getThetaCamera());
            }
        }
        public void onCameraClosed() {}
    };

    /**
     * Constructor of WebRTC class
     *
     * @param activity Activity of main thread of Android application
     */
    public WebRTC(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * Setup peer connection and local stream.
     *
     * @param callbacks Implementation object of callback functions
     */
    public void connect(WebRTCCallbacks callbacks) {
        EglBase eglBase = EglBase.create();

        this.mCallbacks = callbacks;

        setupPeerConnection(eglBase);
        setupLocalStream(eglBase);

        mPeerConnection.addStream(mLocalStream);
        mHandler = new Handler();
    }

    /**
     * Dispose local stream and peer connection
     */
    public void disconnect() {
        mHandler = null;
        mPeerConnection.removeStream(mLocalStream);
        disposeLocalStream();
        mPeerConnection.dispose();
        mPeerConnection = null;
        mFactory.dispose();
        mFactory = null;
        this.mCallbacks = null;
    }

    /**
     * Start video and audio capture
     */
    public void startCapture() {
        _startCapture();
    }

    /**
     * Stop vide and audio capture
     */
    public void stopCapture() {
        _stopCapture();
    }

    /**
     * Create Session Description Protocol to offer
     */
    public void createOffer() {
        _createOffer();
    }

    /**
     * Store the received offer SDP, and create answer SDP.
     *
     * @param sdp String of Session Description Protocol
     */
    public void receiveOffer(String sdp) {
        _receiveOffer(sdp);
    }

    /**
     * Store the received answer SDP.
     *
     * @param sdp String of Session Description Protocol
     */
    public void receiveAnswer(String sdp) {
        _receiveAnswer(sdp);
    }

    /**
     * Set the frame size of the video.
     *
     * @param size Video size (1: 2K, 2; 4K)
     */
    public void setFrameSize(int size) {
        mCaptureWidth = BASE_CAPTURE_WIDTH * size;
        mCaptureHight = mCaptureWidth / 2;
        mCaptureFps = BASE_CAPTURE_FPS;
    }

    /**
     * Specify STUN server URL.
     *
     * @param uri URL.
     */
    public void setStunUri(String uri) {
        mStunUri = "stun:" + uri;
    }


    /*
     * Setup peer connection
     */
    private void setupPeerConnection(EglBase eglBase) {
        // initialize Factory
        String fieldTraials = "";
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(this.mActivity.getApplicationContext())
                .setFieldTrials(fieldTraials)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        );

        // create Factory
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(
                eglBase.getEglBaseContext(),
                false,
                false
        );
        mFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(encoderFactory)
                .createPeerConnectionFactory();

        // create PeerConnection
        List<PeerConnection.IceServer> iceServers = Collections.singletonList(PeerConnection.IceServer.builder(mStunUri).createIceServer());
        mPeerConnection = mFactory.createPeerConnection(iceServers, this);
    }

    /*
     * Create Session Description Protocol to offer
     */
    private void _createOffer() {
        mPeerConnection.createOffer(new SkeletalSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                mPeerConnection.setLocalDescription(new SkeletalSdpObserver() {}, sessionDescription);
            }
        }, WebRTCUtil.offerConnectionConstraints());
    }

    /*
     * Store the received offer SDP, and create answer SDP.
     */
    private void _receiveOffer(String sdp) {
        // setRemoteDescription
        SessionDescription remoteDescription = new SessionDescription(SessionDescription.Type.OFFER, sdp);
        mPeerConnection.setRemoteDescription(new SkeletalSdpObserver() {
            @Override
            public void onSetSuccess() {

                // createAnswer
                mPeerConnection.createAnswer(new SkeletalSdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        mPeerConnection.setLocalDescription(new SkeletalSdpObserver() {}, sessionDescription);
                    }
                }, WebRTCUtil.answerConnectionConstraints());

            }
        }, remoteDescription);
    }

    /*
     * Store the received answer SDP.
     */
    private void _receiveAnswer(String sdp) {
        SessionDescription remoteDescription = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
        mPeerConnection.setRemoteDescription(new SkeletalSdpObserver() {
            @Override
            public void onSetSuccess() {
                Log.i(TAG, "Set Remote Description Success.");
            }
        }, remoteDescription);
    }

    /*
     * Setup local video and audio stream
     */
    private void setupLocalStream(EglBase eglBase) {
        mLocalStream = mFactory.createLocalMediaStream("android_local_stream");

        mVideoCapturer = createCameraCapturer(new ThetaEnumerator(true));
        if (mVideoCapturer != null) {
            // videoTrack
            VideoSource localVideoSource = mFactory.createVideoSource(mVideoCapturer.isScreencast());
            SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("VideoCapturerThread", eglBase.getEglBaseContext());
            mVideoCapturer.initialize(surfaceTextureHelper, this.mActivity.getApplicationContext(), localVideoSource.getCapturerObserver());
            VideoTrack localVideoTrack = mFactory.createVideoTrack("android_local_videotrack", localVideoSource);
            if (!mLocalStream.addTrack(localVideoTrack)) {
                Log.e(TAG, "Add video track to stream error");
            }

            // audioTrack
            AudioSource localAudioSource = mFactory.createAudioSource(WebRTCUtil.mediaStreamConstraints());
            AudioTrack localAudioTrack = mFactory.createAudioTrack("android_local_audiotrack", localAudioSource);
            if (!mLocalStream.addTrack(localAudioTrack)) {
                Log.e(TAG, "Add audio track to stream error");
            }
        }
    }

    /*
     * Dispose local stream
     */
    private void disposeLocalStream() {
        if (mVideoCapturer != null) {
            mVideoCapturer.dispose();
            mLocalStream.dispose();
        }
    }

    /*
     * Start video and audio capture
     */
    private void _startCapture() {
        ((AudioManager)this.mActivity.getSystemService(Context.AUDIO_SERVICE)).setParameters("RicUseBFormat=false");
        mVideoCapturer.startCapture(mCaptureWidth, mCaptureHight, mCaptureFps);
    }

    /*
     * Stop video and audio capture
     */
    private void _stopCapture() {
        try {
            mVideoCapturer.stopCapture();
        } catch (InterruptedException e) {
            Log.e(TAG, "Stop Capture error.");
        }
        ((AudioManager)this.mActivity.getSystemService(Context.AUDIO_SERVICE)).setParameters("RicUseBFormat=true");
    }

    /*
     * Create camera video capture object
     */
    private VideoCapturer createCameraCapturer(ThetaEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, mCameraEventHandler);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {}
    /**
     * {@inheritDoc}
     */
    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {}
    /**
     * {@inheritDoc}
     */
    @Override
    public void onIceConnectionReceivingChange(boolean b) {}
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRemoveStream(MediaStream mediaStream) {}
    /**
     * {@inheritDoc}
     */
    @Override
    public void onDataChannel(DataChannel dataChannel) {}
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRenegotiationNeeded() {}
    /**
     * {@inheritDoc}
     */
    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {}
    /**
     * {@inheritDoc}
     */
    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.i(TAG, "IceCandidate: " + iceCandidate);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}
    /**
     * {@inheritDoc}
     */
    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.i(TAG, "onIceGatheringChange->" + iceGatheringState);

        // Since it takes time to wait until COMPLETE, SDP transmission processing is performed after specified seconds from the start of ICE collection.
        if (iceGatheringState == PeerConnection.IceGatheringState.GATHERING) {
            final Runnable sendSDP = new Runnable() {
                public void run() {
                    Log.d(TAG, "Passed " + (WAITING_TIME_MS / 1000) + " sec");
                    SessionDescription localSdp = mPeerConnection.getLocalDescription();
                    if (mCallbacks != null) {
                        mCallbacks.onCreateLocalSdp(localSdp.description);
                    }
                }
            };
            mHandler.postDelayed(sendSDP, WAITING_TIME_MS);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void onAddStream(MediaStream mediaStream) {
        if (mediaStream.videoTracks.size() == 0) {
            return;
        }
    }
}