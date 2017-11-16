package com.smewise.camera2.manager;

import com.smewise.camera2.module.CameraModule;
import com.smewise.camera2.module.DualCameraModule;
import com.smewise.camera2.module.PhotoModule;
import com.smewise.camera2.module.ProfessionalModule;

/**
 * Created by wenzhe on 9/12/17.
 */

public class ModuleManager {
    public static final int MODULE_PHOTO = 0;
    public static final int MODULE_DUAL_CAMERA = 1;
    public static final int MODULE_PROFESSIONAL = 2;
    public static final int MODULE_NUM = 3;

    private CameraModule[] mModules = new CameraModule[] {
            new PhotoModule(),
            new DualCameraModule(),
            new ProfessionalModule(),
    };

    private static int mCurrentIndex = MODULE_PHOTO;
    private CameraModule mCurrentModule;

    public boolean needChangeModule(int index) {
        if (index < 0 || index >= mModules.length || mCurrentIndex == index) {
            return false;
        } else {
            mCurrentIndex = index;
            return true;
        }
    }

    public CameraModule getNewModule() {
        mCurrentModule = mModules[mCurrentIndex];
        return mCurrentModule;
    }

    public CameraModule getCurrentModule() {
        return mCurrentModule;
    }

    public static int getCurrentIndex() {
        return mCurrentIndex;
    }

    public static boolean isValidIndex(int index) {
        return (index >= 0 && index < MODULE_NUM);
    }

}
