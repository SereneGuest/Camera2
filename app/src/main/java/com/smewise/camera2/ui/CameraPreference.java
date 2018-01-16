package com.smewise.camera2.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import com.smewise.camera2.Config;
import com.smewise.camera2.R;


/**
 * Created by wenzhe on 11/27/17.
 */

public class CameraPreference {
    private static final String TAG = Config.TAG_PREFIX + "CameraPref";
    private String mKey;
    private String mTitle;
    private int mIcon;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private int[] mEntryIcons;
    private String mDefaultValue;

    public CameraPreference(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CameraPreference);
        mKey = a.getString(R.styleable.CameraPreference_key);
        mTitle = a.getString(R.styleable.CameraPreference_title);
        mIcon = a.getResourceId(R.styleable.CameraPreference_icon, 0);
        mEntries = a.getTextArray(R.styleable.CameraPreference_entries);
        mEntryValues = a.getTextArray(R.styleable.CameraPreference_entryValues);
        mEntryIcons = getIds(context.getResources(),
                a.getResourceId(R.styleable.CameraPreference_entryIcons, 0));
        mDefaultValue = a.getString(R.styleable.CameraPreference_defaultValue);
        a.recycle();
        dump();
    }

    public String getKey() {
        return mKey;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getIcon() {
        return mIcon;
    }

    public CharSequence[] getEntries() {
        return mEntries;
    }

    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    public int[] getEntryIcons() {
        return mEntryIcons;
    }

    private int[] getIds(Resources res, int iconsRes) {
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

    private void dump() {
        Log.d(TAG, "key:" + mKey);
        Log.d(TAG, "title:" + mTitle);
        Log.d(TAG, "default value:" + mDefaultValue);
        Log.d(TAG, "icon:" + mIcon);
        if (mEntries != null) {
            for (CharSequence str : mEntries) {
                Log.d(TAG, "entries:" + str);
            }
        }
        if (mEntryValues != null) {
            for (CharSequence str : mEntryValues) {
                Log.d(TAG, "entries value:" + str);
            }
        }
        if (mEntryIcons != null) {
            for (int id : mEntryIcons) {
                Log.d(TAG, "entries icon:" + id);
            }
        }

    }
}
