package com.smewise.camera2;

import android.graphics.ImageFormat;
import android.util.Size;

/**
 * Created by wenzhe on 3/17/17.
 */

public class Config {
    // some default config, not actually
    public static final String MAIN_ID = "0";
    public static final String AUX_ID = "1";
    private static final String TAG_PREFIX = "wenzhe/";
    public static final float AUX_PREVIEW_SCALE = 0.3F;
    public static final String IMAGE_FORMAT = String.valueOf(ImageFormat.JPEG);
    public static final String NULL_VALUE = "SharedPreference No Value";
    public static final int THUMB_SIZE = 128;

    public static String getTag(Class<?> cls) {
        return TAG_PREFIX + cls.getSimpleName();
    }

    public static boolean ratioMatched(Size size) {
        return size.getWidth() * 3 == size.getHeight() * 4;
    }

    public static boolean videoRatioMatched(Size size) {
        return size.getWidth() * 9 == size.getHeight() * 16;
    }
}
