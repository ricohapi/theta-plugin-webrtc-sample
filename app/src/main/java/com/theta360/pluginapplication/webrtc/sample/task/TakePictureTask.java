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

package com.theta360.pluginapplication.webrtc.sample.task;

import android.os.AsyncTask;

import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.theta360.pluginapplication.webrtc.sample.network.HttpConnector;
import com.theta360.pluginapplication.webrtc.sample.network.HttpEventListener;
import com.theta360.pluginapplication.webrtc.sample.network.model.requests.CommandsRequest;
import com.theta360.pluginapplication.webrtc.sample.network.model.values.Errors;

import org.json.JSONObject;

import timber.log.Timber;

/**
 * Take picture task class
 */
public class TakePictureTask extends AsyncTask<Void, Void, HttpConnector.ShootResult> {
    private static final String TAG = "TakePictureTask";
    private Callback mCallback = null;
    private AsyncHttpServerResponse mResponse;
    private CommandsRequest mCommandRequest;

    /**
     * Interface of notification callback function
     */
    public interface Callback {
        void onPreExecute();
        void onSendCommand(AsyncHttpServerResponse response, CommandsRequest commandsRequest, Errors errors);
        void onTakePicture(String fileUrl);
        void onCompleted();
        void onError(String errorMessage);
    }

    /**
     * Constructor of take picture task class
     *
     * @param callback Implementation object of callback functions
     * @param response HTTP response object
     * @param commandsRequest Command request object
     */
    public TakePictureTask(Callback callback, AsyncHttpServerResponse response, CommandsRequest commandsRequest) {
        this.mCallback = callback;
        this.mResponse = response;
        this.mCommandRequest = commandsRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPreExecute() {
        mCallback.onPreExecute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpConnector.ShootResult doInBackground(Void... params) {
        CaptureListener postviewListener = new CaptureListener();
        HttpConnector camera = new HttpConnector("127.0.0.1:8080");

        String error = camera.setOptions(this.mCommandRequest.getCommandsParameters().optJSONObject("parameters"));

        return camera.takePicture(postviewListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(HttpConnector.ShootResult result) {
        Errors errors = null;
        if (result == HttpConnector.ShootResult.FAIL_CAMERA_DISCONNECTED) {
            Timber.d("takePicture:FAIL_CAMERA_DISCONNECTED");
            errors = Errors.UNEXPECTED;
        } else if (result == HttpConnector.ShootResult.FAIL_STORE_FULL) {
            Timber.d("takePicture:FAIL_STORE_FULL");
            errors = Errors.NO_FREE_SPACE;
        } else if (result == HttpConnector.ShootResult.FAIL_DEVICE_BUSY) {
            Timber.d("takePicture:FAIL_DEVICE_BUSY");
            errors = Errors.SERVICE_UNAVAILABLE;
        } else if (result == HttpConnector.ShootResult.SUCCESS) {
            Timber.d("takePicture:SUCCESS");
        }
        mCallback.onSendCommand(this.mResponse, this.mCommandRequest, errors);
    }

    /**
     * Implimentation of HTTP communication event listener
     */
    private class CaptureListener implements HttpEventListener {
        private String latestCapturedFileId;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCheckStatus(boolean newStatus) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onObjectChanged(String latestCapturedFileId) {
            this.latestCapturedFileId = latestCapturedFileId;
            if (mCallback != null) {
                mCallback.onTakePicture(latestCapturedFileId);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCompleted() {
            if (mCallback != null) {
                mCallback.onCompleted();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onError(String errorMessage) {
            if (mCallback != null) {
                mCallback.onError(errorMessage);
            }
        }
    }
}
