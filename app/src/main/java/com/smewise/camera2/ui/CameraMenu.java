package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import com.smewise.camera2.utils.XmlInflater;

/**
 * Created by wenzhe on 11/27/17.
 */

public class CameraMenu {
    private PreferenceGroup mGroup;
    private ListAdapter mAdapter;
    private RecyclerView mRecycleView;
    private PopupWindow mPopWindow;
    public CameraMenu(Context context, int resId) {
        XmlInflater xmlInflater = new XmlInflater(context);
        mGroup = xmlInflater.inflate(resId);
        mAdapter = new ListAdapter(context, mGroup);
        initRecycleView(context);
        initPopupWindow(context);
    }

    private void initRecycleView(Context context) {
        mRecycleView = new RecyclerView(context);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(1,
                StaggeredGridLayoutManager.HORIZONTAL);
        manager.setReverseLayout(true);
        mRecycleView.setLayoutManager(manager);
        mRecycleView.setHasFixedSize(true);
        mRecycleView.setAdapter(mAdapter);
    }

    private void initPopupWindow(Context context) {
        mPopWindow = new PopupWindow(context);
        mPopWindow.setContentView(mRecycleView);
        mPopWindow.setOutsideTouchable(true);
        mPopWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void show(View view) {
        mPopWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.RIGHT, 0, view.getHeight());
    }
}
