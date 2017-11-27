package com.smewise.camera2.utils;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.WindowManager;

import com.smewise.camera2.Config;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by wenzhe on 12/15/16.
 */

public class CameraUtil {

    private static final String TAG = Config.TAG_PREFIX + "CameraUtil";

    public static double RATIO_4X3 = 1.333333333;
    public static double RATIO_16X9 = 1.777777778;
    public static double ASPECT_TOLERANCE = 0.00001;
    public static final String SPLIT_TAG = "x";

    private static void sortCamera2Size(Size[] sizes) {
        Comparator<Size> comparator = new Comparator<Size>() {
            @Override
            public int compare(Size o1, Size o2) {
                return o2.getWidth() * o2.getHeight() - o1.getWidth() * o1.getHeight();
            }
        };
        Arrays.sort(sizes, comparator);
    }

    public static Size getPictureSize(StreamConfigurationMap map, double ratio, int format) {
        Size[] supportSize = map.getOutputSizes(format);
        sortCamera2Size(supportSize);
        for (Size size : supportSize) {
            double tmp = size.getWidth() / (double)size.getHeight() - ratio;
            if (Math.abs(tmp) < ASPECT_TOLERANCE) {
                return size;
            }
        }
        return supportSize[0];
    }

    /* size format is "width x height"*/
    public static String[] getPictureSizeList(StreamConfigurationMap map, int format) {
        Size[] supportSize = map.getOutputSizes(format);
        sortCamera2Size(supportSize);
        String[] sizeStr = new String[supportSize.length];
        for (int i = 0; i < supportSize.length; i++) {
            sizeStr[i] = supportSize[i].getWidth() + SPLIT_TAG + supportSize[i].getHeight();
        }
        return sizeStr;
    }

    public static String[] getPreviewSizeList(StreamConfigurationMap map) {
        Size[] supportSize = map.getOutputSizes(SurfaceTexture.class);
        sortCamera2Size(supportSize);
        String[] sizeStr = new String[supportSize.length];
        for (int i = 0; i < supportSize.length; i++) {
            sizeStr[i] = supportSize[i].getWidth() + SPLIT_TAG + supportSize[i].getHeight();
        }
        return sizeStr;
    }

    public static Size getPreviewSize(StreamConfigurationMap map, double ratio) {
        Size[] supportSize = map.getOutputSizes(SurfaceTexture.class);
        sortCamera2Size(supportSize);
        for (Size size : supportSize) {
            double tmp = size.getWidth() / (double)size.getHeight() - ratio;
            if (Math.abs(tmp) < ASPECT_TOLERANCE) {
                return size;
            }
        }
        return supportSize[0];
    }

    public static Size getPreviewUiSize(Context context, Size previewSize) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context
                .WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        double ratio = previewSize.getWidth() / (double) previewSize.getHeight();
        int w = (int) Math.ceil(metrics.widthPixels * ratio);
        int h = metrics.widthPixels;
        return new Size(w, h);
    }

    public static int getJpgRotation(CameraCharacteristics c, int deviceRotation) {
        int result ;
        Integer sensorRotation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);
        Integer lensFace = c.get(CameraCharacteristics.LENS_FACING);
        if (sensorRotation == null || lensFace == null) {
            Log.e(TAG, "can not get sensor rotation or lens face");
            return deviceRotation;
        }
        if (lensFace == CameraCharacteristics.LENS_FACING_BACK) {
            result = (sensorRotation + deviceRotation) % 360;
        } else {
            result = (sensorRotation - deviceRotation + 360) % 360;
        }
        return result;
    }

    public static Point getDisplaySize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context
                .WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        return point;
    }

    public static int getVirtualKeyHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context
                .WINDOW_SERVICE);
        Point defaultPoint = new Point();
        Point realPoint = new Point();
        windowManager.getDefaultDisplay().getSize(defaultPoint);
        windowManager.getDefaultDisplay().getRealSize(realPoint);
        return realPoint.y - defaultPoint.y;
    }

    public static int getBottomBarHeight(int screenWidth) {
        return (int) (screenWidth * (RATIO_16X9 - RATIO_4X3));
    }

    public static String[][] getOutputFormat(int[] supportFormat) {
        String[][] formatStr = new String[2][supportFormat.length];
        for (int i = 0; i < supportFormat.length; i++) {
            formatStr[0][i] = format2String(supportFormat[i]);
            formatStr[1][i] = String.valueOf(supportFormat[i]);
        }
        return formatStr;
    }

    public static String format2String(int format) {
        switch (format) {
            case ImageFormat.RGB_565:
                return "RGB_565";
            case ImageFormat.NV16:
                return "NV16";
            case ImageFormat.YUY2:
                return "YUY2";
            case ImageFormat.YV12:
                return "YV12";
            case ImageFormat.JPEG:
                return "JPEG";
            case ImageFormat.NV21:
                return "NV21";
            case ImageFormat.YUV_420_888:
                return "YUV_420_888";
            case ImageFormat.YUV_422_888:
                return "YUV_422_888";
            case ImageFormat.YUV_444_888:
                return "YUV_444_888";
            case ImageFormat.FLEX_RGB_888:
                return "FLEX_RGB_888";
            case ImageFormat.FLEX_RGBA_8888:
                return "FLEX_RGBA_8888";
            case ImageFormat.RAW_SENSOR:
                return "RAW_SENSOR";
            case ImageFormat.RAW_PRIVATE:
                return "RAW_PRIVATE";
            case ImageFormat.RAW10:
                return "RAW10";
            case ImageFormat.RAW12:
                return "RAW12";
            case ImageFormat.DEPTH16:
                return "DEPTH16";
            case ImageFormat.DEPTH_POINT_CLOUD:
                return "DEPTH_POINT_CLOUD";
            case ImageFormat.PRIVATE:
                return "PRIVATE";
            default:
                return "ERROR FORMAT";
        }
    }

}
