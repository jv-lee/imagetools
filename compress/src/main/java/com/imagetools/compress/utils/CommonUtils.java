package com.imagetools.compress.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;


public class CommonUtils {

    public static Uri fileToUri(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return FileProvider.getUriForFile(context, context.getPackageName()+".compress.fileprovider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public static String uriToPath(Context context, Uri uri) {
        String imagePath;
        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
        try {
            if (cursor == null) {
                imagePath = uri.getPath();
            } else {
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                imagePath = cursor.getString(index);
                cursor.close();
            }
        } catch (Exception e) {
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    public static String getImageFilePath(Context context) {
        //设置文件路径创建文件对象
        File fileDir = new File(context.getCacheDir(), Constants.COMPRESS_CACHE);
        if (!fileDir.exists()) fileDir.mkdir();
        File file = new File(fileDir.getAbsolutePath(), System.currentTimeMillis() + ".jpg");

        //文件创建操作
        if (!file.getParentFile().exists()) file.getParentFile().mkdir();

        return file.getAbsolutePath();
    }

}
