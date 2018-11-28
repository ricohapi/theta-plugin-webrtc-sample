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

package com.theta360.pluginapplication.webrtc.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.InetAddress;
import java.net.NetworkInterface;

import org.java_websocket.WebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.koushikdutta.async.http.server.AsyncHttpServerResponse;

import com.theta360.pluginapplication.webrtc.sample.model.CameraOption;
import com.theta360.pluginapplication.webrtc.sample.network.model.commands.CommandsName;
import com.theta360.pluginapplication.webrtc.sample.network.model.requests.CommandsRequest;
import com.theta360.pluginapplication.webrtc.sample.network.model.responses.CommandsResponse;
import com.theta360.pluginapplication.webrtc.sample.network.model.values.*;
import com.theta360.pluginapplication.webrtc.sample.network.*;
import com.theta360.pluginapplication.webrtc.sample.task.TakePictureTask;
import com.theta360.pluginapplication.webrtc.sample.task.LiveViewTask;

import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.receiver.KeyReceiver;
import com.theta360.pluginlibrary.callback.KeyCallback;

/**
 * Plug-in main activity class
 */
public class MainActivity extends PluginActivity {
    private static final String TAG = "MainActivity";

    /*
     * Signaling status
     */
    private enum SigState {
        Disconnected,
        Connecting,
        Connected,
        Offering,
        ReceivedOffer,
        ReceivedAnswer,
        CreatingAnswer,
        Done
    }

    /*
     * Video size
     */
    private enum VideoSize {
        VideoSize2K,
        VideoSize4K
    }

    /*
     * Finish status
     */
    private enum FinishStatus {
        Success,
        ModeKeyLongPress,
        Failure
    }

    // For Plug-in control
    private static final String NO_MESSAGE = "";
    private FinishStatus mFinishStatus = FinishStatus.Success;
    private String mFinishMessage;

    // Array of permission to confirm
    private static final String[] REQUEST_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    // Request code for confirm permissions
    private static final int REQUEST_CODE_MULTI_PERMISSIONS = 1;

    // Instance of WebRTC
    private WebRTC mWebRTC = null;
    private boolean mWebRTCConnected = false;
    private Handler mWebRTCHandler = null;

    // For WebSocket connection retry
    private static final int MAX_WS_CONNECT_RETRY_CNT = 3;
    private static final long WS_CONNECT_RETRY_WAITTIME = 5000;
    private boolean mNeedRetry = false;
    private int mRetryCounter = 0;
    private Handler mHandler = null;

    // Server address and port
    private static final String WIFI_INTERFACE_NAME = "wlan0";
    private static final String LOCAL_LOOPBACK_ADDR = "127.0.0.1";
    private static final String INVALID_IP_ADDR = "0.0.0.0";
    private static final int HTTP_PORT_NO = 8888;
    private static final String WS_URI_PREFIX = "ws://";
    private static final int WS_PORT_NO = HTTP_PORT_NO + 1;
    private String mServerAddr = null;
    private String mWsServerUri = null;

    // Send offer / Recv answer state
    private boolean mTypeOffer = false;
    private SigState mState = SigState.Disconnected;

    // HTTPD/WevSocketServer
    private WebServer mWebServer = null;
    private SignalingServer mSigServer = null;

    // WebSocket client
    private WebRTCSocketClient mWsClient = null;

    // RTP Session max bandwidth (Kbits/sec)
    private static final int MAX_RTP_SESSION_BANDWIDTH = 40000;

    // Camera and Parameters
    private Camera mCamera = null;
    CameraOption mCameraOptions = new CameraOption();

    // Shooting status
    private Status mShootingStatus = Status.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        mWebRTCHandler = new Handler();

        /*
         * Get myself IP address
         */
        mServerAddr = getIPAddress(WIFI_INTERFACE_NAME);
        if (mServerAddr == null) {
            mFinishStatus = FinishStatus.Failure;
            mFinishMessage = "Wi-Fi Network is off";
            Log.e(TAG, mFinishMessage);
            finishAndRemoveTask();
        }

        /*
         * Start HTTP server/WebSocket server
         */
        if (!startServices()) {
            mFinishStatus = FinishStatus.Failure;
            mFinishMessage = "Start services error";
            Log.e(TAG, mFinishMessage);
            finishAndRemoveTask();
        }

        /*
         * Register button action
         */
        registerButtonAction();

        /*
         * Setup application View (for debug)
         */
        setupView();

        /*
         * Create WebSocket client
         */
        mWsClient = new WebRTCSocketClient(mWsServerUri);
        mNeedRetry = true;
        mRetryCounter = 0;
        mHandler = new Handler();

        /*
         * Connect to WebSocket server on same host.
         */
        wsConnect();

        /*
         * Check permission
         */
        if (checkPermissions()) {
            // Create and Start WebRTC
            mWebRTC = new WebRTC(this);
        } else {
            // Not have the required permissions.
            mFinishStatus = FinishStatus.Failure;
            mFinishMessage = "Not have the required permissions.";
            finishAndRemoveTask();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");

        // Plug-in terminate
        if (mFinishStatus != FinishStatus.ModeKeyLongPress) {
            finishPlugin(mFinishStatus, mFinishMessage);
        }
    }

    /*
     * Check permission
     */
    private boolean checkPermissions() {
        Log.d(TAG, "checkPermissions");

        for (String permission : REQUEST_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /*
     * Register button action
     */
    private void registerButtonAction() {
        Log.d(TAG, "registerButtonAction");

        setKeyCallback(new KeyCallback() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onKeyDown(int keyCode, KeyEvent event) {}
            /**
             * {@inheritDoc}
             */
            @Override
            public void onKeyUp(int keyCode, KeyEvent event) {}
            /**
             * {@inheritDoc}
             */
            @Override
            public void onKeyLongPress(int keyCode, KeyEvent event) {
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) {
                    if ((event.getFlags() & KeyEvent.FLAG_LONG_PRESS) != 0) {
                        // When the mode key is pressed and held for a long time, the plug-in is finished
                        mFinishStatus = FinishStatus.ModeKeyLongPress;
                        finishPlugin(mFinishStatus, NO_MESSAGE);
                    }
                }
            }
        });
    }

    /*
     * Setup application View (for debug)
     */
    private void setupView() {
        Log.d(TAG, "setupView");

        // Disable screen sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Display IP address and port number
        TextView ipText = (TextView)findViewById(R.id.textIp);
        TextView portText = (TextView)findViewById(R.id.textPort);
        ipText.setText(mServerAddr, BufferType.NORMAL);
        portText.setText(String.valueOf(HTTP_PORT_NO), BufferType.NORMAL);
    }

    /*
     * Plugin termination
     */
    private void finishPlugin(FinishStatus exit_status, String message) {
        Log.d(TAG, "finishPlugin");

        /*
         * Stop WebRTC, if not stopped.
         */
        stopWebRTC();

        /*
         * Connection with the signaling server terminated
         */
        mNeedRetry = false;
        mRetryCounter = 0;
        if (mWsClient != null) {
            mWsClient.closeWebSocket();
        }

        /*
         * Stop HTTP server/WebSocket server
         */
        stopServices();

        /*
         * Plug-in completion notification
         */
        switch (exit_status) {
        case Success:
            notificationSuccess();
            break;
        case Failure:
            if (message == null) {
                notificationError(NO_MESSAGE);
            } else {
                notificationError(message);
            }
            break;
        case ModeKeyLongPress:
        default:
            sleep(1000);
            break;
        }
    }

    /*
     * Start HTTP server/WebSocket server
     */
    private boolean startServices() {
        boolean sigServerResult = false;
        boolean webServerResult = false;

        Log.d(TAG, "startServices");

        try {
            // Start signaling server
            mSigServer = new SignalingServer(new SignalingServer.SignalingServerCallbacks() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onConnected(WebSocket conn) {
                    Log.d(TAG, "SignalingServer onConnected");
                }
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onDisconnected(WebSocket conn) {
                    Log.d(TAG, "SignalingServer onDisconnected");
                    mWebRTCHandler.post(new Runnable() {
                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public void run() {
                            stopWebRTC();
                        }
                    });
                    changeState(SigState.Connected);
                }
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onRecvMessage(WebSocket conn, String message) {
                    Log.d(TAG, "SignalingServer onRecvMessage");
                }
            }, mServerAddr, WS_PORT_NO);
            mSigServer.setReuseAddr(true);
            mSigServer.start();
            mWsServerUri = WS_URI_PREFIX + mServerAddr + ":" + WS_PORT_NO + "/";
            sigServerResult = true;
            // Start HTTP server
            mWebServer = new WebServer(getApplicationContext(), null, mWebServerCallback);
            webServerResult = true;
        } catch (IllegalStateException e) {
            Log.e(TAG, "SignalingServer start error");
        }

        if (!sigServerResult || !webServerResult) {
            stopServices();
        }

        return (sigServerResult && webServerResult);
    }

    /*
     * Stop services
     */
    private void stopServices() {
        Log.d(TAG, "stopServices");

        // Stop HTTP server
        if (mWebServer != null) {
            mWebServer.stop();
            mWebServer = null;
        }
        // Stop signaling server
        if (mSigServer != null) {
            try {
                mSigServer.stop(1000);
                mSigServer = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "Signaling server stop error (InterruptedException)");
            }
        }
    }

    /*
     * Start WebRTC
     */
    private void startWebRTC() {
        Log.d(TAG, "startWebRTC");

        if (mWebRTCConnected) {
            // If you have already started doing nothing
            return;
        }

        mWebRTC.connect(new WebRTC.WebRTCCallbacks() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onCreateLocalSdp(String sdp) {
                String description = preferCodec(sdp, "H264", false);
                description = setSDPRTPSessionBandwidth(description, MAX_RTP_SESSION_BANDWIDTH);
                description = setSDPRequestOnly(description);

                String type = (mTypeOffer) ? "offer" : "answer";
                Log.d(TAG, "WebRTCCallbacks.onCreateLocalSdp(" + type + ")");
                JSONObject json = new JSONObject();
                try {
                    json.put("type", type);
                    json.put("sdp", description);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception");
                    throw new RuntimeException(e);
                }

                if (mWsClient != null) {
                    // Send offer
                    mWsClient.sendWebSocketMessage(json.toString());
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onStartCapture(Camera thetaCamera) {
                if (thetaCamera != null) {
                    mCamera = thetaCamera;
                    mCameraOptions.getCameraParameters(mCamera, false);
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void didReceiveRemoteStream() {
                changeState(SigState.Done);
            }
        });

        mWebRTCConnected = true;
    }

    /*
     * Stop WebRTC
     */
    private void stopWebRTC() {
        Log.d(TAG, "stopWebRTC");

        if (! mWebRTCConnected) {
            // If you have finished already, I will not do anything
            return;
        }

        stopPreview();
        mWebRTC.disconnect();

        mWebRTCConnected = false;
    }

    /*
     * Start preview
     */
    private void startPreview(VideoSize videoSize) {
        Log.d(TAG, "startPreview");

        notificationCameraClose();
        sleep(300);
        mWebRTC.setFrameSize((videoSize == VideoSize.VideoSize2K) ? 1 : 2);
        mWebRTC.startCapture();
    }

    /*
     * Stop preview
     */
    private void stopPreview() {
        Log.d(TAG, "stopPreview");

        mWebRTC.stopCapture();
        sleep(300);
        mCamera = null;
        notificationCameraOpen();
        sleep(600);
    }

    /*
     * Execute still capture
     */
    private void takePicture(AsyncHttpServerResponse response, CommandsRequest commandsRequest) {
        new TakePictureTask(new TakePictureTask.Callback() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onPreExecute() {
                Log.d(TAG, "TakePictureTask.onPreExecute()");
                mShootingStatus = Status.SHOOTING;
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public void onSendCommand(AsyncHttpServerResponse response, CommandsRequest commandsRequest, Errors errors) {
                Log.d(TAG, "TakePictureTask.onSendCommand()");
                CommandsName commandsName = commandsRequest.getCommandsName();
                if (errors == null) {
                    CommandsResponse commandsResponse = new CommandsResponse(commandsName, State.DONE);
                    mWebServer.sendCommandsResponse(response, commandsResponse);
                } else {
                    mWebServer.sendError(response, errors, commandsName);
                }
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public void onTakePicture(String fileUrl) {
                Log.d(TAG, "TakePictureTask.onTakePicture(): " + fileUrl);
                mShootingStatus = Status.IDLE;
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public void onCompleted() {
                Log.d(TAG, "TakePictureTask.onCompleted()");
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "TakePictureTask.onError(): " + errorMessage);
            }
        }, response, commandsRequest).execute();
    }

    /*
     * Execute start live view
     */
    private void startLiveView(AsyncHttpServerResponse response, CommandsRequest commandsRequest) {
        new LiveViewTask(new LiveViewTask.Callback() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onPreExecute() {
                Log.e(TAG, "LiveViewTask.onPreExecute()");
                mWebRTCHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startWebRTC();
                    }
                });
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public void onSendCommand(AsyncHttpServerResponse response, CommandsRequest commandsRequest, Errors errors) {
                Log.e(TAG, "LiveViewTask.onSendCommand()");
                String videoSize = "2K";
                JSONObject json;
                try {
                    json = commandsRequest.getCommandsParameters().getJSONObject("options");
                    videoSize = json.optString("videoSize");
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    startPreview(videoSize.equals("2K") ? VideoSize.VideoSize2K : VideoSize.VideoSize4K);
                    if (mState == SigState.Connected) {
                        // Create offer
                        createOffer();
                    }
                    CommandsName commandsName = commandsRequest.getCommandsName();
                    if (errors == null) {
                        CommandsResponse commandsResponse = new CommandsResponse(commandsName, State.DONE);
                        mWebServer.sendCommandsResponse(response, commandsResponse);
                    } else {
                        mWebServer.sendError(response, errors, commandsName);
                    }
                }
            }
        }, response, commandsRequest).execute();
    }

    /*
     * Execute stop live view
     */
    private void stopLiveView(AsyncHttpServerResponse response, CommandsRequest commandsRequest) {
        new LiveViewTask(new LiveViewTask.Callback() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onPreExecute() {
                Log.e(TAG, "LiveViewTask.onPreExecute()");
                mWebRTCHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        stopWebRTC();
                        changeState(SigState.Connected);
                    }
                });
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public void onSendCommand(AsyncHttpServerResponse response, CommandsRequest commandsRequest, Errors errors) {
                Log.e(TAG, "LiveViewTask.onSendCommand()");
                CommandsName commandsName = commandsRequest.getCommandsName();
                if (errors == null) {
                    CommandsResponse commandsResponse = new CommandsResponse(commandsName, State.DONE);
                    mWebServer.sendCommandsResponse(response, commandsResponse);
                } else {
                    mWebServer.sendError(response, errors, commandsName);
                }
            }
        }, response, commandsRequest).execute();
    }

    /*
     * Connect to WebSocket server
     */
    private void wsConnect() {
        boolean result = false;

        Log.d(TAG, "wsConnect");

        result = mWsClient.openWebSocket(new WebRTCSocketClient.WebSocketCallbacks() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onConnected() {
                wsConnected();
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public void onDisconnected() {
                wsDisonnected();
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public void onRecvMessage(String payload) {
                wsRecvMessage(payload);
            }
        });
        if (result) {
            changeState(SigState.Connecting);
        } else {
            Log.d(TAG, "WebSocket connect error.");
        }
    }

    /*
     * Connection to WebSocket server completed
     */
    private void wsConnected() {
        Log.d(TAG, "wsConnected");

        mNeedRetry = false;
        changeState(SigState.Connected);
    }

    /*
     * Disconnected from WebSocketServer
     */
    private void wsDisonnected() {
        Log.d(TAG, "wsDisonnected");

        // Retry at connection failure, after specified seconds
        if (mNeedRetry) {
            if (++mRetryCounter < MAX_WS_CONNECT_RETRY_CNT) {
                final Runnable retryConnection = new Runnable() {
                    public void run() {
                        mWsClient = null;
                        mWsClient = new WebRTCSocketClient(mWsServerUri);
                        wsConnect();
                    }
                };
                mHandler.postDelayed(retryConnection, WS_CONNECT_RETRY_WAITTIME);
            }
        }
    }

    /*
     * Receiving and dispatching messages
     */
    private void wsRecvMessage(String sdp) {
        Log.d(TAG, "wsRecvMessage");

        try {
            JSONObject json = new JSONObject(sdp);
            String type = json.optString("type");
            switch (type) {
            case "offer":
                // Receive offer SDP
                if (mTypeOffer) {
                    return;
                }
                receiveOffer(json);
                break;
            case "answer":
                // Receive answer SDP
                if (!mTypeOffer) {
                    return;
                }
                receiveAnswer(json);
                break;
            default:
                Log.e(TAG, "Unknown resuest. Ignore.");
                break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error");
        }
    }

    /*
     * WebServer Callbacks
     */
    private WebServer.Callback mWebServerCallback = new WebServer.Callback() {
        @Override
        public void commandsRequest(AsyncHttpServerResponse response, CommandsRequest commandsRequest) {
            CommandsName commandsName = commandsRequest.getCommandsName();
            CommandsResponse commandsResponse;
            HttpConnector camera;
            String responseData;
            JSONObject json = null;
            Log.d("commandsName: ", commandsName.toString());
            switch (commandsName) {
            case TAKE_PICTURE:
                // Execution and response of 'takePicture' command
                json = createJSONFromJSON("options", mCameraOptions.getAllOptions());
                commandsRequest.setCommandParameters(json);
                takePicture(response, commandsRequest);
                break;
            case SET_OPTIONS:
                // Execution and response of 'setOptions' command
                commandsResponse = new CommandsResponse(commandsName, State.DONE);
                mWebServer.sendCommandsResponse(response, commandsResponse);
                try {
                    json = commandsRequest.getCommandsParameters().getJSONObject("options");
                    mCameraOptions.setOptions(json);
                    mCameraOptions.setCameraParameters(mCamera);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case GET_OPTIONS:
                // Execution and response of 'getOptions' command
                try {
                    JSONArray optionName = commandsRequest.getCommandsParameters().optJSONArray("optionNames");
                    ArrayList<String> optionNameList = new ArrayList<String>();
                    for (int i = 0; i < optionName.length(); i++) {
                        optionNameList.add(optionName.getString(i));
                    }
                    json = createJSONFromJSON("results",
                            createJSONFromJSON("options",
                                    mCameraOptions.getOptions(optionNameList)
                            )
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mWebServer.sendGetOptionsResponse(response, json.toString());
                break;
            case START_LIVE_PREVIEW:
                // Execution and response of 'startLivePreview' command
                startLiveView(response, commandsRequest);
                break;
            case STOP_LIVE_PREVIEW:
                // Execution and response of 'stopLivePreview' command
                stopLiveView(response, commandsRequest);
                break;
            case GET_SHOOTING_STATUS:
                // Response of shooting status
                json = new JSONObject();
                try {
                    json.put("status", mShootingStatus.toString());
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception");
                    throw new RuntimeException(e);
                }
                mWebServer.sendGetOptionsResponse(response, json.toString());
                break;
            case SET_SETTINGS:
                // Execution and response of 'setOptions' command
                commandsResponse = new CommandsResponse(commandsName, State.DONE);
                mWebServer.sendCommandsResponse(response, commandsResponse);
                camera = new HttpConnector("127.0.0.1:8080");
                camera.setOptions(commandsRequest.getCommandsParameters());
                break;
            case GET_SETTINGS:
                // Execution and response of 'getOptions' command
                camera = new HttpConnector("127.0.0.1:8080");
                responseData = camera.getOptions(commandsRequest.getCommandsParameters());
                mWebServer.sendGetOptionsResponse(response, responseData);
                break;
            case GET_STATUS:
                // Execution and response of 'State' command
                camera = new HttpConnector("127.0.0.1:8080");
                responseData = camera.getState();
                mWebServer.sendGetOptionsResponse(response, responseData);
                break;
            default:
                mWebServer.sendUnknownCommand(response);
                break;
            }
        }
    };

    /*
     * Create offer SDP
     */
    private void createOffer() {
        changeState(SigState.Offering);
        mTypeOffer = true;
        mWebRTC.createOffer();
    }

    /*
     * Receive offer SDP
     */
    private void receiveOffer(JSONObject json) {
        changeState(SigState.CreatingAnswer);
        String description = json.optString("sdp");
        mWebRTC.receiveOffer(description);
    }

    /*
     * Receive answer SDP
     */
    private void receiveAnswer(JSONObject json) {
        changeState(SigState.ReceivedAnswer);
        String description = json.optString("sdp");
        mWebRTC.receiveAnswer(description);
    }

    /*
     * Make settings that give priority to the specified codec
     */
    private static String preferCodec(String sdpDescription, String codec, boolean isAudio) {
        final String[] lines = sdpDescription.split("\r\n");
        final int mLineIndex = findMediaDescriptionLine(isAudio, lines);
        if (mLineIndex == -1) {
            Log.w(TAG, "No mediaDescription line, so can't prefer " + codec);
            return sdpDescription;
        }
        // A list with all the payload types with name |codec|. The payload types are integers in the
        // range 96-127, but they are stored as strings here.
        final List<String> codecPayloadTypes = new ArrayList<>();
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        final Pattern codecPattern = Pattern.compile("^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$");
        for (String line : lines) {
            Matcher codecMatcher = codecPattern.matcher(line);
            if (codecMatcher.matches()) {
                codecPayloadTypes.add(codecMatcher.group(1));
            }
        }
        if (codecPayloadTypes.isEmpty()) {
            Log.w(TAG, "No payload types with name " + codec);
            return sdpDescription;
        }

        final String newMLine = movePayloadTypesToFront(codecPayloadTypes, lines[mLineIndex]);
        if (newMLine == null) {
            return sdpDescription;
        }
        Log.d(TAG, "Change media description from: " + lines[mLineIndex] + " to " + newMLine);
        lines[mLineIndex] = newMLine;
        return joinString(Arrays.asList(lines), "\r\n", true /* delimiterAtEnd */);
    }

    private static int findMediaDescriptionLine(boolean isAudio, String[] sdpLines) {
        String mediaDescription = isAudio ? "m=audio " : "m=video ";
        for (int i = 0; i < sdpLines.length; ++i) {
            if (sdpLines[i].startsWith(mediaDescription)) {
                return i;
            }
        }
        return -1;
    }

    private static String joinString(
            Iterable<? extends CharSequence> s, String delimiter, boolean delimiterAtEnd) {
        Iterator<? extends CharSequence> iter = s.iterator();
        if (!iter.hasNext()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next());
        }
        if (delimiterAtEnd) {
            buffer.append(delimiter);
        }
        return buffer.toString();
    }

    private static String movePayloadTypesToFront(List<String> preferredPayloadTypes, String mLine) {
        // The format of the media description line should be: m=<media> <port> <proto> <fmt> ...
        final List<String> origLineParts = Arrays.asList(mLine.split(" "));
        if (origLineParts.size() <= 3) {
            Log.e(TAG, "Wrong SDP media description format: " + mLine);
            return null;
        }
        final List<String> header = origLineParts.subList(0, 3);
        final List<String> unpreferredPayloadTypes =
                new ArrayList<>(origLineParts.subList(3, origLineParts.size()));
        unpreferredPayloadTypes.removeAll(preferredPayloadTypes);
        // Reconstruct the line with |preferredPayloadTypes| moved to the beginning of the payload
        // types.
        final List<String> newLineParts = new ArrayList<>();
        newLineParts.addAll(header);
        newLineParts.addAll(preferredPayloadTypes);
        newLineParts.addAll(unpreferredPayloadTypes);
        return joinString(newLineParts, " ", false /* delimiterAtEnd */);
    }

    private static String setSDPRequestOnly(String sdpDescription) {
        return sdpDescription.replace("sendrecv", "sendonly");
    }

    private static String setSDPRTPSessionBandwidth(String sdpDescription, int videoBitrate) {
        return sdpDescription.replace("a=mid:video\r\n", "a=mid:video\r\nb=AS:" + videoBitrate + "\r\n");
    }

    /*
     * Obtain own IP address
     */
    private static String getIPAddress(String interfaceName) {
        try {
            NetworkInterface ni = NetworkInterface.getByName(interfaceName);
            if (ni == null) {
                Log.e(TAG, "Failed to get network interface.");
                return null;
            }

            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                String address = addresses.nextElement().getHostAddress();
                if (!LOCAL_LOOPBACK_ADDR.equals(address) && !INVALID_IP_ADDR.equals(address)) {
                    // Found valid ip address.
                    return address;
                }
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Exception occured: " + e);
            return null;
        }
    }

    /*
     * Create a JSONObject with the given JSONObject as a child element of the specified key
     */
    private JSONObject createJSONFromJSON(String key, JSONObject obj) {
        JSONObject newObj = new JSONObject();
        try {
            newObj.put(key, obj);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return newObj;
        }
    }

    /*
     * Create a JSONObject with the given JSONArray as a child element of the specified key
     */
    private JSONObject createJSONFromJSON(String key, JSONArray obj) {
        JSONObject newObj = new JSONObject();
        try {
            newObj.put(key, obj);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return newObj;
        }
    }

    /*
     * Sleep for the specified time
     */
    private static void sleep(int milisec) {
        try {
            Thread.sleep(milisec);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /*
     * Manage the connection status of WebRTC
     */
    private void changeState(final SigState state) {
        this.mState = state;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String stat = "";
                switch (state) {
                case Connected:
                    stat = "Connected";
                    break;
                case Connecting:
                    stat = "Connecting...";
                    break;
                case Offering:
                    stat = "Offering...";
                    break;
                case CreatingAnswer:
                    stat = "CreatingAnswer...";
                    break;
                case ReceivedOffer:
                    stat = "ReceivedOffer";
                    break;
                case ReceivedAnswer:
                    stat = "ReceivedAnswer";
                    break;
                case Done:
                    stat = "OK!";
                default:
                    break;
                }

                // Display status (for debug)
                setStatusText(stat);
            }
        });
    }

    /*
     * Display status to application View (for debug)
     */
    private void setStatusText(String stateText) {
        TextView statusText = (TextView)findViewById(R.id.status_label);
        statusText.setText(statusText.getText() + "\n" + stateText);
    }
}
