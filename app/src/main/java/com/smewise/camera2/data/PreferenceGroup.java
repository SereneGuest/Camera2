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

    public int find(String key) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getKey().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    public void remove(String key) {
        int index = find(key);
        if (index >= 0) {
            mList.remove(index);
        }
    }

    public void clear() {
        mList.clear();
    }
}
