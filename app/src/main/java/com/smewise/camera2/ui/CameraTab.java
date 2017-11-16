package com.smewise.camera2.ui;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import com.smewise.camera2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenzhe on 9/14/17.
 */

public class CameraTab extends TabLayout {

    private List<Tab> mTabs = new ArrayList<>();

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
        if (strings.length > 2) {
            setTabMode(MODE_SCROLLABLE);
        }
        for (String s : strings) {
            Tab tab = newTab().setText(s);
            addTab(tab);
            mTabs.add(tab);
        }
    }

    public void setSelected(int index) {
        mTabs.get(index).select();
    }

}
