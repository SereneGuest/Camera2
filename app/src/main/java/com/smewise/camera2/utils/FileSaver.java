package com.smewise.camera2.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.location.Location;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.smewise.camera2.Config;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by wenzhe on 9/6/17.
 */

public class FileSaver {

    private static final String TAG = Config.getTag(FileSaver.class);

    private final String JPEG = "image/jpeg";
    private final String YUV = "image/yuv";

    private ContentResolver mResolver;
    private FileListener mListener;
    private Handler mHandler;

    public interface FileListener {
        void onFileSaved(Uri uri, String path, @Nullable Bitmap thumbnail);
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

    public FileSaver(ContentResolver resolver, Handler handler) {
        mHandler = handler;
        mResolver = resolver;
    }

    public void setFileListener(FileListener listener) {
        mListener = listener;
    }

    public void saveFile(int width, int height, int orientation, byte[] data, String tag,
            int format) {
        File file = MediaFunc.getOutputMediaFile(getSaveType(format), tag);
        ImageInfo info = new ImageInfo();
        info.imgData = data;
        if (orientation == 0 || orientation == 180) {
            info.imgWidth = height;
            info.imgHeight = width;
        } else {
            info.imgWidth = width;
            info.imgHeight = height;
        }
        info.imgOrientation = 0;
        info.imgDate = System.currentTimeMillis();
        info.imgPath = file.getPath();
        info.imgTitle = file.getName();
        info.imgMimeType = getMimeType(format);
        startSaveProcess(info);
    }

    public void saveFile(Image image, int orientation, String tag, int format) {
        File file = MediaFunc.getOutputMediaFile(getSaveType(format), tag);
        ImageInfo info = new ImageInfo();
        if (orientation == 0 || orientation == 180) {
            info.imgWidth = image.getHeight();
            info.imgHeight = image.getWidth();
        } else {
            info.imgWidth = image.getWidth();
            info.imgHeight = image.getHeight();
        }
        info.imgOrientation = orientation;
        info.imgDate = System.currentTimeMillis();
        info.imgPath = file.getPath();
        info.imgTitle = file.getName();
        info.imgMimeType = getMimeType(format);
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        info.imgData = new byte[buffer.remaining()];
        buffer.get(info.imgData);
        image.close();
        startSaveProcess(info);
    }

    private int getSaveType(int format) {
        if (format == ImageFormat.JPEG) {
            return MediaFunc.MEDIA_TYPE_IMAGE;
        } else {
            return MediaFunc.MEDIA_TYPE_YUV;
        }
    }

    private String getMimeType(int format) {
        if (format == ImageFormat.JPEG) {
            return JPEG;
        } else {
            return YUV;
        }
    }

    private void startSaveProcess(final ImageInfo info) {
        final Bitmap thumbnail = getThumbnail(info);
        Storage.writeFile(info.imgPath, info.imgData);
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
    }

    private Bitmap getThumbnail(ImageInfo info) {
        if (JPEG.equals(info.imgMimeType)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 12;
            return BitmapFactory.decodeByteArray(
                    info.imgData, 0, info.imgData.length, options);
        } else {
            return null;
        }

    }

    public void release() {
        mListener = null;
    }
}
