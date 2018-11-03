package com.smewise.camera2.utils;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
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

    private static final String TAG = Config.getTag(CameraUtil.class);

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

    /**
     * get default picture size from support picture size list
     * @param map specific stream configuration map used for get supported picture size
     * @param format used for get supported picture size list from map
     * @return largest 4:3 picture size or largest size in supported list
     */
    public static Size getDefaultPictureSize(StreamConfigurationMap map, int format) {
        Size[] supportSize = map.getOutputSizes(format);
        sortCamera2Size(supportSize);
        for (Size size : supportSize) {
            if (Config.ratioMatched(size)) {
                return size;
            }
        }
        return supportSize[0];
    }

    /**
     * get default preview size from supported preview size list
     * @param map specific stream configuration map used for get supported preview size
     * @param displaySize devices screen size
     * @return preview size equals or less than screen size
     */
    public static Size getDefaultPreviewSize(StreamConfigurationMap map, Point displaySize) {
        Size[] supportSize = map.getOutputSizes(SurfaceTexture.class);
        sortCamera2Size(supportSize);
        for (Size size : supportSize) {
            if (!Config.ratioMatched(size)) {continue;}
            if ((size.getHeight() == displaySize.x)
                    || (size.getWidth() <= displaySize.y && size.getHeight() <= displaySize.x)) {
                return size;
            }
        }
        return supportSize[0];
    }

    public static Size getPreviewSizeByRatio(StreamConfigurationMap map, Point displaySize, double ratio) {
        Size[] supportSize = map.getOutputSizes(SurfaceTexture.class);
        sortCamera2Size(supportSize);
        for (Size size : supportSize) {
            boolean isRatioMatch = (int) (size.getHeight() * ratio) == size.getWidth();
            if (!isRatioMatch) {
                continue;
            }
            if ((size.getHeight() == displaySize.x)
                    || (size.getWidth() <= displaySize.y && size.getHeight() <= displaySize.x)) {
                return size;
            }
        }
        return supportSize[0];
    }

    /**
     * Get default video size from support video size list
     * @param map specific stream configuration map used for get supported picture size
     * @return size match screen size or largest size in supported list
     */
    public static Size getDefaultVideoSize(StreamConfigurationMap map, Point displaySize) {
        Size[] supportSize = map.getOutputSizes(MediaRecorder.class);
        sortCamera2Size(supportSize);
        for (Size size : supportSize) {
            if (Config.videoRatioMatched(size) && size.getHeight() <= displaySize.x) {
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

    public static String[] getVideoSizeList(StreamConfigurationMap map) {
        Size[] supportSize = map.getOutputSizes(MediaRecorder.class);
        sortCamera2Size(supportSize);
        String[] sizeStr = new String[supportSize.length];
        for (int i = 0; i < supportSize.length; i++) {
            sizeStr[i] = supportSize[i].getWidth() + SPLIT_TAG + supportSize[i].getHeight();
        }
        return sizeStr;
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

    public static int getDefaultPreviewSizeIndex(Context context, Size[] supportSize) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context
                .WINDOW_SERVICE);
        Point realPoint = new Point();
        windowManager.getDefaultDisplay().getRealSize(realPoint);
        for (int i = 0; i < supportSize.length; i++) {
            if (!Config.ratioMatched(supportSize[i])) {
                continue;
            }
            if ((supportSize[i].getHeight() == realPoint.x) || (supportSize[i].getWidth()
                    <= realPoint.y && supportSize[i].getHeight() <= realPoint.x)) {
                return i;
            }
        }
        return 0;
    }

    public static String[][] getOutputFormat(int[] supportFormat) {
        String[][] formatStr = new String[2][supportFormat.length];
        for (int i = 0; i < supportFormat.length; i++) {
            formatStr[0][i] = format2String(supportFormat[i]);
            formatStr[1][i] = String.valueOf(supportFormat[i]);
        }
        return formatStr;
    }

    public static String hardwareLevel2Sting(int level) {
        switch (level) {
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                return "LEVEL_FULL";
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                return "LEVEL_LEGACY";
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                return "LEVEL_3";
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                return "LEVEL_LIMITED";
            default:
                return "Unknown Level";
        }
    }

    public static String capabilities2String(int cap) {
        switch (cap) {
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE:
                return "BACKWARD_COMPATIBLE";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE:
                return "BURST_CAPTURE";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO:
                return "CONSTRAINED_HIGH_SPEED_VIDEO";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT:
                return "DEPTH_OUTPUT";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING:
                return "MANUAL_POST_PROCESSING";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR:
                return "MANUAL_SENSOR";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING:
                return "PRIVATE_REPROCESSING";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW:
                return "RAW";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS:
                return "READ_SENSOR_SETTINGS";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING:
                return "YUV_REPROCESSING";
            default:
                return "Unknown Capabilities";
        }
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
