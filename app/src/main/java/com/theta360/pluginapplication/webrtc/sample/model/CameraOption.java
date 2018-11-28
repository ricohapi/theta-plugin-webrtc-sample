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

package com.theta360.pluginapplication.webrtc.sample.model;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Camera option class
 */
public class CameraOption {
    private static final String TAG = "CameraOption";
    private Camera.Parameters mParameters = null;
    private Filter mStillImageCaptureFilter = Filter.OFF;

    /*
     * Camera.Parameters Key&Value
     */
    private static final String KEY_RIC_EXPOSURE_MODE = "RIC_EXPOSURE_MODE";
    private static final String KEY_RIC_WB_MODE = "RIC_WB_MODE";
    private static final String KEY_RIC_WB_TEMPERATURE = "RIC_WB_TEMPERATURE";
    private static final String KEY_RIC_MANUAL_EXPOSURE_ISO_FRONT = "RIC_MANUAL_EXPOSURE_ISO_FRONT";
    private static final String KEY_RIC_MANUAL_EXPOSURE_ISO_REAR = "RIC_MANUAL_EXPOSURE_ISO_REAR";
    private static final String KEY_RIC_MANUAL_EXPOSURE_TIME_FRONT = "RIC_MANUAL_EXPOSURE_TIME_FRONT";
    private static final String KEY_RIC_MANUAL_EXPOSURE_TIME_REAR = "RIC_MANUAL_EXPOSURE_TIME_REAR";
    private static final String KEY_EXPOSURE_COMPENSATION_STEP = "exposure-compensation-step";
    private static final String EXPOSURE_COMPENSATION_STEP = "0.333333333";
    private static final Number[] EXPOSURE_COMPENSATION_SUPPORT = { -2d, -1.7, -1.3, -1d, -0.7, -0.3, 0d, 0.3, 0.7, 1d, 1.3, 1.7, 2d };

    /*
     * Default value
     */
    private static final ExposureMode EXPOSURE_MODE_DEFAULT = ExposureMode.NORMAL_PROGRAM;
    private static final Iso MANUAL_EXPOSURE_ISO_DEFAULT = Iso.ISO_100;
    private static final ShutterSpeed MANUAL_EXPOSURE_TIME_DEFAULT = ShutterSpeed.SHUTTER_SPEED_0_01666666;
    private static final WhiteBalance WB_MODE_DEFAULT = WhiteBalance.AUTO;
    private static final Number COLOR_TEMPERATURE_DEFAULT = 5000;
    private static final Filter FILTER_DEFAULT = Filter.OFF;

    /*
     * Each option value details
     */
    // Exposure mode
    public enum ExposureMode {
        MANUAL(1, "RicManualExposure"),
        NORMAL_PROGRAM(2, "RicAutoExposureP"),
        SHUTTER_PRIORITY(4, "RicAutoExposureT"),
        ISO_PRIORITY(9, "RicAutoExposureS");

        private final Number mExposureMode;
        private final String mExposureModeValue;

        ExposureMode(final Number exposureMode, final String exposureModeValue) {
            mExposureMode = exposureMode;
            mExposureModeValue = exposureModeValue;
        }

        public Number getMode() {
            return mExposureMode;
        }

        public String getValue() {
            return mExposureModeValue;
        }

        public static ExposureMode getValue(final Number exposureMode) {
            for (ExposureMode eMode : ExposureMode.values()) {
                if (eMode.getMode() == exposureMode) {
                    return eMode;
                }
            }
            return EXPOSURE_MODE_DEFAULT;
        }

        public static ExposureMode getMode(final String exposureModeValue) {
            for (ExposureMode eMode : ExposureMode.values()) {
                if (eMode.getValue().equals(exposureModeValue)) {
                    return eMode;
                }
            }
            return EXPOSURE_MODE_DEFAULT;
        }
    };

    // ISO
    public enum Iso {
        ISO_AUTO(0, -1),
        ISO_64(64, 1),
        ISO_80(80, 2),
        ISO_100(100, 3),
        ISO_125(125, 4),
        ISO_160(160, 5),
        ISO_200(200, 6),
        ISO_250(250, 7),
        ISO_320(320, 8),
        ISO_400(400, 9),
        ISO_500(500, 10),
        ISO_640(640, 11),
        ISO_800(800, 12),
        ISO_1000(1000, 13),
        ISO_1250(1250, 14),
        ISO_1600(1600, 15),
        ISO_2000(2000, 16),
        ISO_2500(2500, 17),
        ISO_3200(3200, 18);

        private final Number mIso;
        private final int mIsoValue;

        Iso(final int iso, final int isoValue) {
            mIso = iso;
            mIsoValue = isoValue;
        }

        public Number getIso() {
            return mIso;
        }

        public int getValue() {
            return mIsoValue;
        }

        public static Iso getIso(int isoValue) {
            for (Iso eIso : Iso.values()) {
                if (eIso.getValue() == isoValue) {
                    return eIso;
                }
            }
            return MANUAL_EXPOSURE_ISO_DEFAULT;
        }

        public static Iso getValue(Number iso) {
            for (Iso eIso : Iso.values()) {
                if (eIso.getIso().equals(iso)) {
                    return eIso;
                }
            }
            return MANUAL_EXPOSURE_ISO_DEFAULT;
        }
    }

    // Shutter speed
    public enum ShutterSpeed {
        SHUTTER_SPEED_AUTO(0, -1),
        SHUTTER_SPEED_0_00004(0.00004, 0),
        SHUTTER_SPEED_0_00005(0.00005, 1),
        SHUTTER_SPEED_0_0000625(0.0000625, 2),
        SHUTTER_SPEED_0_00008(0.00008, 3),
        SHUTTER_SPEED_0_0001(0.0001, 4),
        SHUTTER_SPEED_0_000125(0.000125, 5),
        SHUTTER_SPEED_0_00015625(0.00015625, 6),
        SHUTTER_SPEED_0_0002(0.0002, 7),
        SHUTTER_SPEED_0_00025(0.00025, 8),
        SHUTTER_SPEED_0_0003125(0.0003125, 9),
        SHUTTER_SPEED_0_0004(0.0004, 10),
        SHUTTER_SPEED_0_0005(0.0005, 11),
        SHUTTER_SPEED_0_000625(0.000625, 12),
        SHUTTER_SPEED_0_0008(0.0008, 13),
        SHUTTER_SPEED_0_001(0.001, 14),
        SHUTTER_SPEED_0_00125(0.00125, 15),
        SHUTTER_SPEED_0_0015625(0.0015625, 16),
        SHUTTER_SPEED_0_002(0.002, 17),
        SHUTTER_SPEED_0_0025(0.0025, 18),
        SHUTTER_SPEED_0_003125(0.003125, 19),
        SHUTTER_SPEED_0_004(0.004, 20),
        SHUTTER_SPEED_0_005(0.005, 21),
        SHUTTER_SPEED_0_00625(0.00625, 22),
        SHUTTER_SPEED_0_008(0.008, 23),
        SHUTTER_SPEED_0_01(0.01, 24),
        SHUTTER_SPEED_0_0125(0.0125, 25),
        SHUTTER_SPEED_0_01666666(0.01666666, 26),
        SHUTTER_SPEED_0_02(0.02, 27),
        SHUTTER_SPEED_0_025(0.025, 28),
        SHUTTER_SPEED_0_03333333(0.03333333, 29),
        SHUTTER_SPEED_0_04(0.04, 30),
        SHUTTER_SPEED_0_05(0.05, 31),
        SHUTTER_SPEED_0_06666666(0.06666666, 32),
        SHUTTER_SPEED_0_07692307(0.07692307, 33),
        SHUTTER_SPEED_0_1(0.1, 34),
        SHUTTER_SPEED_0_125(0.125, 35),
        SHUTTER_SPEED_0_16666666(0.16666666, 36),
        SHUTTER_SPEED_0_2(0.2, 37),
        SHUTTER_SPEED_0_25(0.25, 38),
        SHUTTER_SPEED_0_33333333(0.33333333, 39),
        SHUTTER_SPEED_0_4(0.4, 40),
        SHUTTER_SPEED_0_5(0.5, 41),
        SHUTTER_SPEED_0_625(0.625, 42),
        SHUTTER_SPEED_0_76923076(0.76923076, 43),
        SHUTTER_SPEED_1(1d, 44),
        SHUTTER_SPEED_1_3(1.3, 45),
        SHUTTER_SPEED_1_6(1.6, 46),
        SHUTTER_SPEED_2(2d, 47),
        SHUTTER_SPEED_2_5(2.5, 48),
        SHUTTER_SPEED_3_2(3.2, 49),
        SHUTTER_SPEED_4(4d, 50),
        SHUTTER_SPEED_5(5d, 51),
        SHUTTER_SPEED_6(6d, 52),
        SHUTTER_SPEED_8(8d, 53),
        SHUTTER_SPEED_10(10d, 54),
        SHUTTER_SPEED_13(13d, 55),
        SHUTTER_SPEED_15(15d, 56),
        SHUTTER_SPEED_20(20d, 57),
        SHUTTER_SPEED_25(25d, 58),
        SHUTTER_SPEED_30(30d, 59),
        SHUTTER_SPEED_60(60d, 62);

        private final Number mShutterSpeed;
        private final int mShutterSpeedValue;

        ShutterSpeed(final Number shutterSpeed, final int shutterSpeedValue) {
            mShutterSpeed = shutterSpeed;
            mShutterSpeedValue = shutterSpeedValue;
        }

        public Number getShutterSpeed() {
            return mShutterSpeed;
        }

        public int getValue() {
            return mShutterSpeedValue;
        }

        public static ShutterSpeed getShutterSpeed(int shutterSpeedValue) {
            for (ShutterSpeed eSpeed : ShutterSpeed.values()) {
                if (eSpeed.getValue() == shutterSpeedValue) {
                    return eSpeed;
                }
            }
            return MANUAL_EXPOSURE_TIME_DEFAULT;
        }

        public static ShutterSpeed getValue(Number shutterSpeed) {
            for (ShutterSpeed eSpeed : ShutterSpeed.values()) {
                if (eSpeed.getShutterSpeed().equals(shutterSpeed)) {
                    return eSpeed;
                }
            }
            return MANUAL_EXPOSURE_TIME_DEFAULT;
        }
    }

    // White balance mode
    public enum WhiteBalance {
        AUTO("auto", "RicWbAuto"),
        DAYLIGHT("daylight", "RicWbPrefixDaylight"),
        SHADE("shade", "RicWbPrefixShade"),
        CLOUDY_DAYLIGHT("cloudy-daylight", "RicWbPrefixCloudyDaylight"),
        INCANDESCENT("incandescent", "RicWbPrefixIncandescent"),
        WARM_WHITE_FLUORESCENT("_warmWhiteFluorescent", "RicWbPrefixFluorescentWW"),
        DAY_LIGHT_FLUORESCENT("_dayLightFluorescent", "RicWbPrefixFluorescentD"),
        DAY_WHITE_FLUORESCENT("_dayWhiteFluorescent", "RicWbPrefixFluorescentN"),
        FLUORESCENT("fluorescent", "RicWbPrefixFluorescentW"),
        BULB_FLUORESCENT("_bulbFluorescent", "RicWbPrefixFluorescentL"),
        COLOR_TEMPERATURE("_colorTemperature", "RicWbPrefixTemperature");

        private final String mWhiteBalance;
        private final String mWhiteBalanceValue;

        WhiteBalance(final String whiteBalance, final String whiteBalanceValue) {
            mWhiteBalance = whiteBalance;
            mWhiteBalanceValue = whiteBalanceValue;
        }

        public String getMode() {
            return mWhiteBalance;
        }

        public String getValue() {
            return mWhiteBalanceValue;
        }

        public static WhiteBalance getValue(final String whiteBalance) {
            for (WhiteBalance wb : WhiteBalance.values()) {
                if (wb.getMode().equals(whiteBalance)) {
                    return wb;
                }
            }
            return WB_MODE_DEFAULT;
        }

        public static WhiteBalance getMode(final String whiteBalanceVal) {
            for (WhiteBalance wb : WhiteBalance.values()) {
                if (wb.getValue().equals(whiteBalanceVal)) {
                    return wb;
                }
            }
            return WB_MODE_DEFAULT;
        }
    };

    // Filtter
    public enum Filter {
        OFF("off", "RicStillCaptureStd", null),
        NOISE_REDUCTION("Noise Reduction", "RicStillCaptureMultiRawNR", ExposureMode.NORMAL_PROGRAM),
        HDR("hdr", "RicStillCaptureMultiYuvHdr", ExposureMode.NORMAL_PROGRAM),
        DR_COMP("DR Comp", "RicStillCaptureWDR", ExposureMode.NORMAL_PROGRAM);

        private String mFilter;
        private String mFiltreValue;
        private ExposureMode mExposureMode;

        Filter(final String filter, final String filterValue, final ExposureMode exposureMode) {
            mFilter = filter;
            mFiltreValue = filterValue;
            mExposureMode = exposureMode;
        }

        private String getFilter() {
            return mFilter;
        }

        private String getValue() {
            return mFiltreValue;
        }

        private ExposureMode getExposureMode() {
            return mExposureMode;
        }

        private static Filter getValue(final String filter) {
            for (Filter eFilter : Filter.values()) {
                if (eFilter.getFilter().equals(filter)) {
                    return eFilter;
                }
            }
            return FILTER_DEFAULT;
        }

        private static Filter getFilter(final String filterValue) {
            for (Filter eFilter : Filter.values()) {
                if (eFilter.getValue().equals(filterValue)) {
                    return eFilter;
                }
            }
            return FILTER_DEFAULT;
        }
    }

    // Option name
    public enum OptionName {
        EXPOSURE_PROGRAM("exposureProgram"),
        ISO("iso"),
        SHUTTER_SPEED("shutterSpeed"),
        WHITE_BALANCE("whiteBalance"),
        COLOR_TEMPERATURE("_colorTemperature"),
        EXPOSURE_COMPENSATION("exposureCompensation"),
        FILTER("_filter");

        private String mName;

        OptionName(String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }

        public static OptionName getValue(String name) {
            for (OptionName optionName : OptionName.values()) {
                if (optionName.toString().equals(name)) {
                    return optionName;
                }
            }
            return null;
        }
    }

    // Setting name
    public enum SettingName {
        SHUTTER_VOLUME("_shutterVolume");

        private String mName;

        SettingName(String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }

        public static SettingName getValue(String name) {
            for (SettingName settingName : SettingName.values()) {
                if (settingName.toString().equals(name)) {
                    return settingName;
                }
            }
            return null;
        }
    }

    /*
     * Method
     */

    /**
     * Get camera parameters direct from camera using Camera API .
     *
     * @param camera Camera object, connected, locked and ready for use.
     * @param direct true: Get directly from the camera, false: Returns the held value, If it is not first call.
     */
    public void getCameraParameters(Camera camera, boolean direct) {
        if (camera != null) {
            if ((mParameters == null) || direct) {
                mParameters = camera.getParameters();
            }
            mParameters.set(KEY_EXPOSURE_COMPENSATION_STEP, EXPOSURE_COMPENSATION_STEP);
            if (! direct) {
                camera.setParameters(mParameters);
            }
        }
    }

    /**
     * Set camera parameters direct to camera using Camera API .
     *
     * @param camera Camera object, connected, locked and ready for use.
     */
    public void setCameraParameters(Camera camera) {
        if (camera != null) {
            camera.setParameters(mParameters);
        }
    }

    /**
     * Return 'All' options of JSON object format based on 'RICOH THETA API v2.1'
     * ('All' means an option list enumerated by 'OptionName'.)
     *
     * @return Options of JSON object format based on 'RICOH THETA API v2.1'
     */
    public JSONObject getAllOptions() {
        JSONObject webApiOptions = null;
        ArrayList<String> paramNameList = new ArrayList<String>();

        for (OptionName optionName : OptionName.values()) {
            paramNameList.add(optionName.toString());
        }
        webApiOptions = getOptions(paramNameList);
        return webApiOptions;
    }

    /**
     * Return requested options of JSON object format based on 'RICOH THETA API v2.1'
     *
     * @param optionNameList List of option names to request
     * @return Options of JSON object format based on 'RICOH THETA API v2.1'
     */
    public JSONObject getOptions(ArrayList<String> optionNameList) {
        JSONObject webApiOptions = new JSONObject();

        for (String name : optionNameList) {
            OptionName optionName = OptionName.getValue(name);
            try {
                switch (optionName) {
                case EXPOSURE_PROGRAM:
                    ExposureMode expMode = getExposureProgram();
                    webApiOptions.put(name, expMode.getMode().intValue());
                    break;
                case ISO:
                    Iso iso = getIso();
                    webApiOptions.put(name, iso.getIso().intValue());
                    break;
                case SHUTTER_SPEED:
                    ShutterSpeed shutterSpeed = getShutterSpeed();
                    webApiOptions.put(name, shutterSpeed.getShutterSpeed());
                    break;
                case WHITE_BALANCE:
                    WhiteBalance whiteBalance = getWhiteBalance();
                    webApiOptions.put(name, whiteBalance.getMode());
                    break;
                case COLOR_TEMPERATURE:
                    webApiOptions.put(name, getColorTemperature());
                    break;
                case EXPOSURE_COMPENSATION:
                    webApiOptions.put(name, getExposureCompensation());
                    break;
                case FILTER:
                    webApiOptions.put(name, getFilter());
                    break;
                default:
                    // ignore
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return webApiOptions;
    }

    /**
     * Set options specified in the JSON object format based on the 'RICOH THETA API v2.1' format.
     *
     * @param webApioptions Options of JSON object format based on 'RICOH THETA API v2.1'
     */
    public void setOptions(JSONObject webApioptions) {
        Iterator<String> keys = webApioptions.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            OptionName optionName = OptionName.getValue(key);
            switch (optionName) {
            case EXPOSURE_PROGRAM:
                int exposureProgram = Integer.parseInt(webApioptions.optString(key));
                modifyExposureProgram(ExposureMode.getValue(exposureProgram));
                break;
            case ISO:
                Number iso = Integer.parseInt(webApioptions.optString(key));
                modifyIso(Iso.getValue(iso));
                break;
            case SHUTTER_SPEED:
                Number shutterSpeed = Double.parseDouble(webApioptions.optString(key));
                modifyShutterSpeed(ShutterSpeed.getValue(shutterSpeed));
                break;
            case WHITE_BALANCE:
                String whiteBalanceMode = webApioptions.optString(key);
                modifyWhiteBalance(WhiteBalance.getValue(whiteBalanceMode));
                break;
            case COLOR_TEMPERATURE:
                Number colorTemperature = Integer.parseInt(webApioptions.optString(key));
                modifyColorTemperature(colorTemperature);
                break;
            case EXPOSURE_COMPENSATION:
                Number exposureCompensation = Double.parseDouble(webApioptions.optString(key));
                modifyExposureCompensation(exposureCompensation);
                break;
            case FILTER:
                String filter = webApioptions.optString(key);
                modifyFilter(filter);
                break;
            default:
                // ignore
                break;
            }
        }
    }

    /*
     * Get exposure mode value
     */
    private ExposureMode getExposureProgram() {
        String exposureProgram = mParameters.get(KEY_RIC_EXPOSURE_MODE);
        if (exposureProgram == null) {
            exposureProgram = EXPOSURE_MODE_DEFAULT.getValue();
            mParameters.set(KEY_RIC_EXPOSURE_MODE, exposureProgram);
        }
        return ExposureMode.getMode(exposureProgram);
    }

    /*
     * Modify exposure mode value
     */
    private void modifyExposureProgram(ExposureMode exposureProgram) {
        mParameters.set(KEY_RIC_EXPOSURE_MODE, exposureProgram.getValue());
    }

    /*
     * Get ISO value
     */
    private Iso getIso() {
        String iso = mParameters.get(KEY_RIC_MANUAL_EXPOSURE_ISO_FRONT);
        if (iso == null) {
            iso = Integer.toString(MANUAL_EXPOSURE_ISO_DEFAULT.getValue());
            mParameters.set(KEY_RIC_MANUAL_EXPOSURE_ISO_FRONT, iso);
            mParameters.set(KEY_RIC_MANUAL_EXPOSURE_ISO_REAR, iso);
        }
        return Iso.getIso(Integer.parseInt(iso));
    }

    /*
     * Modify ISO value
     */
    private void modifyIso(Iso iso) {
        mParameters.set(KEY_RIC_MANUAL_EXPOSURE_ISO_FRONT, Integer.toString(iso.getValue()));
        mParameters.set(KEY_RIC_MANUAL_EXPOSURE_ISO_REAR, Integer.toString(iso.getValue()));
    }

    /*
     * Get shutter speed value
     */
    private ShutterSpeed getShutterSpeed() {
        String shutterSpeed = mParameters.get(KEY_RIC_MANUAL_EXPOSURE_TIME_FRONT);
        if (shutterSpeed == null) {
            shutterSpeed = Integer.toString(MANUAL_EXPOSURE_TIME_DEFAULT.getValue());
            mParameters.set(KEY_RIC_MANUAL_EXPOSURE_TIME_FRONT, shutterSpeed);
            mParameters.set(KEY_RIC_MANUAL_EXPOSURE_TIME_REAR, shutterSpeed);
        }
        return ShutterSpeed.getShutterSpeed(Integer.parseInt(shutterSpeed));
    }

    /*
     * Modify shutter speed value
     */
    private void modifyShutterSpeed(ShutterSpeed shutterSpeed) {
        mParameters.set(KEY_RIC_MANUAL_EXPOSURE_TIME_FRONT, Integer.toString(shutterSpeed.getValue()));
        mParameters.set(KEY_RIC_MANUAL_EXPOSURE_TIME_REAR, Integer.toString(shutterSpeed.getValue()));
    }

    /*
     * Get exposure compensation value
     */
    private Number getExposureCompensation() {
        int evZeroIdx = getExposureCompensationZeroIdx();
        Number exposureCompensation = 0d;
        if (evZeroIdx != -1) {
            int exposureCompensationIdx = mParameters.getExposureCompensation() + evZeroIdx;
            exposureCompensation = EXPOSURE_COMPENSATION_SUPPORT[exposureCompensationIdx];
        }
        return exposureCompensation;
    }

    /*
     * Modify exposure compensation value
     */
    private void modifyExposureCompensation(Number value) {
        int evZeroIdx = getExposureCompensationZeroIdx();
        if (evZeroIdx != -1) {
            for (int i = 0; i < EXPOSURE_COMPENSATION_SUPPORT.length; i++) {
                if (EXPOSURE_COMPENSATION_SUPPORT[i].equals(value)) {
                    mParameters.set(KEY_EXPOSURE_COMPENSATION_STEP, EXPOSURE_COMPENSATION_STEP);
                    mParameters.setExposureCompensation(i - evZeroIdx);
                    break;
                }
            }
        }
    }

    private int getExposureCompensationZeroIdx() {
        int idx = -1;
        for (int i = 0; i < EXPOSURE_COMPENSATION_SUPPORT.length; i++) {
            if (EXPOSURE_COMPENSATION_SUPPORT[i].equals(0d)) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    /*
     * Get white balance mode value
     */
    private WhiteBalance getWhiteBalance() {
        String whiteBalance = mParameters.get(KEY_RIC_WB_MODE);
        if (whiteBalance == null) {
            whiteBalance = WhiteBalance.AUTO.getValue();
            mParameters.set(KEY_RIC_WB_MODE, whiteBalance);
        }
        return WhiteBalance.getMode(whiteBalance);
    }

    /*
     * Modify white balance mode value
     */
    private void modifyWhiteBalance(WhiteBalance whiteBalanceMode) {
        mParameters.set(KEY_RIC_WB_MODE, whiteBalanceMode.getValue());
    }

    /*
     * Get color temperature value
     */
    private Number getColorTemperature() {
        String colorTemperature = mParameters.get(KEY_RIC_WB_TEMPERATURE);
        if (colorTemperature == null) {
            colorTemperature = COLOR_TEMPERATURE_DEFAULT.toString();
            mParameters.set(KEY_RIC_WB_TEMPERATURE, colorTemperature);
        }
        return Integer.parseInt(colorTemperature);
    }

    /*
     * Modify color temperature value
     */
    private void modifyColorTemperature(Number value) {
        mParameters.set(KEY_RIC_WB_TEMPERATURE, value.toString());
    }

    /*
     * Get filtter value
     */
    private String getFilter() {
        return mStillImageCaptureFilter.getFilter();
    }

    /*
     * Modify filtter value
     */
    private void modifyFilter(String filter) {
        mStillImageCaptureFilter = Filter.getValue(filter);
    }
}