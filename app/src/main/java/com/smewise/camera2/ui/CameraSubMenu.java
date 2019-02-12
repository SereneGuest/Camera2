package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.smewise.camera2.R;
import com.smewise.camera2.data.CamListPreference;
import com.smewise.camera2.data.SubPrefListAdapter;

/**
 * Created by wenzhe on 11/27/17.
 */

public class CameraSubMenu extends CameraBaseMenu{

    private SubPrefListAdapter mAdapter;
    private PopupWindow mPopWindow;

    public CameraSubMenu(Context context, CamListPreference preference) {
        super(context);
        mAdapter = new SubPrefListAdapter(context, preference);
        recycleView.setAdapter(mAdapter);
        initPopWindow(context);
    }

    private void initPopWindow(Context context) {
        mPopWindow = new PopupWindow(context);
        mPopWindow.setContentView(recycleView);
        int color = context.getResources().getColor(R.color.pop_window_bg);
        mPopWindow.setBackgroundDrawable(new ColorDrawable(color));
        mPopWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopWindow.setAnimationStyle(-1);
        mPopWindow.setOutsideTouchable(false);
    }

    public void setItemClickListener(SubPrefListAdapter.PrefItemClickListener listener) {
        mAdapter.setClickListener(listener);
    }

    public void notifyDataSetChange(CamListPreference preference) {
        mAdapter.updateDataSet(preference);
    }

    public void show(View view, int xOffset, int yOffset) {
        if (!mPopWindow.isShowing()) {
            mPopWindow.showAtLocation(view, Gravity.TOP | Gravity.CENTER, xOffset, yOffset);
        } else  {
            mPopWindow.dismiss();
        }
    }

    public void close() {
        if (mPopWindow != null && mPopWindow.isShowing()) {
            mPopWindow.dismiss();
        }
    }
}
