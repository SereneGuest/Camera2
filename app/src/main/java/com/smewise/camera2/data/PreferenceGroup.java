package com.smewise.camera2.data;

import java.util.ArrayList;

/**
 * Created by wenzhe on 11/27/17.
 */

public class PreferenceGroup {
    private ArrayList<CamListPreference> mList = new ArrayList<>();

    public void add(CamListPreference camListPreference) {
        mList.add(camListPreference);
    }

    public CamListPreference get(int index) {
        return mList.get(index);
    }

    public int size() {
        return mList.size();
    }

    public CamListPreference find(String key) {
        for (CamListPreference preference : mList) {
            if (preference.getKey().equals(key)) {
                return preference;
            }
        }
        return null;
    }
}
