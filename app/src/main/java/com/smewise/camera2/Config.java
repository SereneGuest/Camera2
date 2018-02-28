package com.smewise.camera2;

import android.graphics.ImageFormat;
import android.util.Size;

import com.smewise.camera2.utils.CameraUtil;

/**
 * Created by wenzhe on 3/17/17.
 */

public class Config {
    // some default config, not actually
    public static final String MAIN_ID = "0";
    public static final String AUX_ID = "2";
    private static final String TAG_PREFIX = "wenzhe/";
    public static final float AUX_PREVIEW_SCALE = 0.3F;
    public static final int IMAGE_FORMAT = ImageFormat.JPEG;
    public static final boolean CLOSE_CAMERA_ASYNC = true;
    public static final double DEFAULT_RATIO = CameraUtil.RATIO_4X3;
    public static final String NULL_VALUE = "SharedPreference No Value";

    public static String getTag(Class<?> cls) {
        return TAG_PREFIX + cls.getSimpleName();
    }

    public static boolean ratioMatched(Size size) {
        return size.getWidth() * 3 == size.getHeight() * 4;
    }
}
