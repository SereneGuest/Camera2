package com.smewise.camera2.utils;

import android.content.ContentResolver;
import android.graphics.ImageFormat;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Handler;
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
    private SaveThread mSaveThread;
    private List<ImageInfo> imageInfos = new LinkedList<>();

    public interface FileListener {
        void onFileSaved(Uri uri, String path);
    }

    private class ImageInfo {
        public byte[] imgData;
        public int imgWidth;
        public int imgHeight;
        public int imgOrientation;
        public long imgDate;
        public Location imgLocation = null;
        public String imgTitle;
        public String imgPath;
        public String imgMimeType;
    }

    public FileSaver(ContentResolver resolver, Handler handler) {
        mHandler = handler;
        mResolver = resolver;
        mSaveThread = new SaveThread();
        mSaveThread.start();
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
        imageInfos.add(info);

        mSaveThread.notifyDirty();
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
        imageInfos.add(info);

        mSaveThread.notifyDirty();
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
        Storage.writeFile(info.imgPath, info.imgData);
        final Uri uri = Storage.addImageToDB(mResolver, info.imgTitle, info.imgDate,
                info.imgLocation, info.imgOrientation, info.imgData.length, info.imgPath,
                info.imgWidth, info.imgHeight, info.imgMimeType);
        if (mListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onFileSaved(uri, info.imgPath);
                }
            });
        }
    }

    public void release() {
        if (mSaveThread != null) {
            mSaveThread.terminate();
            try {
                mSaveThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mSaveThread = null;
        }
        imageInfos.clear();
    }

    private class SaveThread extends Thread {
        private volatile boolean mIsActive = true;
        private volatile boolean mDirty = false;

        @Override
        public void run() {
            super.run();
            while (mIsActive) {
                synchronized (this) {
                    if (mIsActive && !mDirty) {
                        Log.d(TAG, "save thread wait");
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
                //save file
                if (imageInfos.isEmpty()) {
                    Log.d(TAG, " list empty");
                    mDirty = false;
                } else {
                    Log.d(TAG, " save file");
                    startSaveProcess(imageInfos.get(0));
                    imageInfos.remove(0);
                }
            }
        }

        public synchronized void notifyDirty() {
            mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            mIsActive = false;
            notifyAll();
        }
    }

}
