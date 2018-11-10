package com.smewise.camera2.module;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smewise.camera2.CameraActivity;
import com.smewise.camera2.Config;
import com.smewise.camera2.R;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.CameraToolKit;
import com.smewise.camera2.manager.Controller;
import com.smewise.camera2.manager.ModuleManager;
import com.smewise.camera2.ui.AppBaseUI;
import com.smewise.camera2.utils.JobExecutor;

public class CameraFragment extends Fragment {

    private static final String TAG = Config.getTag(CameraFragment.class);
    private CameraToolKit mToolKit;
    private ModuleManager mModuleManager;
    private AppBaseUI mBaseUI;
    private CameraSettings mSettings;
    private Context mAppContext;
    private View mRootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppContext = getActivity().getApplicationContext();
        mToolKit = new CameraToolKit(mAppContext);
        mRootView = LayoutInflater.from(getActivity()).inflate(R.layout.camera_fragment_layout, null);
        mBaseUI = new AppBaseUI(mAppContext, mRootView);
        mModuleManager = new ModuleManager(mAppContext, mController);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mModuleManager.getCurrentModule() == null) {
            Log.d(TAG, "init module");
            updateThumbnail(mAppContext);
            CameraModule cameraModule = mModuleManager.getNewModule();
            cameraModule.init(mAppContext, mController);
        }
        mModuleManager.getCurrentModule().startModule();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mModuleManager.getCurrentModule() != null) {
            mModuleManager.getCurrentModule().stopModule();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mToolKit.destroy();
    }

    public Controller getController() {
        return mController;
    }


    private Controller mController = new Controller() {
        @Override
        public void changeModule(int index) {
            if (mModuleManager.needChangeModule(index)) {
                mModuleManager.getCurrentModule().stopModule();
                CameraModule module = mModuleManager.getNewModule();
                module.init(mAppContext, this);
                module.startModule();
            }
        }

        @Override
        public CameraToolKit getToolKit() {
            return mToolKit;
        }

        @Override
        public void showSetting() {
            getCameraActivity().addSettingFragment();
        }

        @Override
        public CameraSettings getCameraSettings(Context context) {
            if (mSettings == null) {
                mSettings = new CameraSettings(context);
            }
            return mSettings;
        }

        @Override
        public AppBaseUI getBaseUI() {
            return mBaseUI;
        }
    };

    private CameraActivity getCameraActivity() {
        if (getActivity() instanceof CameraActivity) {
            return (CameraActivity) getActivity();
        } else {
            throw new RuntimeException("CameraFragment must add to CameraActivity");
        }
    }

    private void updateThumbnail(final Context context) {
        mToolKit.getExecutor().execute(new JobExecutor.Task<Void>() {
            @Override
            public Void run() {
                mBaseUI.updateThumbnail(context, mToolKit.getMainHandler());
                return super.run();
            }
        });
    }
}
