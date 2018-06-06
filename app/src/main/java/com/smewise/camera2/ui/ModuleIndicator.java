package com.smewise.camera2.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.smewise.camera2.R;
import com.smewise.camera2.data.PrefListAdapter;
import com.smewise.camera2.data.PreferenceGroup;
import com.smewise.camera2.utils.XmlInflater;

public class ModuleIndicator {
    private PreferenceGroup mGroup;
    private PrefListAdapter mAdapter;
    private RecyclerView mRecycleView;

    public ModuleIndicator(Context context) {
        XmlInflater inflater = new XmlInflater(context);
        mGroup = inflater.inflate(R.xml.module_preference);

        mAdapter = new PrefListAdapter(context, mGroup);
        // init recycler view
        mRecycleView = new RecyclerView(context);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRecycleView.setLayoutParams(params);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setHasFixedSize(true);
        mRecycleView.setAdapter(mAdapter);
    }

    public View getIndicatorView() {
        return mRecycleView;
    }

    public Class<?>[] getModuleClass() {
        Class<?>[] moduleCls = new Class[mGroup.size()];
        for (int i = 0; i < mGroup.size(); i++) {
            try {
                moduleCls[i] = Class.forName(mGroup.get(i).getKey());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return moduleCls;
    }

    public void setPrefClickListener(PrefListAdapter.PrefClickListener listener) {
        mAdapter.setClickListener(listener);
    }
}
