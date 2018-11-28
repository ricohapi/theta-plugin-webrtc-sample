/**
 * Video size and bitrate definition
 */
const VIDEO_SIZE_2K = '2K';
const VIDEO_SIZE_4K = '4K';
const VIDEO_SIZE_CURRENT = '';
const PREVIEW_VIDEO_BITRATE_2K = 6500;
const PREVIEW_VIDEO_BITRATE_4K = 40000;

/**
 * Audio volume definition
 *  (Min: 0.0, Max: 1.0)
 */
const AUDIO_VOLUME_DEFAULT = 0.5;

/**
 * WebRTC WebAPI prefix
 */
RTCPeerConnection = window.RTCPeerConnection || window.webkitRTCPeerConnection || window.mozRTCPeerConnection;
RTCSessionDescription = window.RTCSessionDescription || window.webkitRTCSessionDescription || window.mozRTCSessionDescription;

/**
 * Globals
 */
let videSize = VIDEO_SIZE_2K;
let remoteVideo = null;
let peerConnection = null;
let webSocket = null;

/**
 * Initialize preview when page was loaded
 */
function initPreview() {
    remoteVideo = document.getElementById('previewImage');

    /*
     * Disable shoot button and setting button
     */
    disableShootButton();

    /*
     * Make WebSocket URL
     */
    let wsHostName = location.hostname;
    let wsPort = parseInt(location.port) + 1;
    let wsUrl = 'ws://' + wsHostName + ':' + wsPort + '/';

    /*
     * Connect WebSocket
     */
    webSocket = new WebSocket(wsUrl);
    if (webSocket != null) {
        /*
         * WebSocket callback functions definition
         */
        webSocket.onopen = function(evt) {
            console.log('WebSocket open()');
        };
        webSocket.onerror = function(err) {
            console.error('WebSocket onerror() ERR:', err);
        };
        webSocket.onmessage = function(evt) {
            console.log('WebSocket onmessage() data:', evt.data);
            let message = JSON.parse(evt.data);
            switch (message.type) {
            case 'offer': {
                // --- got offer ---
                console.log('Received offer ...');
                let offer = new RTCSessionDescription(message);
                setOffer(offer);
                break;
            }
            case 'disconnect': {
                console.log('Received disconnect ...');
                if (peerConnection) {
                    hangUp();
                }
                break;
            }
            }
        };
    } else {
        console.log('WebSocket connect error');
    }

    changeViewSize();
}

//
// ---------------------- Media handling -----------------------
//
/**
 * Play video
 */
function playVideo() {
    playVideoInternal(remoteVideo, peerConnection.getRemoteStreams()[0]);
}

/**
 * Pause video
 */
function pauseVideo() {
    pauseVideoInternal(remoteVideo);
}

/**
 * Play video (internal use)
 */
function playVideoInternal(element, stream) {
    if ('srcObject' in element) {
        element.srcObject = stream;
    } else {
        element.src = window.URL.createObjectURL(stream);
    }
    element.play();
    element.volume = AUDIO_VOLUME_DEFAULT;
}

/**
 * Pause video (internal use)
 */
function pauseVideoInternal(element) {
    element.pause();
    if ('srcObject' in element) {
        element.srcObject = null;
    } else {
        if (element.src && (element.src !== '')) {
            window.URL.revokeObjectURL(element.src);
        }
        element.src = '';
    }
}

//
// ---------------------- Connection handling -----------------------
//
/**
 * Sending SDP
 */
function sendSdp(sessionDescription) {
    console.log('---sending sdp ---');
    /*
     * Set bitrate
     */
    let videoBitRate = (videSize === VIDEO_SIZE_2K) ? PREVIEW_VIDEO_BITRATE_2K : PREVIEW_VIDEO_BITRATE_4K;
    sessionDescription.sdp = sessionDescription.sdp.replace(/a=mid:video\r\n/g, 'a=mid:video\r\na=framerate:30.0\r\nb=AS:' + videoBitRate + '\r\n');

    /*
     * Send SDP to signaling server
     */
    let message = JSON.stringify(sessionDescription);
    console.log('sending SDP=' + message);
    webSocket.send(message);
}

/**
 * Prepare new connection
 */
function prepareNewConnection() {
    let pc_config = { 'iceServers':[] };
    let peer = new RTCPeerConnection(pc_config);

    /*
     * On get remote stream (Currently unused)
     */
    if ('ontrack' in peer) {
        peer.ontrack = function(event) {
            console.log('-- peer.ontrack()');
            let stream = event.streams[0];
            playVideoInternal(remoteVideo, stream);
        };
    } else {
        peer.onaddstream = function(event) {
            console.log('-- peer.onaddstream()');
            let stream = event.stream;
            playVideoInternal(remoteVideo, stream);
        };
    }

    /*
     * On get local ICE candidate
     */
    peer.onicecandidate = function (evt) {
        if (evt.candidate) {
            console.log(evt.candidate);

            // For Trickle ICE, send ICE candidate to the other peer.
            // In the case of Vanilla ICE, Do nothing.
        } else {
            console.log('empty ice event');

            // For Trickle ICE, send ICE candidate to the other peer.
            // In case of Vanilla ICE, send SDP including ICE candidate to the other peer.
            sendSdp(peer.localDescription);

            /*
             * Enable shoot button and setting button
             */
            enableShootButton();
        }
    };

    /*
     * Processing when ICE status changes
     */
    peer.oniceconnectionstatechange = function() {
        console.log('ICE connection Status has changed to ' + peer.iceConnectionState);
        switch (peer.iceConnectionState) {
        case 'connected':
            break;
        case 'closed':
            break;
        case 'failed':
        case 'dissconnected':
        /*
            if (peerConnection) {
                hangUp();
            }
        */
            break;
        }
    };

    return peer;
}

/**
 * Start preview
 */
function startPreview(previewSize) {
    if ((previewSize === VIDEO_SIZE_2K) || (previewSize === VIDEO_SIZE_4K)) {
        videSize = previewSize;
    } else if (previewSize !== VIDEO_SIZE_CURRENT) {
        videSize = VIDEO_SIZE_2K;   // Default;
    }

    /*
     * Start live view
     */
    startLivePreview(videSize)
    .then(function() {
        return sleep(500);
    })
    .then(function() {
        /*
         * Get options
         */
        return getOptionsInternal();
    })
    .then(function(responseText) {
        /*
         * Set options to UI
         */
        let json = JSON.parse(responseText);
        let cameraParams = json.results.options;
        setUIOptions(cameraParams, true);
    });

    /*
     * Play video
     */
    if (peerConnection != null) {
        playVideoInternal(remoteVideo, peerConnection.getRemoteStreams()[0]);
    }
}

/**
 * Stop preview
 */
function stopPreview() {
    /*
     * Pause video
     */
    pauseVideoInternal(remoteVideo);

    /*
     * Stop live view
     */
    stopLivePreview();
}

/**
 * Executing still image shooting, and start shooting status check
 */
function shoot() {
    console.log('Still image shooting...');

    /*
     * Disable shoot button and setting button
     */
    disableShootButton();

    /*
     * Pause video
     */
    pauseVideoInternal(remoteVideo);

    /*
     * Stop live view
     */
    stopLivePreview()
    .then(function() {
        /*
         * Shoot
         */
        return takePictureInternal();
    })
    .then(function() {
        /*
         * Start check shooting status
         */
        checkShootingStatus();
    });
}

/**
 * Shooting status check
 */
function checkShootingStatus() {
    /*
     * Get shooting status
     */
    getShootingStatus()
    .then(function(responseText) {
        let json = JSON.parse(responseText);
        if (json.status !== 'idle') {
            /*
             * Re-start check shooting status
             */
            setTimeout(checkShootingStatus, 500);
        } else {
            shootComplete();
        }
    });
}

/**
 * Shooting completion processing
 */
function shootComplete() {
    console.log('Still image shooting complete');

    /*
     * Start live view
     */
    startLivePreview(videSize)
    .then(function() {
        return sleep(500);
    })
    .then(function() {
        if (peerConnection != null) {
            /*
             * Play video
             */
            playVideoInternal(remoteVideo, peerConnection.getRemoteStreams()[0]);
        }
    });
}

/**
 * Make offer SDP (Currently unused)
 */
function makeOffer() {
    peerConnection = prepareNewConnection();
    peerConnection.createOffer()
    .then(function (sessionDescription) {
        console.log('createOffer() succsess in promise');
        return peerConnection.setLocalDescription(sessionDescription);
    }).then(function() {
        console.log('setLocalDescription() succsess in promise');

        // For Trickle ICE, send the initial SDP to the other peer.
        // In the case of vanilla ICE, SDP is not sent yet.
        // sendSdp(peerConnection.localDescription);
    }).catch(function(err) {
        console.error(err);
    });
}

/**
 * Store SDP of the received offer
 */
function setOffer(sessionDescription) {
    if (peerConnection) {
        console.log('PeerConnection alreay exist!');
    }
    peerConnection = prepareNewConnection();
    peerConnection.setRemoteDescription(sessionDescription)
    .then(function() {
        console.log('setRemoteDescription(offer) succsess in promise');
        makeAnswer();
    }).catch(function(err) {
        console.error('setRemoteDescription(offer) ERROR: ', err);
    });
}

/**
 * Make answer SDP
 */
function makeAnswer() {
    console.log('sending Answer. Creating remote session description...' );
     if (! peerConnection) {
        console.error('PeerConnection NOT exist!');
        return;
    }

    peerConnection.createAnswer()
    .then(function (sessionDescription) {
        console.log('createAnswer() succsess in promise');
        return peerConnection.setLocalDescription(sessionDescription);
    }).then(function() {
        console.log('setLocalDescription() succsess in promise');

        // For Trickle ICE, send the initial SDP to the other peer.
        // In the case of vanilla ICE, SDP is not sent yet.
        // sendSdp(peerConnection.localDescription);
    }).catch(function(err) {
        console.error(err);
    });
}

/**
 * Start PeerConnection (Currently unused)
 */
function connect() {
    if (! peerConnection) {
        console.log('make Offer');
        makeOffer();
    } else {
        console.warn('peer already exist.');
    }
}

/**
 * Close PeerConnection
 */
function hangUp() {
    console.log('hangUp()');
    if (peerConnection) {
        console.log('Hang up.');
        peerConnection.close();
        peerConnection = null;
        pauseVideoInternal(remoteVideo);
    } else {
        console.warn('peer NOT exist.');
    }
}
