package com.smewise.camera2.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Size;
import android.view.WindowManager;

import com.smewise.camera2.Config;
import com.smewise.camera2.R;
import com.smewise.camera2.utils.CameraUtil;

import java.util.ArrayList;


/**
 * Created by wenzhe on 12/16/16.
 */

public class CameraSettings {
    private final String TAG = Config.getTag(CameraSettings.class);

    public static final String KEY_PICTURE_SIZE = "pref_picture_size";
    public static final String KEY_PREVIEW_SIZE = "pref_preview_size";
    public static final String KEY_CAMERA_ID = "pref_camera_id";
    public static final String KEY_MAIN_CAMERA_ID = "pref_main_camera_id";
    public static final String KEY_AUX_CAMERA_ID = "pref_aux_camera_id";
    public static final String KEY_PICTURE_FORMAT = "pref_picture_format";
    public static final String KEY_RESTART_PREVIEW = "pref_restart_preview";
    public static final String KEY_SWITCH_CAMERA = "pref_switch_camera";
    public static final String KEY_FLASH_MODE = "pref_flash_mode";
    //for flash mode
    public static final String FLASH_VALUE_ON = "on";
    public static final String FLASH_VALUE_OFF = "off";
    public static final String FLASH_VALUE_AUTO = "auto";
    public static final String FLASH_VALUE_TORCH = "torch";

    private static final ArrayList<String> SPEC_KEY = new ArrayList<>(3);

    static {
        SPEC_KEY.add(KEY_PICTURE_SIZE);
        SPEC_KEY.add(KEY_PREVIEW_SIZE);
        SPEC_KEY.add(KEY_PICTURE_FORMAT);
    }

    private SharedPreferences mSharedPreference;
    private Context mContext;
    private Point mRealDisplaySize = new Point();

    public CameraSettings(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.camera_setting, false);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context
                .WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealSize(mRealDisplaySize);
        mContext = context;
    }

    /**
     * get related shared preference by camera id
     * @param cameraId valid camera id
     * @return related SharedPreference from the camera id
     */
    public SharedPreferences getSharedPrefById(String cameraId) {
        return mContext.getSharedPreferences(getSharedPrefName(cameraId), Context.MODE_PRIVATE);
    }

    public String getValueFromPref(String cameraId, String key, String defaultValue) {
        SharedPreferences preferences;
        if (!SPEC_KEY.contains(key)) {
            preferences = mSharedPreference;
        } else {
            preferences = getSharedPrefById(cameraId);
        }
        return preferences.getString(key, defaultValue);
    }

    public boolean setPrefValueById(String cameraId, String key, String value) {
        SharedPreferences preferences;
        if (!SPEC_KEY.contains(key)) {
            preferences = mSharedPreference;
        } else {
            preferences = getSharedPrefById(cameraId);
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }


    public boolean setGlobalPref(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public String getGlobalPref(String key, String defaultValue) {
        return mSharedPreference.getString(key, defaultValue);
    }

    private String getSharedPrefName(String cameraId) {
        return mContext.getPackageName() + "_camera_" + cameraId;
    }

    public int getPicFormat(String id, String key) {
        return Integer.parseInt(getValueFromPref(id, key, Config.IMAGE_FORMAT));
    }

    public String getPicFormatStr(String id, String key) {
        return getValueFromPref(id, key, Config.IMAGE_FORMAT);
    }

    public boolean needStartPreview() {
        return mSharedPreference.getBoolean(KEY_RESTART_PREVIEW, true);
    }

    public Size getPictureSize(String id, String key, StreamConfigurationMap map, int format) {
        String picStr = getValueFromPref(id, key, Config.NULL_VALUE);
        if (Config.NULL_VALUE.equals(picStr)) {
            // preference not set, use default value
            return CameraUtil.getDefaultPictureSize(map, format);
        } else {
            String[] size = picStr.split(CameraUtil.SPLIT_TAG);
            return new Size(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        }
    }

    public String getPictureSizeStr(String id, String key, StreamConfigurationMap map, int format) {
        String picStr = getValueFromPref(id, key, Config.NULL_VALUE);
        if (Config.NULL_VALUE.equals(picStr)) {
            // preference not set, use default value
            Size size = CameraUtil.getDefaultPictureSize(map, format);
            return size.getWidth() + CameraUtil.SPLIT_TAG + size.getHeight();
        } else {
            return picStr;
        }
    }

    public Size getPreviewSize(String id, String key, StreamConfigurationMap map) {
        String preStr = getValueFromPref(id, key, Config.NULL_VALUE);
        if (Config.NULL_VALUE.equals(preStr)) {
            // preference not set, use default value
            return CameraUtil.getDefaultPreviewSize(map, mRealDisplaySize);
        } else {
            String[] size = preStr.split(CameraUtil.SPLIT_TAG);
            return new Size(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        }
    }

    public String getPreviewSizeStr(String id, String key, StreamConfigurationMap map) {
        String preStr = getValueFromPref(id, key, Config.NULL_VALUE);
        if (Config.NULL_VALUE.equals(preStr)) {
            // preference not set, use default value
            Size size = CameraUtil.getDefaultPreviewSize(map, mRealDisplaySize);
            return size.getWidth() + CameraUtil.SPLIT_TAG + size.getHeight();
        } else {
            return preStr;
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
