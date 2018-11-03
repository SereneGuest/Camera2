package com.smewise.camera2.utils;

import com.smewise.camera2.Config;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by wenzhe on 9/6/17.
 */

public class Storage {
    private static final String TAG = Config.getTag(Storage.class);

    public static void writeFile(String path, byte[] data) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(data);
        } catch (Exception e) {
            Log.e(TAG, "Failed to write data", e);
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                Log.e(TAG, "Failed to close file after write", e);
            }
        }
    }

    public static void writeFile(File file, byte[] data) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data);
        } catch (Exception e) {
            Log.e(TAG, "Failed to write data", e);
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                Log.e(TAG, "Failed to close file after write", e);
            }
        }
    }

    // Add the image to media store.
    public static Uri addImageToDB(ContentResolver resolver, String title, long date,
            Location location, int orientation, long jpegLength,
            String path, int width, int height, String mimeType) {
        // Insert into MediaStore.
        ContentValues values = new ContentValues(11);
        values.put(MediaStore.Images.ImageColumns.TITLE, title);
        values.put(MediaStore.MediaColumns.WIDTH, width);
        values.put(MediaStore.MediaColumns.HEIGHT, height);
        if (mimeType.equalsIgnoreCase("jpeg")
                || mimeType.equalsIgnoreCase("image/jpeg")) {
            values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, title + ".jpg");
        } else {
            values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, title + ".raw");
        }
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, date);
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, orientation);
        values.put(MediaStore.Images.ImageColumns.DATA, path);
        values.put(MediaStore.Images.ImageColumns.SIZE, jpegLength);
        if (location != null) {
            values.put(MediaStore.Images.ImageColumns.LATITUDE, location.getLatitude());
            values.put(MediaStore.Images.ImageColumns.LONGITUDE, location.getLongitude());
        }
        return insert(resolver, values, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    // Add the video to media store.
    public static Uri addVideoToDB(ContentResolver resolver, String title, long date,
                                   Location location, long length, String path,
                                   int width, int height, String mimeType) {
        // Insert into MediaStore.
        ContentValues values = new ContentValues(10);
        values.put(MediaStore.Video.VideoColumns.TITLE, title);
        values.put(MediaStore.MediaColumns.WIDTH, width);
        values.put(MediaStore.MediaColumns.HEIGHT, height);
        values.put(MediaStore.Video.VideoColumns.DISPLAY_NAME, title + ".mp4");
        values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, date);
        values.put(MediaStore.Video.VideoColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.Video.VideoColumns.DATA, path);
        values.put(MediaStore.Video.VideoColumns.SIZE, length);
        if (location != null) {
            values.put(MediaStore.Video.VideoColumns.LATITUDE, location.getLatitude());
            values.put(MediaStore.Video.VideoColumns.LONGITUDE, location.getLongitude());
        }
        return insert(resolver, values, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
    }

    private static Uri insert(ContentResolver resolver, ContentValues values, Uri targetUri) {
        Uri uri = null;
        try {
            uri = resolver.insert(targetUri, values);
        } catch (Throwable th) {
            Log.e(TAG, "Failed to write MediaStore:" + th);
        }
        return uri;
    }
}
