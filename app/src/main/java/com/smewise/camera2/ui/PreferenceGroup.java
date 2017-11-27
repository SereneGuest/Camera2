package com.smewise.camera2.ui;

import java.util.ArrayList;

/**
 * Created by wenzhe on 11/27/17.
 */

public class PreferenceGroup {
    private ArrayList<CameraPreference> mList = new ArrayList<>();

    public void add(CameraPreference cameraPreference) {
        mList.add(cameraPreference);
    }

    public CameraPreference get(int index) {
        return mList.get(index);
    }

    public int size() {
        return mList.size();
    }

    public CameraPreference find(String key) {
        for (CameraPreference preference : mList) {
            if (preference.getKey().equals(key)) {
                return preference;
            }
        }
        return null;
    }
}
