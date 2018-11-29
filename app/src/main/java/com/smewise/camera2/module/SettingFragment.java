package com.smewise.camera2.module;

import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
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
import com.smewise.camera2.utils.SupportInfoDialog;

/**
 * Created by wenzhe on 9/25/17.
 */

public class SettingFragment extends PreferenceFragment {

    private static final String TAG = Config.getTag(SettingFragment.class);
    private DeviceManager mManger;
    private String mBackCameraStr;
    private String mFrontCameraStr;
    private String mOtherCameraStr;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mManger = new DeviceManager(getActivity());
        mBackCameraStr = getString(R.string.setting_back_camera);
        mFrontCameraStr = getString(R.string.setting_front_camera);
        mOtherCameraStr = getString(R.string.setting_other_camera);
        addPreferencesFromResource(R.xml.camera_setting);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.camera_setting, false);
        initPreference();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    private CameraSettings getSettingMgr() {
        return ((CameraActivity) getActivity()).getController()
                .getCameraSettings(getActivity().getApplicationContext());
    }

    private String setCameraIdPref(String idKey) {
        // TODO: no need setEntries every time
        String[] idList = mManger.getCameraIdList();
        ListPreference idPref = (ListPreference) findPreference(idKey);
        idPref.setEntryValues(idList);
        idPref.setEntries(getCameraIdEntries(idList));
        // aux camera not set default value in xml,
        // set largest camera id by default
        if (idPref.getValue() == null) {
            idPref.setValueIndex(idList.length - 1);
        }
        idPref.setSummary(idPref.getEntry());
        return idPref.getValue();
    }

    private int setPictureFormatPref(String cameraId, String key) {
        ListPreference picFormatPref = (ListPreference) findPreference(key);
        int[] supportFormat = mManger.getConfigMap(cameraId).getOutputFormats();
        // get support format display string and value string
        String[][] supportFormatStr = CameraUtil.getOutputFormat(supportFormat);
        picFormatPref.setEntries(supportFormatStr[0]);
        picFormatPref.setEntryValues(supportFormatStr[1]);
        // if no value , set JPEG for default
        String currentValue = getSettingMgr().getPicFormatStr(cameraId, key);
        int index = findIndexByValue(currentValue, supportFormatStr[1]);
        // save to preference, not default preference
        getSettingMgr().setPrefValueById(cameraId, key, supportFormatStr[1][index]);
        picFormatPref.setValueIndex(index);
        picFormatPref.setSummary(picFormatPref.getValue());
        return supportFormat[index];
    }

    private void setPictureSizePref(String cameraId, String picKey, int format) {
        ListPreference picPreference = (ListPreference) findPreference(picKey);
        StreamConfigurationMap map = mManger.getConfigMap(cameraId);
        String[] sizes = CameraUtil.getPictureSizeList(map, format);
        picPreference.setEntries(sizes);
        picPreference.setEntryValues(sizes);
        String currentPicSize = getSettingMgr().getPictureSizeStr(cameraId, picKey, map, format);
        int index = findIndexByValue(currentPicSize, sizes);
        // save to preference, not default preference
        getSettingMgr().setPrefValueById(cameraId, picKey, sizes[index]);
        picPreference.setValueIndex(index);
        picPreference.setSummary(picPreference.getValue());
        Log.d(TAG, picKey + "--" + picPreference.getValue());
    }

    private void setVideoSizePref(String cameraId, String videoKey) {
        ListPreference videoPreference = (ListPreference) findPreference(videoKey);
        StreamConfigurationMap map = mManger.getConfigMap(cameraId);
        String[] sizes = CameraUtil.getVideoSizeList(map);
        videoPreference.setEntries(sizes);
        videoPreference.setEntryValues(sizes);
        String currentVideoSize = getSettingMgr().getVideoSizeStr(cameraId, videoKey, map);
        int index = findIndexByValue(currentVideoSize, sizes);
        // save to preference, not default preference
        getSettingMgr().setPrefValueById(cameraId, videoKey, sizes[index]);
        videoPreference.setValueIndex(index);
        videoPreference.setSummary(videoPreference.getValue());
        Log.d(TAG, videoKey + "--" + videoPreference.getValue());
    }

    private void setPreviewSizePref(String cameraId, String key) {
        ListPreference prePreference = (ListPreference) findPreference(key);
        StreamConfigurationMap map = mManger.getConfigMap(cameraId);
        String[] sizes = CameraUtil.getPreviewSizeList(map);
        prePreference.setEntries(sizes);
        prePreference.setEntryValues(sizes);
        String currentPreSize = getSettingMgr().getPreviewSizeStr(cameraId, key, map);
        int index = findIndexByValue(currentPreSize, sizes);
        // save to preference, not default preference
        getSettingMgr().setPrefValueById(cameraId, key, sizes[index]);
        prePreference.setValueIndex(index);
        prePreference.setSummary(prePreference.getValue());
        Log.d(TAG, key + "--" + prePreference.getValue());
    }

    private void initPreference() {
        // Photo preference init
        String cameraId = setCameraIdPref(CameraSettings.KEY_CAMERA_ID);
        int picFormat = setPictureFormatPref(cameraId, CameraSettings.KEY_PICTURE_FORMAT);
        setPictureSizePref(cameraId, CameraSettings.KEY_PICTURE_SIZE, picFormat);
        setPreviewSizePref(cameraId, CameraSettings.KEY_PREVIEW_SIZE);
        // Video preference init
        String videoId = setCameraIdPref(CameraSettings.KEY_VIDEO_ID);
        setVideoSizePref(videoId, CameraSettings.KEY_VIDEO_SIZE);
        // Dual camera preference init
        setCameraIdPref(CameraSettings.KEY_MAIN_CAMERA_ID);
        setCameraIdPref(CameraSettings.KEY_AUX_CAMERA_ID);
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
        View view = inflater.inflate(R.layout.pref_settings_layout, container, false);
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
            case CameraSettings.KEY_SUPPORT_INFO:
                SupportInfoDialog dialog = new SupportInfoDialog();
                dialog.setMessage(getSettingMgr().getSupportInfo(getActivity()));
                dialog.show(getFragmentManager(), SupportInfoDialog.class.getSimpleName());
                break;
            default:
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
                case CameraSettings.KEY_CAMERA_ID:{
                    String cameraId = ((ListPreference) preference).getValue();
                    int format = setPictureFormatPref(cameraId, CameraSettings.KEY_PICTURE_FORMAT);
                    setPictureSizePref(cameraId, CameraSettings.KEY_PICTURE_SIZE, format);
                    setPreviewSizePref(cameraId, CameraSettings.KEY_PREVIEW_SIZE);
                    preference.setSummary(((ListPreference) preference).getEntry());
                    break;
                }
                case CameraSettings.KEY_VIDEO_ID:{
                    String cameraId = ((ListPreference) preference).getValue();
                    setVideoSizePref(cameraId, CameraSettings.KEY_VIDEO_SIZE);
                    preference.setSummary(((ListPreference) preference).getEntry());
                    break;
                }
                case CameraSettings.KEY_MAIN_CAMERA_ID:
                case CameraSettings.KEY_AUX_CAMERA_ID:
                    ListPreference pref = (ListPreference) preference;
                    pref.setSummary(pref.getEntry());
                    break;
                case CameraSettings.KEY_PICTURE_FORMAT:{
                    String value = ((ListPreference) preference).getValue();
                    updatePrefById(key, value, CameraSettings.KEY_CAMERA_ID);
                    // format change, update picture size
                    String id = getCameraId(CameraSettings.KEY_CAMERA_ID);
                    setPictureSizePref(id, CameraSettings.KEY_PICTURE_SIZE, Integer.parseInt(value));
                    preference.setSummary(value);
                    break;
                }
                case CameraSettings.KEY_RESTART_PREVIEW:
                    // no need to set summary
                    break;
                case CameraSettings.KEY_ENABLE_DUAL_CAMERA:
                    // no need to set summary
                    break;
                case CameraSettings.KEY_VIDEO_SIZE:{
                    String value = ((ListPreference) preference).getValue();
                    updatePrefById(key, value, CameraSettings.KEY_VIDEO_ID);
                    preference.setSummary(value);
                    break;
                }
                case CameraSettings.KEY_PREVIEW_SIZE:
                case CameraSettings.KEY_PICTURE_SIZE:
                    String value = ((ListPreference) preference).getValue();
                    updatePrefById(key, value, CameraSettings.KEY_CAMERA_ID);
                    preference.setSummary(value);
                    break;
                default:
                    preference.setSummary(sharedPreferences.getString(key, "null"));
                    break;
            }
        }
    };

    /**
     * Update preference with camera id, picture format, picture size and preview size
     * will save to speicific preference, not default preference
     * @param key the key which preference need to update
     * @param value update value
     * @param idKey used for get camera id
     */
    private void updatePrefById(String key, String value, String idKey) {
        String cameraId = getCameraId(idKey);
        getSettingMgr().setPrefValueById(cameraId, key, value);
    }

    private String getCameraId(String idKey) {
        ListPreference preference = (ListPreference) findPreference(idKey);
        return preference.getValue();
    }

    private String[] getCameraIdEntries(String[] idList) {
        String[] entries = new String[idList.length];
        for (int i = 0; i < idList.length; i++) {
            Integer face = mManger.getCharacteristics(idList[i])
                    .get(CameraCharacteristics.LENS_FACING);
            if (CameraCharacteristics.LENS_FACING_BACK == face) {
                entries[i] = mBackCameraStr + "(ID:" + idList[i] + ")";
            } else if (CameraCharacteristics.LENS_FACING_FRONT == face) {
                entries[i] = mFrontCameraStr + "(ID:" + idList[i] + ")";
            } else {
                entries[i] = mOtherCameraStr + "(ID:" + idList[i] + ")";
            }
        }
        return entries;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }
}
