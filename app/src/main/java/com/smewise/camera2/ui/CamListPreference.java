package com.smewise.camera2.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.smewise.camera2.Config;
import com.smewise.camera2.R;


/**
 * Created by wenzhe on 11/27/17.
 */

public class CamListPreference {
    private static final String TAG = Config.getTag(CamListPreference.class);
    private String mKey;
    private String mTitle;

    public CamListPreference(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CamListPreference);
        mKey = a.getString(R.styleable.CamListPreference_key);
        mTitle = a.getString(R.styleable.CamListPreference_title);
        a.recycle();
    }

    public String getKey() {
        return mKey;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getIcon() {
        return 0;
    }

    public CharSequence[] getEntries() {
        return null;
    }

    public CharSequence[] getEntryValues() {
        return null;
    }

    public int[] getEntryIcons() {
        return null;
    }

    public int getHighLightIdx() {
        return -1;
    }

    int[] getIds(Resources res, int iconsRes) {
        if (iconsRes == 0) return null;
        TypedArray array = res.obtainTypedArray(iconsRes);
        int n = array.length();
        int ids[] = new int[n];
        for (int i = 0; i < n; ++i) {
            ids[i] = array.getResourceId(i, 0);
        }
        array.recycle();
        return ids;
    }
}
