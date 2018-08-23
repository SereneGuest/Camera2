package com.smewise.camera2.utils;

import com.smewise.camera2.R;

public class SupportInfoDialog extends CameraDialog {

    private String mMessage;

    public void setMessage(String msg) {
        mMessage = msg;
    }

    @Override
    String getTitle() {
        return getResources().getString(R.string.support_info_title);
    }

    @Override
    String getMessage() {
        return mMessage;
    }

    @Override
    String getOKButtonMsg() {
        return getResources().getString(R.string.support_info_done);
    }

    @Override
    String getNoButtonMsg() {
        return null;
    }

    @Override
    void onButtonClick(int which) {
        dismiss();
    }
}
