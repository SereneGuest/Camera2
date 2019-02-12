package com.smewise.camera2.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ViewGroup;

/**
 * Created by wenzhe on 1/16/18.
 */

public abstract class CameraBaseMenu {
    protected RecyclerView recycleView;

    public interface OnMenuClickListener {
        void onMenuClick(String key, String value);
    }

    protected CameraBaseMenu(Context context) {
        recycleView = new RecyclerView(context);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recycleView.setLayoutParams(params);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(1,
                StaggeredGridLayoutManager.HORIZONTAL);
        manager.setReverseLayout(true);
        recycleView.setLayoutManager(manager);
        recycleView.setHasFixedSize(true);
    }
}
