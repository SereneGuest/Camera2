package com.smewise.camera2.manager;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.smewise.camera2.R;
import com.smewise.camera2.data.CamListPreference;
import com.smewise.camera2.data.PrefListAdapter;
import com.smewise.camera2.data.PreferenceGroup;
import com.smewise.camera2.module.CameraModule;
import com.smewise.camera2.module.DualCameraModule;
import com.smewise.camera2.module.PhotoModule;
import com.smewise.camera2.module.ProfessionalModule;
import com.smewise.camera2.ui.ModuleIndicator;
import com.smewise.camera2.utils.XmlInflater;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenzhe on 9/12/17.
 */

public class ModuleManager implements PrefListAdapter.PrefClickListener {
    private static int sModuleNum = 1;
    private static int mCurrentIndex = 0;
    private CameraModule mCurrentModule;
    private ModuleIndicator mIndicator;
    private Class<?>[] mModulesClass;
    private Controller mController;

    /**
     * Manage all module, use reflection to create module instance
     * @param context used to init ModuleIndicator
     * @param controller interface for change module
     */
    public ModuleManager(Context context, Controller controller) {
        mController = controller;
        mIndicator = new ModuleIndicator(context);
        boolean loadDualCamera = mController.getCameraSettings(context).isDualCameraEnable();
        mModulesClass = mIndicator.getModuleClass(loadDualCamera);
        sModuleNum = mModulesClass.length;
        mIndicator.setPrefClickListener(this);
    }

    public boolean needChangeModule(int index) {
        if (index < 0 || index >= sModuleNum || mCurrentIndex == index) {
            return false;
        } else {
            mCurrentIndex = index;
            mIndicator.updateHighlightIndex(mCurrentIndex);
            return true;
        }
    }

    public CameraModule getNewModule() {
        try {
            mCurrentModule = (CameraModule) mModulesClass[mCurrentIndex].newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return mCurrentModule;
    }

    public CameraModule getCurrentModule() {
        return mCurrentModule;
    }

    public View getIndicatorView() {
        return mIndicator.getIndicatorView();
    }

    public static int getCurrentIndex() {
        return mCurrentIndex;
    }

    public static boolean isValidIndex(int index) {
        return (index >= 0 && index < sModuleNum);
    }

    public static int getModuleCount() {
        return sModuleNum;
    }

    @Override
    public void onClick(View view, int position, CamListPreference preference) {
        mController.changeModule(position);
    }
}
