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

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;

/**
 * Web Socket Client class for WebRTC
 * (Used to connect to the Signaling server on the same host.)
 */
public class WebRTCSocketClient extends WebSocketClient {
    private static final String TAG = "WebRTCSocketClient";
    private WebSocketCallbacks mWebSocketCallbacks = null;

    /**
     * Interface of notification callback function from Web Socket client library
     */
    public static interface WebSocketCallbacks {
        public void onConnected();
        public void onDisconnected();
        public void onRecvMessage(String sdp);
    }

    /**
     * Constructor of Web Socket client class
     *
     * @param serverUri Server URI
     */
    public WebRTCSocketClient(String serverUri) {
        super(URI.create(serverUri));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen( ServerHandshake handshakedata ) {
        Log.d(TAG, "WS Connect: HttpStatus = " + handshakedata.getHttpStatus() + ", HttpStatusMessage = " + handshakedata.getHttpStatusMessage());
        if (mWebSocketCallbacks != null) {
            mWebSocketCallbacks.onConnected();
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage( String message ) {
        Log.d(TAG, "WS Recv");
        if (mWebSocketCallbacks != null) {
            mWebSocketCallbacks.onRecvMessage(message);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose( int code, String reason, boolean remote ) {
        Log.d(TAG, "WS Close");
        if (mWebSocketCallbacks != null) {
            mWebSocketCallbacks.onDisconnected();
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void onError( Exception ex ) {
        Log.e(TAG, "WS Error: " + ex);
    }

    /**
     * Initiates the websocket connection
     *
     * @param webSocketCallbacks Implementation object of callback functions
     * @return true: success, false: failed
     */
    public boolean openWebSocket(WebRTCSocketClient.WebSocketCallbacks webSocketCallbacks) {
        Log.d(TAG, "openWebSocket");
        boolean result = true;
        this.mWebSocketCallbacks = webSocketCallbacks;

        try {
            connect();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException: " + e);
            result = false;
        }

        return result;
    }

    /**
     * Initiates the websocket close handshake.
     */
    public void closeWebSocket() {
        Log.d(TAG, "closeWebSocket");
        close();
    }

    /**
     * Sends message to the connected websocket server.
     *
     * @param message Message for send
     */
    public void sendWebSocketMessage(final String message) {
        Log.d(TAG, "sendWebSocketMessage");
        try {
            send(message);
        } catch (NotYetConnectedException e) {
            Log.d(TAG, "NotYetConnectedException: " + e);
        }
    }
}