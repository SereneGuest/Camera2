package com.smewise.camera2.ui;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.smewise.camera2.R;
import com.smewise.camera2.manager.ModuleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenzhe on 9/14/17.
 */

public class CameraTab extends TabLayout {

    public CameraTab(Context context) {
        this(context, null);
    }

    public CameraTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTab();
    }

    private void initTab() {
        String[] strings = getResources().getStringArray(R.array.module_list);
        int moduleCount = ModuleManager.getModuleCount();
        if (moduleCount > 2) {
            setTabMode(MODE_SCROLLABLE);
        }
        for (int i = 0; i < moduleCount; i++) {
            String str;
            if(i >= strings.length) {
                str = getResources().getString(R.string.un_named);
            } else {
                str = strings[i];
            }
            Tab tab = newTab().setText(str);
            addTab(tab);
        }
    }

    public void setSelected(int index) {
        if (getTabAt(index) != null) {
            getTabAt(index).select();
        }
    }

}
