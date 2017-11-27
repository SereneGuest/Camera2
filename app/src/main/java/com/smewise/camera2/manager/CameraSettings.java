package com.smewise.camera2.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Size;

import com.smewise.camera2.Config;
import com.smewise.camera2.R;
import com.smewise.camera2.ui.CameraMenu;
import com.smewise.camera2.utils.CameraUtil;


/**
 * Created by wenzhe on 12/16/16.
 */

public class CameraSettings {
    private final String TAG = Config.TAG_PREFIX + "CameraSetting";

    public static final String KEY_PICTURE_SIZE = "pref_picture_size";
    public static final String KEY_PREVIEW_SIZE = "pref_preview_size";
    public static final String KEY_CAMERA_ID = "pref_camera_id";
    public static final String KEY_MAIN_PICTURE_SIZE = "pref_main_picture_size";
    public static final String KEY_MAIN_PREVIEW_SIZE = "pref_main_preview_size";
    public static final String KEY_MAIN_CAMERA_ID = "pref_main_camera_id";
    public static final String KEY_AUX_PICTURE_SIZE = "pref_aux_picture_size";
    public static final String KEY_AUX_PREVIEW_SIZE = "pref_aux_preview_size";
    public static final String KEY_AUX_CAMERA_ID = "pref_aux_camera_id";
    public static final String KEY_PICTURE_FORMAT = "pref_picture_format";
    public static final String KEY_MAIN_PICTURE_FORMAT = "pref_main_picture_format";
    public static final String KEY_AUX_PICTURE_FORMAT = "pref_aux_picture_format";
    public static final String KEY_RESTART_PREVIEW = "pref_restart_preview";

    private SharedPreferences mSharedPreference;

    public CameraSettings(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.camera_setting, false);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getCameraId(String key) {
        String defaultValue = KEY_AUX_CAMERA_ID.equals(key) ? Config.AUX_ID : Config.MAIN_ID;
        return mSharedPreference.getString(key, defaultValue);
    }

    public String getCameraId(String key, String defaultValue) {
        return mSharedPreference.getString(key, defaultValue);
    }

    public int getPicFormat(String key) {
        String format = mSharedPreference.getString(key, String.valueOf(Config.IMAGE_FORMAT));
        return Integer.parseInt(format);
    }

    private int getPicFormatFormKey(String picSize) {
        String formatKey;
        switch (picSize) {
            case KEY_PICTURE_SIZE:
                formatKey = KEY_PICTURE_FORMAT;
                break;
            case KEY_MAIN_PICTURE_SIZE:
                formatKey = KEY_MAIN_PICTURE_FORMAT;
                break;
            case KEY_AUX_PICTURE_SIZE:
                formatKey = KEY_AUX_PICTURE_FORMAT;
                break;
            default:
                formatKey = KEY_PICTURE_FORMAT;
                break;
        }
        String format = mSharedPreference.getString(formatKey, String.valueOf(Config.IMAGE_FORMAT));
        return Integer.parseInt(format);
    }

    public boolean needStartPreview() {
        return mSharedPreference.getBoolean(KEY_RESTART_PREVIEW, true);
    }

    public Size getPictureSize(String key, StreamConfigurationMap map) {
        String picStr = mSharedPreference.getString(key, Config.NULL_VALUE);
        if (Config.NULL_VALUE.equals(picStr)) {
            // preference not set, use default value
            return CameraUtil.getPictureSize(map, Config.DEFAULT_RATIO, getPicFormatFormKey(key));
        } else {
            String[] size = picStr.split(CameraUtil.SPLIT_TAG);
            return new Size(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        }
    }

    public Size getPreviewSize(String key, StreamConfigurationMap map) {
        String preStr = mSharedPreference.getString(key, Config.NULL_VALUE);
        if (Config.NULL_VALUE.equals(preStr)) {
            // preference not set, use default value
            return CameraUtil.getPreviewSize(map, Config.DEFAULT_RATIO);
        } else {
            String[] size = preStr.split(CameraUtil.SPLIT_TAG);
            return new Size(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        }
    }

    public void dumpSupportInfo(CameraCharacteristics characteristics) {
        //print hardware support info
        isHardwareSupported(characteristics);
        //print support output format
        StreamConfigurationMap map =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        for (int format : map.getOutputFormats()) {
            printFormatString(format);
        }
        int[] capa = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        for (int cap : capa) {
            Log.i(TAG, "Capabilities:" + cap);
        }
    }

    private int isHardwareSupported(CameraCharacteristics characteristics) {
        Integer deviceLevel = characteristics.get(CameraCharacteristics
                .INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == null) {
            Log.e(TAG, "can not get INFO_SUPPORTED_HARDWARE_LEVEL");
            return -1;
        }
        switch (deviceLevel) {
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                Log.i(TAG, "hardware supported level:LEVEL_FULL");
                break;
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                Log.i(TAG, "hardware supported level:LEVEL_LEGACY");
                break;
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                Log.i(TAG, "hardware supported level:LEVEL_3");
                break;
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                Log.i(TAG, "hardware supported level:LEVEL_LIMITED");
                break;
        }
        return deviceLevel;
    }

    private void printFormatString(int format) {
        switch (format) {
            case ImageFormat.RGB_565:
                Log.i(TAG, "support format: RGB_565");
                break;
            case ImageFormat.NV16:
                Log.i(TAG, "support format: NV16");
                break;
            case ImageFormat.YUY2:
                Log.i(TAG, "support format: YUY2");
                break;
            case ImageFormat.YV12:
                Log.i(TAG, "support format: YV12");
                break;
            case ImageFormat.JPEG:
                Log.i(TAG, "support format: JPEG");
                break;
            case ImageFormat.NV21:
                Log.i(TAG, "support format: NV21");
                break;
            case ImageFormat.YUV_420_888:
                Log.i(TAG, "support format: YUV_420_888");
                break;
            case ImageFormat.YUV_422_888:
                Log.i(TAG, "support format: YUV_422_888");
                break;
            case ImageFormat.YUV_444_888:
                Log.i(TAG, "support format: YUV_444_888");
                break;
            case ImageFormat.FLEX_RGB_888:
                Log.i(TAG, "support format: FLEX_RGB_888");
                break;
            case ImageFormat.FLEX_RGBA_8888:
                Log.i(TAG, "support format: FLEX_RGBA_8888");
                break;
            case ImageFormat.RAW_SENSOR:
                Log.i(TAG, "support format: RAW_SENSOR");
                break;
            case ImageFormat.RAW_PRIVATE:
                Log.i(TAG, "support format: RAW_PRIVATE");
                break;
            case ImageFormat.RAW10:
                Log.i(TAG, "support format: RAW10");
                break;
            case ImageFormat.RAW12:
                Log.i(TAG, "support format: RAW12");
                break;
            case ImageFormat.DEPTH16:
                Log.i(TAG, "support format: DEPTH16");
                break;
            case ImageFormat.DEPTH_POINT_CLOUD:
                Log.i(TAG, "support format: DEPTH_POINT_CLOUD");
                break;
            case ImageFormat.PRIVATE:
                Log.i(TAG, "support format: PRIVATE");
                break;
        }
    }
}
