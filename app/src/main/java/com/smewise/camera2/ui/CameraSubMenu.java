package com.smewise.camera2.ui;

import android.content.Context;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.smewise.camera2.utils.XmlInflater;

/**
 * Created by wenzhe on 11/27/17.
 */

public class CameraSubMenu extends CameraBaseMenu{

    private SubMenuListAdapter mAdapter;

    public CameraSubMenu(Context context, CameraPreference preference) {
        super(context);
        mAdapter = new SubMenuListAdapter(context, preference);
        recycleView.setAdapter(mAdapter);
    }

    public void notifyDataSetChange(CameraPreference preference) {
        mAdapter.updateDataSet(preference);
    }

    @Override
    public void show(View view, int xOffset, int yOffset) {
        popWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER, xOffset, yOffset);
    }
}
