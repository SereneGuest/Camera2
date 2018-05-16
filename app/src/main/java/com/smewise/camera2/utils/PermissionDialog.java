package com.smewise.camera2.utils;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.smewise.camera2.R;

public class PermissionDialog extends CameraDialog {
    @Override
    String getTitle() {
        return getResources().getString(R.string.permission_tip);
    }

    @Override
    String getMessage() {
        return getResources().getString(R.string.permission_msg);
    }

    @Override
    String getOKButtonMsg() {
        return getResources().getString(R.string.permission_btn_setting);
    }

    @Override
    String getNoButtonMsg() {
        return getResources().getString(R.string.permission_btn_quit);
    }

    @Override
    void onButtonClick(int which) {
        dismiss();
        if (which == DialogInterface.BUTTON_POSITIVE) {
            //setting detail intent
            final Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } else {
            getActivity().finish();
        }
    }
}
