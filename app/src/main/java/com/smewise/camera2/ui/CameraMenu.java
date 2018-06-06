package com.smewise.camera2.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.smewise.camera2.Config;
import com.smewise.camera2.data.CamListPreference;
import com.smewise.camera2.data.PrefListAdapter;
import com.smewise.camera2.data.SubPrefListAdapter;
import com.smewise.camera2.utils.XmlInflater;

/**
 * Created by wenzhe on 11/27/17.
 */

public class CameraMenu extends CameraBaseMenu {

    public static final String TAG = Config.getTag(CameraMenu.class);
    private CameraSubMenu mSubMenu;
    private Context mContext;
    private OnMenuClickListener mMenuClickListener;

    public CameraMenu(Context context, int resId, OnMenuClickListener listener) {
        super(context);
        mContext = context;
        XmlInflater xmlInflater = new XmlInflater(context);
        PrefListAdapter adapter = new PrefListAdapter(context, xmlInflater.inflate(resId));
        adapter.setClickListener(mMenuListener);
        recycleView.setAdapter(adapter);
        mMenuClickListener = listener;
    }

    public View getView() {
        return recycleView;
    }

    private PrefListAdapter.PrefClickListener mMenuListener =
            new PrefListAdapter.PrefClickListener() {
        @Override
        public void onClick(View view, int position, CamListPreference preference) {
            if (mSubMenu == null) {
                mSubMenu = new CameraSubMenu(mContext, preference);
                mSubMenu.setItemClickListener(mItemClickListener);
            } else {
                mSubMenu.notifyDataSetChange(preference);
            }
            mSubMenu.show(view, 0, view.getHeight());
        }
    };

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
