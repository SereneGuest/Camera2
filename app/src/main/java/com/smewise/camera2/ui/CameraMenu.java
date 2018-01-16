package com.smewise.camera2.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import com.smewise.camera2.utils.XmlInflater;

/**
 * Created by wenzhe on 11/27/17.
 */

public class CameraMenu extends CameraBaseMenu{

    private CameraSubMenu mSubMenu;
    private Context mContext;
    private View mShowView;
    public CameraMenu(Context context, int resId) {
        super(context);
        mContext = context;
        XmlInflater xmlInflater = new XmlInflater(context);
        MenuListAdapter adapter = new MenuListAdapter(context, xmlInflater.inflate(resId));
        adapter.setMenuListener(mMenuListener);
        recycleView.setAdapter(adapter);
    }

    private Listener mMenuListener = new Listener() {
        @Override
        public void onMenuItemClick(View view, String key, CameraPreference preference) {
            if (mSubMenu == null) {
                mSubMenu = new CameraSubMenu(mContext, preference);
            } else {
                mSubMenu.notifyDataSetChange(preference);
            }
            mSubMenu.show(mShowView, 0, mShowView.getHeight() + view.getHeight());
        }

        @Override
        public void onSubMenuItemClick(String key, String value) {

        }
    };

    @Override
    public void show(View view, int xOffset, int yOffset) {
        mShowView = view;
        popWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.RIGHT, xOffset, yOffset);
    }
}
