package com.smewise.camera2.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public abstract class CameraDialog extends DialogFragment implements DialogInterface.OnClickListener {
    abstract String getTitle();

    abstract String getMessage();

    abstract String getOKButtonMsg();

    abstract String getNoButtonMsg();

    abstract void onButtonClick(int which);

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getTitle());
        builder.setMessage(getMessage());
        builder.setCancelable(false);
        if (getOKButtonMsg() != null) {
            builder.setPositiveButton(getOKButtonMsg(), this);
        }
        if (getNoButtonMsg() != null) {
            builder.setNegativeButton(getNoButtonMsg(), this);
        }
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        onButtonClick(which);
    }
}
