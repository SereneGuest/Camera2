package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
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

    public interface OnMenuClickListener {
        void onMenuClick(String key, String value);
    }

    protected CameraBaseMenu(Context context) {
        // init recycler view
        View rootView = LayoutInflater.from(context).inflate(R.layout.pop_up_layout, null);
        recycleView = (RecyclerView) rootView.findViewById(R.id.pop_list);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(1,
                StaggeredGridLayoutManager.HORIZONTAL);
        manager.setReverseLayout(true);
        recycleView.setLayoutManager(manager);
        recycleView.setHasFixedSize(true);
        // init pop window
        popWindow = new PopupWindow(context);
        popWindow.setContentView(rootView);
        popWindow.setOutsideTouchable(true);
        popWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popWindow.setAnimationStyle(-1);
    }

    public abstract void show(View view, int xOffset, int yOffset);

    public void close() {
        if (popWindow.isShowing()) {
            popWindow.dismiss();
        }
    }
}
