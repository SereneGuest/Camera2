package com.smewise.camera2.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.smewise.camera2.Config;
import com.smewise.camera2.callback.MenuInfo;
import com.smewise.camera2.data.CamListPreference;
import com.smewise.camera2.data.PrefListAdapter;
import com.smewise.camera2.data.PreferenceGroup;
import com.smewise.camera2.data.SubPrefListAdapter;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.utils.XmlInflater;

/**
 * Created by wenzhe on 11/27/17.
 */

public class CameraMenu extends CameraBaseMenu {

    public static final String TAG = Config.getTag(CameraMenu.class);
    private CameraSubMenu mSubMenu;
    private Context mContext;
    private OnMenuClickListener mMenuClickListener;
    private MenuInfo mMenuInfo;
    private PrefListAdapter mAdapter;

    public CameraMenu(Context context, int resId, MenuInfo info) {
        super(context);
        mContext = context;
        mMenuInfo = info;
        XmlInflater xmlInflater = new XmlInflater(context);
        mAdapter = new PrefListAdapter(context, xmlInflater.inflate(resId));
        updateAllMenuIcon();
        mAdapter.setClickListener(mMenuListener);
        recycleView.setAdapter(mAdapter);
    }

    private void updateAllMenuIcon() {
        PreferenceGroup group = mAdapter.getPrefGroup();
        for (int i = 0; i < group.size(); i++) {
            updateMenuIcon(i);
        }
    }

    private void updateMenuIcon(int position) {
        CamListPreference preference = mAdapter.getPrefGroup().get(position);
        switch (preference.getKey()) {
            case CameraSettings.KEY_SWITCH_CAMERA:
                updateSwitchIcon(preference);
                break;
            default:
                break;
        }
        mAdapter.notifyItemChanged(position);
    }

    private void updateSwitchIcon(CamListPreference preference) {
        int index = getIndex(preference.getEntryValues(),
                mMenuInfo.getCurrentCameraId());
        if (index < preference.getEntryIcons().length) {
            preference.setIcon(preference.getEntryIcons()[index]);
        }
    }

    public View getView() {
        return recycleView;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        mMenuClickListener = listener;
    }

    /**
     * Camera menu click listener
     */
    private PrefListAdapter.PrefClickListener mMenuListener =
            new PrefListAdapter.PrefClickListener() {
        @Override
        public void onClick(View view, int position, CamListPreference preference) {
            // if is switch menu click, no need show sub menu
            if (preference.getKey().equals(CameraSettings.KEY_SWITCH_CAMERA)) {
                if (mMenuClickListener != null) {
                    mMenuClickListener.onMenuClick(preference.getKey(), null);
                    updateMenuIcon(position);
                }
                return;
            }
            if (mSubMenu == null) {
                mSubMenu = new CameraSubMenu(mContext, preference);
                mSubMenu.setItemClickListener(mItemClickListener);
            }
            mSubMenu.notifyDataSetChange(preference, mMenuInfo);
            mSubMenu.show(view, 0, view.getHeight());
        }
    };

    /**
     * Camera sub menu click listener
     */
    private SubPrefListAdapter.PrefItemClickListener mItemClickListener =
            new SubPrefListAdapter.PrefItemClickListener() {
        @Override
        public void onItemClick(String key, String value) {
            Log.d(TAG, "sub menu click key:" + key + " value:" + value);
            if (mMenuClickListener != null) {
                mMenuClickListener.onMenuClick(key, value);
            }
        }
    };

    public void close() {
        if (mSubMenu != null) {
            mSubMenu.close();
        }
    }
}
