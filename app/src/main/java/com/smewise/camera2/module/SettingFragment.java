package com.smewise.camera2.module;

import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smewise.camera2.Config;
import com.smewise.camera2.R;
import com.smewise.camera2.manager.Camera2Manager;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.utils.CameraUtil;

/**
 * Created by wenzhe on 9/25/17.
 */

public class SettingFragment extends PreferenceFragment {

    private static final String TAG = Config.TAG_PREFIX + "SettingFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.camera_setting);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.camera_setting, false);
        initPreference();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    private void initPreference() {
        // Camera
        initCameraInfo(CameraSettings.KEY_CAMERA_ID, CameraSettings.KEY_PICTURE_SIZE,
                CameraSettings.KEY_PREVIEW_SIZE, CameraSettings.KEY_PICTURE_FORMAT);
        // Dual Camera
        initCameraInfo(CameraSettings.KEY_MAIN_CAMERA_ID, CameraSettings.KEY_MAIN_PICTURE_SIZE,
                CameraSettings.KEY_MAIN_PREVIEW_SIZE, CameraSettings.KEY_MAIN_PICTURE_FORMAT);
        initCameraInfo(CameraSettings.KEY_AUX_CAMERA_ID, CameraSettings.KEY_AUX_PICTURE_SIZE,
                CameraSettings.KEY_AUX_PREVIEW_SIZE, CameraSettings.KEY_AUX_PICTURE_FORMAT);
    }

    private void initCameraInfo(String idKey, String picSizeKey, String preSizeKey, String
            picFormatKey) {
        // get camera id info
        ListPreference camIdPref = (ListPreference) findPreference(idKey);
        String cameraId = camIdPref.getValue();
        String[] idList = Camera2Manager.getManager().getCameraIdList(getActivity());
        camIdPref.setEntries(idList);
        camIdPref.setEntryValues(idList);
        // aux camera not set default value in xml, set largest camera id
        if (cameraId == null) {
            camIdPref.setValueIndex(idList.length - 1);
        }
        camIdPref.setSummary(camIdPref.getValue());
        Log.d(TAG, "camera id:" + cameraId);
        StreamConfigurationMap map = Camera2Manager.getManager().getConfigMap(getActivity(),
                camIdPref.getValue());
        // get picture format info
        int currentFormat = setPictureFormat(map, picFormatKey);
        // get picture size info
        String[] picSize = CameraUtil.getPictureSizeList(map, currentFormat);
        setCameraSizeInfo(picSizeKey, picSize);
        // get preview size info
        String[] preSize = CameraUtil.getPreviewSizeList(map);
        setCameraSizeInfo(preSizeKey, preSize);
    }

    private int setPictureFormat(StreamConfigurationMap map, String key) {
        ListPreference picFormatPref = (ListPreference) findPreference(key);
        int[] supportFormat = map.getOutputFormats();
        // get support format display string and value string
        String[][] supportFormatStr = CameraUtil.getOutputFormat(supportFormat);
        picFormatPref.setEntries(supportFormatStr[0]);
        picFormatPref.setEntryValues(supportFormatStr[1]);
        int index = 0;
        // if no value , set JPEG for default
        String currentValue = picFormatPref.getValue();
        int desireValue = currentValue == null ? ImageFormat.JPEG : Integer.parseInt(currentValue);
        for (int i = 0; i < supportFormat.length; i++) {
            if (supportFormat[i] == desireValue) {
                index = i;
                break;
            }
        }
        picFormatPref.setValueIndex(index);
        picFormatPref.setSummary(picFormatPref.getValue());
        return Integer.parseInt(picFormatPref.getValue());
    }

    private void setCameraSizeInfo(String key, String[] size) {
        ListPreference listPreference = (ListPreference) findPreference(key);
        listPreference.setEntries(size);
        listPreference.setEntryValues(size);
        if (listPreference.getValue() == null) {
            //first init set default value
            listPreference.setValueIndex(0);
        } else {
            // value exist, check whether value in size list
            int index = 0;
            String value = listPreference.getValue();
            for (int i = 0; i < size.length; i++) {
                if (value.equals(size[i])) {
                    index = i;
                    break;
                }
            }
            listPreference.setValueIndex(index);
        }
        listPreference.setSummary(listPreference.getValue());
        Log.d(TAG, key + "--" + listPreference.getValue());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_list, null);
        View bottomBar = view.findViewById(R.id.bottom_bar);
        bottomBar.getLayoutParams().height = CameraUtil.getVirtualKeyHeight(getActivity());
        return view;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case CameraSettings.KEY_PICTURE_SIZE:
                break;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener = new
            SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference(key);
            switch (key) {
                case CameraSettings.KEY_CAMERA_ID:
                case CameraSettings.KEY_PICTURE_FORMAT:
                    initCameraInfo(CameraSettings.KEY_CAMERA_ID,
                            CameraSettings.KEY_PICTURE_SIZE,
                            CameraSettings.KEY_PREVIEW_SIZE,
                            CameraSettings.KEY_PICTURE_FORMAT);
                    break;
                case CameraSettings.KEY_MAIN_CAMERA_ID:
                case CameraSettings.KEY_MAIN_PICTURE_FORMAT:
                    initCameraInfo(CameraSettings.KEY_MAIN_CAMERA_ID,
                            CameraSettings.KEY_MAIN_PICTURE_SIZE,
                            CameraSettings.KEY_MAIN_PREVIEW_SIZE,
                            CameraSettings.KEY_MAIN_PICTURE_FORMAT);
                    break;
                case CameraSettings.KEY_AUX_CAMERA_ID:
                case CameraSettings.KEY_AUX_PICTURE_FORMAT:
                    initCameraInfo(CameraSettings.KEY_AUX_CAMERA_ID,
                            CameraSettings.KEY_AUX_PICTURE_SIZE,
                            CameraSettings.KEY_AUX_PREVIEW_SIZE,
                            CameraSettings.KEY_AUX_PICTURE_FORMAT);
                    break;
                case CameraSettings.KEY_RESTART_PREVIEW:
                    // no need to set summary
                    break;
                default:
                    preference.setSummary(sharedPreferences.getString(key, "null"));
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }
}
