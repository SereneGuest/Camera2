package com.smewise.camera2.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.smewise.camera2.R;


/**
 * Created by wenzhe on 16-3-25.
 */
public class Permission {

    public static final int REQUEST_CODE = 5;

    private static final String[] permission = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };

    public static boolean checkPermission(Activity activity) {
        if (isPermissionGranted(activity)) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity, permission, REQUEST_CODE);
            return false;
        }
    }

    public static boolean isPermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            for (int i = 0; i < permission.length; i++) {
                int checkPermission = ContextCompat.checkSelfPermission(activity, permission[i]);
                if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    public static void showPermissionDenyDialog(final Activity activity) {
        //setting detail intent
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.permission_tip));
        builder.setMessage(activity.getString(R.string.permission_msg));
        builder.setPositiveButton(activity.getString(R.string.permission_btn_setting),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(intent);
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(activity.getString(R.string.permission_btn_quit),
                new DialogInterface.OnClickListener() {
                @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(false);
        builder.create().show();
    }


}
