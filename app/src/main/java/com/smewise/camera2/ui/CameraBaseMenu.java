package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.smewise.camera2.R;

/**
 * Created by wenzhe on 1/16/18.
 */

public abstract class CameraBaseMenu {
    protected RecyclerView recycleView;
    protected PopupWindow popWindow;

    public interface Listener {
        void onMenuItemClick(View view, String key, CameraPreference preference);

        void onSubMenuItemClick(String key, String value);
    }

    protected CameraBaseMenu(Context context) {
        // init recycler view
        recycleView = new RecyclerView(context);
        recycleView.setBackgroundResource(R.color.pop_window_bg);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(1,
                StaggeredGridLayoutManager.HORIZONTAL);
        manager.setReverseLayout(true);
        recycleView.setLayoutManager(manager);
        recycleView.setHasFixedSize(true);
        // init pop window
        popWindow = new PopupWindow(context);
        popWindow.setContentView(recycleView);
        popWindow.setOutsideTouchable(true);
        popWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popWindow.setAnimationStyle(-1);
    }

    public abstract void show(View view, int xOffset, int yOffset);
}
