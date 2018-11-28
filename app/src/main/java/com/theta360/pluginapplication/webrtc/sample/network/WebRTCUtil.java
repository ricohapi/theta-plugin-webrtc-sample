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

import org.webrtc.MediaConstraints;

/**
 * Utility class for WebRTC
 */
public class WebRTCUtil {
    /**
     * Return MediaConstraints object to create peer connection.
     * (Currently unused)
     *
     * @return MediaConstraints object
     */
    static final MediaConstraints peerConnectionConstraints() {
        return audioVideoConstraints();
    }

    /**
     * Return MediaConstraints object to create offer SDP
     *
     * @return MediaConstraints object
     */
    static final MediaConstraints offerConnectionConstraints() {
        return audioVideoConstraints();
    }

    /**
     * Return MediaConstraints object to create answer SDP
     *
     * @return MediaConstraints object
     */
    static final MediaConstraints answerConnectionConstraints() {
        return audioVideoConstraints();
    }

    /**
     * Create MediaConstraints object
     *
     * @return MediaConstraints object
     */
    static final MediaConstraints mediaStreamConstraints() {
        MediaConstraints constraints = new MediaConstraints();

        return constraints;
    }

    /*
     * Create and setup MediaConstraints object
     */
    private static final MediaConstraints audioVideoConstraints() {
        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        return constraints;
    }

}
