package com.smewise.camera2.manager;

import com.smewise.camera2.module.CameraModule;
import com.smewise.camera2.module.DualCameraModule;
import com.smewise.camera2.module.PhotoModule;
import com.smewise.camera2.module.ProfessionalModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenzhe on 9/12/17.
 */

public class ModuleManager {
    private static final String MODULE_PHOTO = "PhotoModule";
    private static final String MODULE_DUAL_CAMERA = "DualCameraModule";
    private static final String MODULE_PROFESSIONAL = "ProfessionalModule";
    private final List<String> moduleList;
    // at least on module
    private static int sModuleNum = 1;
    private static int mCurrentIndex = 0;
    private CameraModule mCurrentModule;

    public ModuleManager() {
        moduleList = new ArrayList<>();
        // add module
        moduleList.add(MODULE_PHOTO);
        moduleList.add(MODULE_DUAL_CAMERA);
        moduleList.add(MODULE_PROFESSIONAL);
        // update MODULE_NUM by MODULE_INFO size
        sModuleNum = moduleList.size();
    }

    public boolean needChangeModule(int index) {
        if (index < 0 || index >= sModuleNum || mCurrentIndex == index) {
            return false;
        } else {
            mCurrentIndex = index;
            return true;
        }
    }

    public CameraModule getNewModule() {
        switch (moduleList.get(mCurrentIndex)) {
            case MODULE_PHOTO:
                mCurrentModule = new PhotoModule();
                break;
            case MODULE_DUAL_CAMERA:
                mCurrentModule = new DualCameraModule();
                break;
            case MODULE_PROFESSIONAL:
                mCurrentModule = new ProfessionalModule();
                break;
            default:
                mCurrentModule = new PhotoModule();
                break;
        }
        return mCurrentModule;
    }

    public CameraModule getCurrentModule() {
        return mCurrentModule;
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

}
