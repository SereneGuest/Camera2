package com.smewise.camera2.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

/**
 * Created by wenzhe on 11/27/17.
 */

public class CameraSubMenu extends CameraBaseMenu{

    private SubMenuListAdapter mAdapter;

    public CameraSubMenu(Context context, CameraPreference preference) {
        super(context);
        popWindow.setOutsideTouchable(false);
        mAdapter = new SubMenuListAdapter(context, preference);
        recycleView.setAdapter(mAdapter);
    }

    public void setItemClickListener(Listener listener) {
        mAdapter.setMenuListener(listener);
    }

    public void notifyDataSetChange(CameraPreference preference) {
        mAdapter.updateDataSet(preference);
    }

    @Override
    public void show(View view, int xOffset, int yOffset) {
        if (!popWindow.isShowing()) {
            popWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER, xOffset, yOffset);
        }
    }
}
