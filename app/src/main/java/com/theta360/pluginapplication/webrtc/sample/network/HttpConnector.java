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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * HTTP connection to device
 */
public class HttpConnector {
    private static final String TAG = "HttpConnector";
    private static final long CHECK_STATUS_PERIOD_MS = 300;
    private String mIpAddress = null;

    private String mFingerPrint = null;
    private Timer mCheckStatusTimer = null;
    private HttpEventListener mHttpEventListener = null;

    /**
     * Constructor
     *
     * @param cameraIpAddress IP address of connection destination
     */
    public HttpConnector(String cameraIpAddress) {
        mIpAddress = cameraIpAddress;
    }

    /**
     * Acquire device information
     *
     * @return Device information
     */
    public DeviceInfo getDeviceInfo() {
        HttpURLConnection getConnection = createHttpConnection("GET", "/osc/info");
        String responseData;
        DeviceInfo deviceInfo = new DeviceInfo();
        InputStream is = null;

        try {
            // send HTTP GET
            // this protocol has no input.
            getConnection.connect();

            is = getConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);

            String model = output.getString("model");
            deviceInfo.setModel(model);

            String version = output.getString("firmwareVersion");
            deviceInfo.setDeviceVersion(version);

            String serialNumber = output.getString("serialNumber");
            deviceInfo.setSerialNumber(serialNumber);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return deviceInfo;
    }

    /**
     * Take photo<p> After shooting, the status is checked for each {@link
     * HttpConnector#CHECK_STATUS_PERIOD_MS} and the listener notifies you of the status.
     *
     * @param listener Post-shooting event listener
     * @return Shooting request results
     */
    public ShootResult takePicture(HttpEventListener listener) {
        ShootResult result = ShootResult.FAIL_DEVICE_BUSY;

        // set capture mode to image
        String errorMessage = setImageCaptureMode();
        if (errorMessage != null) {
            listener.onError(errorMessage);
            result = ShootResult.FAIL_DEVICE_BUSY;
            return result;
        }

        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/execute");
        JSONObject input = new JSONObject();
        String responseData;
        mHttpEventListener = listener;
        InputStream is = null;

        try {
            // send HTTP POST
            input.put("name", "camera.takePicture");

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            String status = output.getString("state");
            String commandId = output.getString("id");

            if (status.equals("inProgress")) {
                mCheckStatusTimer = new Timer(true);
                CapturedTimerTask capturedTimerTask = new CapturedTimerTask();
                capturedTimerTask.setCommandId(commandId);
                mCheckStatusTimer.scheduleAtFixedRate(capturedTimerTask, CHECK_STATUS_PERIOD_MS,
                        CHECK_STATUS_PERIOD_MS);
                result = ShootResult.SUCCESS;
            } else if (status.equals("done")) {
                JSONObject results = output.getJSONObject("results");
                String lastFileId = results.getString("fileUri");

                mHttpEventListener.onObjectChanged(lastFileId);
                mHttpEventListener.onCompleted();
                result = ShootResult.SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = ShootResult.FAIL_DEVICE_BUSY;
        } catch (JSONException e) {
            e.printStackTrace();
            result = ShootResult.FAIL_DEVICE_BUSY;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    /**
     * Check still image shooting status
     *
     * @param commandId Command ID for shooting still images
     * @return ID of saved file (null is returned if the file is not saved)
     */
    private String checkCaptureStatus(String commandId) {
        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/status");
        JSONObject input = new JSONObject();
        String responseData;
        String capturedFileId = null;
        InputStream is = null;

        try {
            // send HTTP POST
            input.put("id", commandId);

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            String status = output.getString("state");

            if (status.equals("done")) {
                JSONObject results = output.getJSONObject("results");
                capturedFileId = results.getString("fileUrl");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return capturedFileId;
    }

    /**
     * Set still image as shooting mode
     *
     * @return Error message (null is returned if successful)
     */
    private String setImageCaptureMode() {
        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/execute");
        JSONObject input = new JSONObject();
        String responseData;
        String errorMessage = null;
        InputStream is = null;

        try {
            // send HTTP POST
            input.put("name", "camera.setOptions");
            JSONObject parameters = new JSONObject();
            JSONObject options = new JSONObject();
            options.put("captureMode", "image");
            parameters.put("options", options);
            input.put("parameters", parameters);

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            String status = output.getString("state");

            if (status.equals("error")) {
                JSONObject errors = output.getJSONObject("error");
                errorMessage = errors.getString("message");
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage = e.toString();
            InputStream es = postConnection.getErrorStream();
            try {
                if (es != null) {
                    String errorData = InputStreamToString(es);
                    JSONObject output = new JSONObject(errorData);
                    JSONObject errors = output.getJSONObject("error");
                    errorMessage = errors.getString("message");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } finally {
                if (es != null) {
                    try {
                        es.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = e.toString();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return errorMessage;
    }

    /**
     * Set shooting options
     *
     * @param options Options
     * @return Error message (null is returned if successful)
     */
    public String setOptions(JSONObject options) {
        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/execute");
        JSONObject input = new JSONObject();
        String responseData;
        String errorMessage = null;
        InputStream is = null;

        try {
            // send HTTP POST
            input.put("name", "camera.setOptions");
            input.put("parameters", options);

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            String status = output.getString("state");

            if (status.equals("error")) {
                JSONObject errors = output.getJSONObject("error");
                errorMessage = errors.getString("message");
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage = e.toString();
            InputStream es = postConnection.getErrorStream();
            try {
                if (es != null) {
                    String errorData = InputStreamToString(es);
                    JSONObject output = new JSONObject(errorData);
                    JSONObject errors = output.getJSONObject("error");
                    errorMessage = errors.getString("message");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } finally {
                if (es != null) {
                    try {
                        es.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = e.toString();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return errorMessage;
    }

    /**
     * Set shooting options
     *
     * @param optionNames Option name list
     * @return Options
     */
    public String getOptions(JSONObject optionNames) {
        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/execute");
        JSONObject input = new JSONObject();
        String responseData = null;
        String errorMessage = null;
        InputStream is = null;

        try {
            // send HTTP POST
            input.put("name", "camera.getOptions");
            input.put("parameters", optionNames);

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            String status = output.getString("state");

            if (status.equals("error")) {
                JSONObject errors = output.getJSONObject("error");
                errorMessage = errors.getString("message");
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage = e.toString();
            InputStream es = postConnection.getErrorStream();
            try {
                if (es != null) {
                    String errorData = InputStreamToString(es);
                    JSONObject output = new JSONObject(errorData);
                    JSONObject errors = output.getJSONObject("error");
                    errorMessage = errors.getString("message");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } finally {
                if (es != null) {
                    try {
                        es.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return responseData;
    }

    /**
     * Get device status
     *
     * @return Camera status
     */
    public String getState() {
        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/state");
        String responseData = null;
        String errorMessage = null;
        InputStream is = null;

        try {
            // send HTTP POST
            postConnection.connect();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            JSONObject status = output.getJSONObject("state");

            if (status.equals("error")) {
                JSONObject errors = output.getJSONObject("error");
                errorMessage = errors.getString("message");
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage = e.toString();
            InputStream es = postConnection.getErrorStream();
            try {
                if (es != null) {
                    String errorData = InputStreamToString(es);
                    JSONObject output = new JSONObject(errorData);
                    JSONObject errors = output.getJSONObject("error");
                    errorMessage = errors.getString("message");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } finally {
                if (es != null) {
                    try {
                        es.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return responseData;
    }

    /**
     * Check for updates to device status
     *
     * @return true:Update available, false:No update available
     */
    private boolean isUpdate() {
        boolean update = false;
        InputStream is = null;

        if (mFingerPrint == null) {
            return update;
        }

        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/checkForUpdates");
        JSONObject input = new JSONObject();
        String responseData = null;

        try {
            // send HTTP POST
            input.put("stateFingerprint", mFingerPrint);

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            String currentFingerPrint = output.getString("stateFingerprint");
            if (!currentFingerPrint.equals(mFingerPrint)) {
                mFingerPrint = currentFingerPrint;
                update = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return update;
    }

    /**
     * Generate connection destination URL
     *
     * @param path Path
     * @return URL
     */
    private String createUrl(String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(mIpAddress);
        sb.append(path);

        return sb.toString();
    }

    /**
     * Generate HTTP connection
     *
     * @param method Method
     * @param path Path
     * @return HTTP Connection instance
     */
    private HttpURLConnection createHttpConnection(String method, String path) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(createUrl(path));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoInput(true);

            if (method.equals("POST")) {
                connection.setRequestMethod(method);
                connection.setDoOutput(true);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return connection;
    }

    /**
     * Convert input stream to string
     *
     * @param is InputStream
     * @return String
     * @throws IOException IO error
     */
    private String InputStreamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String lineData;
        while ((lineData = br.readLine()) != null) {
            sb.append(lineData);
        }
        br.close();
        return sb.toString();
    }

    public enum ShootResult {
        SUCCESS, FAIL_CAMERA_DISCONNECTED, FAIL_STORE_FULL, FAIL_DEVICE_BUSY
    }

    private class CapturedTimerTask extends TimerTask {
        private String mCommandId;

        public void setCommandId(String commandId) {
            mCommandId = commandId;
        }

        @Override
        public void run() {
            String capturedFileId = checkCaptureStatus(mCommandId);

            if (capturedFileId != null) {
                mHttpEventListener.onCheckStatus(true);
                mCheckStatusTimer.cancel();
                mHttpEventListener.onObjectChanged(capturedFileId);
                mHttpEventListener.onCompleted();
            } else {
                mHttpEventListener.onCheckStatus(false);
            }
        }
    }
}
