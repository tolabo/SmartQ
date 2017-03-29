package com.vaibhav.smartq.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.vaibhav.smartq.MyVariables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by vaibhav on 10/22/2016.
 */

public class MyFileOperations {
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageReadWriteAccessible() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (isExternalStorageReadable() && isExternalStorageWritable()) {
                return true;
            }
        }
        return false;
    }


    public static boolean writeqrCodetoFile(File fileOnStorage, Bitmap qrBitmap) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileOnStorage);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("QRAPP", "Problem saving bitmap to png");
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public static Uri generateUriForQrBitmap(Bitmap qrBitmap) {
        Uri uri = null;
        if (qrBitmap == null) {
            Log.d(MyVariables.TAG, "QR Code is null");
            return null;
        }

        if (isExternalStorageReadWriteAccessible()) {
            File qrFileDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "SmartQImages");
            if (!qrFileDir.exists()) {
                qrFileDir.mkdir();
            }
            File qrFile = new File(qrFileDir, "qrImage.png");
            if (qrFile != null) {
                if (!writeqrCodetoFile(qrFile, qrBitmap)) {
                    Log.d(MyVariables.TAG, "Failed to save bitmap to file");
                    return null;
                }
                uri = Uri.fromFile(qrFile);
            }
        }else{
            Log.d(MyVariables.TAG, "External Storage Inaccessible");
        }
        return uri;
    }
}
