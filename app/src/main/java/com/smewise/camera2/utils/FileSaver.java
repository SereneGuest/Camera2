package com.smewise.camera2.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.smewise.camera2.Config;
import com.smewise.camera2.R;
import com.smewise.camera2.exif.ExifInterface;

import java.io.File;
import java.io.IOException;

/**
 * Created by wenzhe on 9/6/17.
 */

public class FileSaver {

    private static final String TAG = Config.getTag(FileSaver.class);

    private final String JPEG = "image/jpeg";
    private final String VIDEO = "video/mpeg";
    private final String YUV = "image/yuv";

    private ContentResolver mResolver;
    private Context mContext;
    private FileListener mListener;
    private Handler mHandler;

    public interface FileListener {
        void onFileSaved(Uri uri, String path, @Nullable Bitmap thumbnail);

        void onFileSaveError(String msg);
    }

    private class ImageInfo {
        byte[] imgData;
        int imgWidth;
        int imgHeight;
        int imgOrientation;
        long imgDate;
        Location imgLocation;
        String imgTitle;
        String imgPath;
        String imgMimeType;
    }

    public FileSaver(Context context, Handler handler) {
        mHandler = handler;
        mContext = context;
        mResolver = context.getContentResolver();
    }

    public void setFileListener(FileListener listener) {
        mListener = listener;
    }

    public void saveFile(int width, int height, int orientation, byte[] data, String tag,
            int saveType) {
        File file = MediaFunc.getOutputMediaFile(saveType, tag);
        if (file == null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onFileSaveError("can not create file or directory");
                }
            });
            return;
        }
        ImageInfo info = new ImageInfo();
        info.imgWidth = width;
        info.imgHeight = height;
        info.imgData = data;
        info.imgOrientation = 0;
        info.imgDate = System.currentTimeMillis();
        info.imgPath = file.getPath();
        info.imgTitle = file.getName();
        info.imgMimeType = getMimeType(saveType);
        if (saveType == MediaFunc.MEDIA_TYPE_YUV) {
            saveYuvFile(info);
        } else {
            saveJpegFile(info);
        }
    }

    public void saveVideoFile(int width, int height, int orientation,
                              final String path, int type) {
        if (orientation % 180 == 0) {
            width = width + height;
            height = width - height;
            width = width - height;
        }
        File file = new File(path);
        final Uri uri = Storage.addVideoToDB(mResolver, file.getName(),
                System.currentTimeMillis(), null, file.length(), path,
                width, height, getMimeType(type));
        if (mListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onFileSaved(uri, path, null);
                }
            });
        }
    }

    private void saveJpegFile(final ImageInfo info) {
        try {
            ExifInterface exif = new ExifInterface();
            exif.readExif(info.imgData);
            final Bitmap thumbnail = rotateAndWriteJpegData(exif, info);
            final Uri uri = Storage.addImageToDB(mResolver, info.imgTitle, info.imgDate,
                    info.imgLocation, info.imgOrientation, info.imgData.length, info.imgPath,
                    info.imgWidth, info.imgHeight, info.imgMimeType);
            if (mListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onFileSaved(uri, info.imgPath, thumbnail);
                    }
                });
            }
        } catch (IOException e) {
            Log.e(TAG, " error get exif msg:" + e.getMessage());
        }
    }

    private void saveYuvFile(final ImageInfo info) {
        Storage.writeFile(info.imgPath, info.imgData);
        final Uri uri = Storage.addImageToDB(mResolver, info.imgTitle, info.imgDate,
                info.imgLocation, info.imgOrientation, info.imgData.length, info.imgPath,
                info.imgWidth, info.imgHeight, info.imgMimeType);
        final Bitmap thumbnail = BitmapFactory.decodeResource(
                mContext.getResources(), R.mipmap.yuv_file);
        if (mListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onFileSaved(uri, info.imgPath, thumbnail);
                }
            });
        }
    }

    private String getMimeType(int type) {
        if (type == MediaFunc.MEDIA_TYPE_IMAGE) {
            return JPEG;
        } else if (type == MediaFunc.MEDIA_TYPE_VIDEO) {
            return VIDEO;
        } else {
            return YUV;
        }
    }

    private Bitmap rotateAndWriteJpegData(ExifInterface exif, ImageInfo info) {
        int orientation = ExifInterface.Orientation.TOP_LEFT;
        int oriW = info.imgWidth;
        int oriH = info.imgHeight;
        try {
            orientation = exif.getTagIntValue(ExifInterface.TAG_ORIENTATION);
            oriW = exif.getTagIntValue(ExifInterface.TAG_IMAGE_WIDTH);
            oriH = exif.getTagIntValue(ExifInterface.TAG_IMAGE_LENGTH);
        } catch (Exception e) {
            // getTagIntValue() may cause NullPointerException
            e.printStackTrace();
        }
        // no need rotate, just save and return
        if (orientation == ExifInterface.Orientation.TOP_LEFT) {
            // use exif width & height
            info.imgWidth = oriW;
            info.imgHeight = oriH;
            Storage.writeFile(info.imgPath, info.imgData);
            return getThumbnail(info);
        }
        if (orientation <= 0) {
            Log.e(TAG, "invalid orientation value:" + orientation);
        }
        Matrix matrix = new Matrix();
        switch (orientation) {
            //case ExifInterface.Orientation.TOP_LEFT:
                // do nothing, just save jpeg data
            //    break;
            case ExifInterface.Orientation.TOP_RIGHT:
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.Orientation.BOTTOM_LEFT:
                matrix.postRotate(180);
                break;
            case ExifInterface.Orientation.BOTTOM_RIGHT:
                matrix.postScale(1, -1);
                break;
            case ExifInterface.Orientation.LEFT_TOP:
                matrix.postScale(1, -1);
                matrix.postRotate(90);
                // swap width and height
                exif.setTagValue(ExifInterface.TAG_IMAGE_WIDTH, oriH);
                exif.setTagValue(ExifInterface.TAG_IMAGE_LENGTH, oriW);
                break;
            case ExifInterface.Orientation.RIGHT_TOP:
                matrix.postRotate(90);
                exif.setTagValue(ExifInterface.TAG_IMAGE_WIDTH, oriH);
                exif.setTagValue(ExifInterface.TAG_IMAGE_LENGTH, oriW);
                break;
            case ExifInterface.Orientation.LEFT_BOTTOM:
                matrix.postScale(-1, 1);
                matrix.postRotate(90);
                exif.setTagValue(ExifInterface.TAG_IMAGE_WIDTH, oriH);
                exif.setTagValue(ExifInterface.TAG_IMAGE_LENGTH, oriW);
                break;
            case ExifInterface.Orientation.RIGHT_BOTTOM:
                matrix.postRotate(270);
                exif.setTagValue(ExifInterface.TAG_IMAGE_WIDTH, oriH);
                exif.setTagValue(ExifInterface.TAG_IMAGE_LENGTH, oriW);
                break;
            default:
                Log.e(TAG, "exif orientation error value:" + orientation);
                break;
        }
        // jpeg rotated, set orientation to normal
        exif.setTagValue(ExifInterface.TAG_ORIENTATION, ExifInterface.Orientation.TOP_LEFT);
        try {
            // use exif width & height
            info.imgWidth = exif.getTagIntValue(ExifInterface.TAG_IMAGE_WIDTH);
            info.imgHeight = exif.getTagIntValue(ExifInterface.TAG_IMAGE_LENGTH);;
        } catch (Exception e) {
            // getTagIntValue() may cause NullPointerException
            e.printStackTrace();
        }
        Bitmap origin = BitmapFactory.decodeByteArray(info.imgData, 0, info.imgData.length);
        Bitmap rotatedMap = Bitmap.createBitmap(origin,
                0, 0, origin.getWidth(), origin.getHeight(), matrix, true);
        Bitmap thumb = getThumbnail(rotatedMap);
        try {
            exif.writeExif(rotatedMap, info.imgPath, 90);
        } catch (IOException e) {
            Log.e(TAG, "write file failed msg:" + e.getMessage());
        } finally {
            origin.recycle();
            rotatedMap.recycle();
        }
        return thumb;
    }

    private Bitmap getThumbnail(ImageInfo info) {
        if (JPEG.equals(info.imgMimeType)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = info.imgWidth / Config.THUMB_SIZE;
            return BitmapFactory.decodeByteArray(
                    info.imgData, 0, info.imgData.length, options);
        } else {
            return null;
        }
    }

    private Bitmap getThumbnail(Bitmap origin) {
        int height = origin.getHeight() / (origin.getWidth() / Config.THUMB_SIZE);
        return Bitmap.createScaledBitmap(origin, Config.THUMB_SIZE, height, true);
    }

    public void release() {
        mListener = null;
        mContext = null;
    }
}
