package com.smewise.camera2.data;

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

    public static final int RES_NULL = 0;

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
        return RES_NULL;
    }

    public CharSequence[] getEntries() {
        return null;
    }

    public CharSequence[] getEntryValues() {
        return null;
    }

    public void setEntries(CharSequence[] entries) {
    }

    public void setEntryValues(CharSequence[] entryValues) {
    }

    public void setIcon(int icon) {
    }

    public int[] getEntryIcons() {
        return null;
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
