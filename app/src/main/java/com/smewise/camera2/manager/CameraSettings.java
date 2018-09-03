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
    public static final String KEY_ENABLE_DUAL_CAMERA = "pref_enable_dual_camera";
    public static final String KEY_SUPPORT_INFO = "pref_support_info";
    public static final String KEY_VIDEO_ID = "pref_video_camera_id";
    public static final String KEY_VIDEO_SIZE = "pref_video_size";
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
        SPEC_KEY.add(KEY_VIDEO_SIZE);
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

    public String getGlobalPref(String key) {
        String defaultValue;
        switch (key) {
            case KEY_FLASH_MODE:
                defaultValue = mContext.getResources().getString(R.string.flash_off);
                break;
            case KEY_CAMERA_ID:
                defaultValue = mContext.getResources().getString(R.string.default_camera_id);
                break;
            default:
                defaultValue = "no value";
                break;
        }
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

    public boolean isDualCameraEnable() {
        return mSharedPreference.getBoolean(KEY_ENABLE_DUAL_CAMERA, true);
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

    public Size getPreviewSizeByRatio(StreamConfigurationMap map, double ratio) {
        return CameraUtil.getPreviewSizeByRatio(map, mRealDisplaySize, ratio);
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

    public Size getVideoSize(String id, String key, StreamConfigurationMap map) {
        String videoStr = getValueFromPref(id, key, Config.NULL_VALUE);
        if (Config.NULL_VALUE.equals(videoStr)) {
            // preference not set, use default value
            return CameraUtil.getDefaultVideoSize(map, mRealDisplaySize);
        } else {
            String[] size = videoStr.split(CameraUtil.SPLIT_TAG);
            return new Size(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        }
    }

    public String getVideoSizeStr(String id, String key, StreamConfigurationMap map) {
        String videoStr = getValueFromPref(id, key, Config.NULL_VALUE);
        if (Config.NULL_VALUE.equals(videoStr)) {
            // preference not set, use default value
            Size size = CameraUtil.getDefaultVideoSize(map, mRealDisplaySize);
            return size.getWidth() + CameraUtil.SPLIT_TAG + size.getHeight();
        } else {
            return videoStr;
        }
    }

    public String getSupportInfo(Context context) {
        StringBuilder builder = new StringBuilder();
        DeviceManager deviceManager = new DeviceManager(context);
        String[] idList = deviceManager.getCameraIdList();
        String splitLine = "- - - - - - - - - -";
        builder.append(splitLine).append("\n");
        for (String cameraId : idList) {
            builder.append("Camera ID: ").append(cameraId).append("\n");
            // hardware support level
            CameraCharacteristics c = deviceManager.getCharacteristics(cameraId);
            Integer level = c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            builder.append("Hardware Support Level:").append("\n");
            builder.append(CameraUtil.hardwareLevel2Sting(level)).append("\n");
            builder.append("(LEGACY < LIMITED < FULL < LEVEL_3)").append("\n");
            // Capabilities
            builder.append("Camera Capabilities:").append("\n");
            int[] caps = c.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
            for (int cap : caps) {
                builder.append(CameraUtil.capabilities2String(cap)).append(" ");
            }
            builder.append("\n");
            builder.append(splitLine).append("\n");
        }
        return builder.toString();
    }

}
