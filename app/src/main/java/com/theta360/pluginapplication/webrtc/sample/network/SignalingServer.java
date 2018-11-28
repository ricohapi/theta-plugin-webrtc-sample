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
import java.util.Collection;
import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;

/**
 * Signaling erver class
 */
public class SignalingServer extends WebSocketServer {
    private static final String TAG = "SignalingServer";
    private SignalingServerCallbacks mSigServerCallbacks = null;

    /**
     * Interface of notification callback function from signaling server
     */
    public static interface SignalingServerCallbacks {
        public void onConnected(WebSocket conn);
        public void onDisconnected(WebSocket conn);
        public void onRecvMessage(WebSocket conn, String message);
    }

    /**
     * Constructor of Signaling server class
     *
     * @param sigServerCallbacks Implementation object of callback functions
     * @param hostname Host name of Signaling server
     * @param port Port number of Signaling server
     */
    public SignalingServer(SignalingServerCallbacks sigServerCallbacks, String hostname, int port) {
        super(new InetSocketAddress(hostname, port));
        this.mSigServerCallbacks = sigServerCallbacks;
    }

    /**
     * Constructor of Signaling server class
     *
     * @param sigServerCallbacks Implementation object of callback functions
     * @param port Port number of Signaling server
     */
    public SignalingServer(SignalingServerCallbacks sigServerCallbacks, int port) {
        super(new InetSocketAddress(port));
        this.mSigServerCallbacks = sigServerCallbacks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.d(TAG, "session open");
        if (mSigServerCallbacks != null) {
            this.mSigServerCallbacks.onConnected(conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(TAG, "session close to " + conn.getRemoteSocketAddress().getAddress());
        if (mSigServerCallbacks != null) {
            this.mSigServerCallbacks.onDisconnected(conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(TAG, "recv message: ");
        if (mSigServerCallbacks != null) {
            this.mSigServerCallbacks.onRecvMessage(conn, message);
        }

        /*
         * Broadcast the received message to the connection destination excluding the sending host
         */
        Collection<WebSocket> connections = this.getConnections();
        connections.forEach((WebSocket connection) -> {
            if (connection.getRemoteSocketAddress().getAddress() != conn.getRemoteSocketAddress().getAddress()) {
                if (connection.isOpen()) {
                    connection.send(message);
                    Log.d(TAG, "Send msg to " + connection.getRemoteSocketAddress().getAddress());
                }
            } else {
                Log.d(TAG, "skip sender");
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        Log.d(TAG, "server start");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e(TAG, "error: " + ex);
        this.mSigServerCallbacks = null;
    }
}
