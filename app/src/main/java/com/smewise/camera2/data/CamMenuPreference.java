package com.smewise.camera2.data;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import com.smewise.camera2.Config;
import com.smewise.camera2.R;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.DeviceManager;


/**
 * Created by wenzhe on 11/27/17.
 */

public class CamMenuPreference extends CamListPreference{
    private static final String TAG = Config.getTag(CamMenuPreference.class);
    private int mIcon;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private int[] mEntryIcons;
    private String mDefaultValue;

    public CamMenuPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CamListPreference);
        mIcon = a.getResourceId(R.styleable.CamListPreference_icon, 0);
        mEntries = a.getTextArray(R.styleable.CamListPreference_entries);
        mEntryValues = a.getTextArray(R.styleable.CamListPreference_entryValues);
        mEntryIcons = getIds(context.getResources(),
                a.getResourceId(R.styleable.CamListPreference_entryIcons, 0));
        mDefaultValue = a.getString(R.styleable.CamListPreference_defaultValue);
        a.recycle();
    }

    @Override
    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
    }

    @Override
    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
    }

    @Override
    public void setIcon(int icon) {
        mIcon = icon;
    }

    @Override
    public int getIcon() {
        return mIcon;
    }

    @Override
    public CharSequence[] getEntries() {
        return mEntries;
    }

    @Override
    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    @Override
    public int[] getEntryIcons() {
        return mEntryIcons;
    }

    private void dump() {
        Log.d(TAG, "key:" + getKey());
        Log.d(TAG, "title:" + getTitle());
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
