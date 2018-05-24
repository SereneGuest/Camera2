package com.smewise.camera2.module;

import android.content.SharedPreferences;
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
import android.widget.ImageView;

import com.smewise.camera2.CameraActivity;
import com.smewise.camera2.Config;
import com.smewise.camera2.R;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.DeviceManager;
import com.smewise.camera2.utils.CameraUtil;

/**
 * Created by wenzhe on 9/25/17.
 */

public class SettingFragment extends PreferenceFragment {

    private static final String TAG = Config.getTag(SettingFragment.class);
    private DeviceManager mManger;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mManger = new DeviceManager(getActivity());
        addPreferencesFromResource(R.xml.camera_setting);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.camera_setting, false);
        initPreference();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    private CameraSettings getSettingMgr() {
        return ((CameraActivity) getActivity()).getController().getSettingManager();
    }

    private void initPreference() {
        // Camera
        initCameraInfo(CameraSettings.KEY_CAMERA_ID, CameraSettings.KEY_PICTURE_SIZE,
                CameraSettings.KEY_PREVIEW_SIZE, CameraSettings.KEY_PICTURE_FORMAT);
        // Dual Camera
        String[] idList = mManger.getCameraIdList();
        ListPreference mainIdPref = (ListPreference) findPreference(CameraSettings.KEY_MAIN_CAMERA_ID);
        ListPreference auxIdPref = (ListPreference) findPreference(CameraSettings.KEY_AUX_CAMERA_ID);
        mainIdPref.setEntries(idList);
        mainIdPref.setEntryValues(idList);
        auxIdPref.setEntries(idList);
        auxIdPref.setEntryValues(idList);
        // aux camera not set default value in xml, set largest camera id
        if (auxIdPref.getValue() == null) {
            auxIdPref.setValueIndex(idList.length - 1);
        }
        mainIdPref.setSummary(mainIdPref.getValue());
        auxIdPref.setSummary(auxIdPref.getValue());
    }

    private void initCameraInfo(String idKey, String picSizeKey, String preSizeKey, String
            picFormatKey) {
        // get camera id info
        ListPreference camIdPref = (ListPreference) findPreference(idKey);
        String[] idList = mManger.getCameraIdList();
        camIdPref.setEntries(idList);
        camIdPref.setEntryValues(idList);
        camIdPref.setSummary(camIdPref.getValue());
        StreamConfigurationMap map = mManger.getConfigMap(camIdPref.getValue());
        // get picture format info
        int currentFormat = setPictureFormat(camIdPref.getValue(), map, picFormatKey);
        // get picture size info
        String[] picSize = CameraUtil.getPictureSizeList(map, currentFormat);
        setCameraPicSizeInfo(camIdPref.getValue(), picSizeKey, currentFormat, picSize, map);
        // get preview size info
        String[] preSize = CameraUtil.getPreviewSizeList(map);
        setCameraPreSizeInfo(camIdPref.getValue(), preSizeKey, preSize, map);
    }

    private int setPictureFormat(String cameraId, StreamConfigurationMap map, String key) {
        ListPreference picFormatPref = (ListPreference) findPreference(key);
        int[] supportFormat = map.getOutputFormats();
        // get support format display string and value string
        String[][] supportFormatStr = CameraUtil.getOutputFormat(supportFormat);
        picFormatPref.setEntries(supportFormatStr[0]);
        picFormatPref.setEntryValues(supportFormatStr[1]);
        // if no value , set JPEG for default
        String currentValue = getSettingMgr().getPicFormatStr(cameraId, key);
        int index = findIndexByValue(currentValue, supportFormatStr[1]);
        // if currentValue not in support list (may occur when camera id change)
        // use value of support list [0], and set value to shared preference
        getSettingMgr().setPrefValueById(cameraId, key, supportFormatStr[1][index]);
        picFormatPref.setValueIndex(index);
        picFormatPref.setSummary(picFormatPref.getValue());
        return supportFormat[index];
    }

    private void setCameraPreSizeInfo(String cameraId, String key, String[] size,
                                      StreamConfigurationMap map) {
        ListPreference listPreference = (ListPreference) findPreference(key);
        listPreference.setEntries(size);
        listPreference.setEntryValues(size);
        String currentPreSize = getSettingMgr().getPreviewSizeStr(cameraId, key, map);
        int index = findIndexByValue(currentPreSize, size);
        // if currentValue not in support list (may occur when camera id change)
        // use value of support list [0], and set value to shared preference
        getSettingMgr().setPrefValueById(cameraId, key, size[index]);
        listPreference.setValueIndex(index);
        listPreference.setSummary(listPreference.getValue());
        Log.d(TAG, key + "--" + listPreference.getValue());
    }

    private void setCameraPicSizeInfo(String cameraId, String picKey, int format, String[] size,
                                   StreamConfigurationMap map) {
        ListPreference listPreference = (ListPreference) findPreference(picKey);
        listPreference.setEntries(size);
        listPreference.setEntryValues(size);
        String currentPicSize = getSettingMgr().getPictureSizeStr(cameraId, picKey, map, format);
        int index = findIndexByValue(currentPicSize, size);
        // if currentValue not in support list (may occur when camera id change)
        // use value of support list [0], and set value to shared preference
        getSettingMgr().setPrefValueById(cameraId, picKey, size[index]);
        listPreference.setValueIndex(index);
        listPreference.setSummary(listPreference.getValue());
        Log.d(TAG, picKey + "--" + listPreference.getValue());
    }

    private int findIndexByValue(String value, String[] lists) {
        for (int i = 0; i < lists.length; i++) {
            if (value.equals(lists[i])) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_list, null);
        View bottomBar = view.findViewById(R.id.bottom_bar);
        ImageView menuBack = view.findViewById(R.id.iv_menu_back);
        menuBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CameraActivity) getActivity()).removeSettingFragment();
            }
        });
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
                    initCameraInfo(CameraSettings.KEY_CAMERA_ID,
                            CameraSettings.KEY_PICTURE_SIZE,
                            CameraSettings.KEY_PREVIEW_SIZE,
                            CameraSettings.KEY_PICTURE_FORMAT);
                    break;
                case CameraSettings.KEY_PICTURE_FORMAT:
                    updateSizeValue(key, preference);
                    initCameraInfo(CameraSettings.KEY_CAMERA_ID,
                            CameraSettings.KEY_PICTURE_SIZE,
                            CameraSettings.KEY_PREVIEW_SIZE,
                            CameraSettings.KEY_PICTURE_FORMAT);
                    break;
                case CameraSettings.KEY_RESTART_PREVIEW:
                    // no need to set summary
                    break;
                case CameraSettings.KEY_PREVIEW_SIZE:
                case CameraSettings.KEY_PICTURE_SIZE:
                    updateSizeValue(key, preference);
                    break;
                default:
                    preference.setSummary(sharedPreferences.getString(key, "null"));
                    break;
            }
        }
    };

    private void updateSizeValue(String key, Preference preference) {
        ListPreference listPref = (ListPreference) preference;
        ListPreference idPref = (ListPreference) findPreference(CameraSettings.KEY_CAMERA_ID);
        getSettingMgr().setPrefValueById(idPref.getValue(), key, listPref.getValue());
        listPref.setSummary(listPref.getValue());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }
}
