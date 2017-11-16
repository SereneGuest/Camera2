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
    private static final String TAG = Config.TAG_PREFIX + "Storage";

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
            Location location, int orientation, int jpegLength,
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
        return insertImage(resolver, values);
    }

    private static Uri insertImage(ContentResolver resolver, ContentValues values) {
        Uri uri = null;
        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Throwable th) {
            // This can happen when the external volume is already mounted, but
            // MediaScanner has not notify MediaProvider to add that volume.
            // The picture is still safe and MediaScanner will find it and
            // insert it into MediaProvider. The only problem is that the user
            // cannot click the thumbnail to review the picture.
            Log.e(TAG, "Failed to write MediaStore" + th);
        }
        return uri;
    }
}
