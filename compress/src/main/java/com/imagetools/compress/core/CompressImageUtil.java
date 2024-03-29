package com.imagetools.compress.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.imagetools.compress.config.CompressConfig;
import com.imagetools.compress.listener.CompressResultListener;
import com.imagetools.compress.utils.CommonUtils;
import com.imagetools.compress.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 压缩图片
 *
 * @author jv.lee
 */
public class CompressImageUtil {
    private final CompressConfig config;
    private final Context context;
    private final Handler mHandler = new Handler();

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3,
            Integer.MAX_VALUE,
            15,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(5),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r);
                }
            });

    public CompressImageUtil(Context context, CompressConfig config) {
        this.context = context.getApplicationContext();
        this.config = config == null ? CompressConfig.getDefaultConfig() : config;
    }

    public void compress(Uri imageUri, CompressResultListener listener) {
        if (config.isEnablePixelCompress()) {
            try {
                // 启用像素压缩
                compressImageByPixel(imageUri, listener);
            } catch (Exception e) {
                listener.onCompressFailed(imageUri, String.format("Picture compress failed, %s", e.toString()));
                e.printStackTrace();
            }
        } else {
            // 质量压缩
            try {
                compressImageByQuality(BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri)), imageUri, listener);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                listener.onCompressFailed(imageUri, String.format("Picture compress failed %s", e.toString()));
            }
        }
    }

    /**
     * 质量压缩
     * 多线程压缩图片的质量
     */
    private void compressImageByQuality(final Bitmap bitmap, final Uri imgUri, final CompressResultListener listener) {
        if (bitmap == null) {
            sendMsg(false, imgUri, "quality compress failed,bitmap is null.", listener);
            return;
        }
        // 开启多线程进行压缩处理
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int options = 100;
                // 质量压缩方法，把压缩后的数据存放到baos中 (100表示不压缩，0表示压缩到最小)
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                // 循环判断如果压缩后图片是否大于指定大小,大于继续压缩
                while (baos.toByteArray().length > config.getMaxSize()) {
                    // 重置baos即让下一次的写入覆盖之前的内容
                    baos.reset();
                    // 图片质量每次减少5
                    options -= 5;
                    // 如果图片质量小于5，为保证压缩后的图片质量，图片最底压缩质量为5
                    if (options <= 5) {
                        options = 5;
                    }
                    // 将压缩后的图片保存到baos中
                    bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                    // 如果图片的质量已降到最低则，不再进行压缩
                    if (options == 5) {
                        break;
                    }
                }
                try {
                    File thumbnailFile = getThumbnailFile(new File(CommonUtils.getImageFilePath(context)));
                    // 将压缩后的图片保存的本地上指定路径中
                    FileOutputStream fos = new FileOutputStream(thumbnailFile);
                    fos.write(baos.toByteArray());
                    fos.flush();
                    fos.close();
                    baos.flush();
                    baos.close();
                    bitmap.recycle();
                    sendMsg(true, CommonUtils.fileToUri(context,thumbnailFile), null, listener);
                    sendProgress(1, listener);

                } catch (Exception e) {
                    sendMsg(false, imgUri, "quality compress failed,bitmap is null.", listener);
                    e.printStackTrace();
                }
            }
        };
        threadPoolExecutor.execute(runnable);
    }

    /**
     * 像素压缩
     * 按比例缩小图片的像素以达到压缩的目的
     */
    private void compressImageByPixel(Uri imgUri, CompressResultListener listener) throws FileNotFoundException {
        if (imgUri == null || TextUtils.isEmpty(imgUri.getPath())) {
            sendMsg(false, null, "is compress picture is not extends.", listener);
            return;
        }
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 只读边,不读内容
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imgUri), null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int width = newOpts.outWidth;
        int height = newOpts.outHeight;
        float maxSize = config.getMaxPixel();
        int be = 1;
        // 缩放比,用高或者宽其中较大的一个数据进行计算
        if (width >= height && width > maxSize) {
            be = (int) (newOpts.outWidth / maxSize);
            be++;
        } else if (width < height && height > maxSize) {
            be = (int) (newOpts.outHeight / maxSize);
            be++;
        }
        if (width <= config.getUnCompressNormalPixel() || height <= config.getUnCompressNormalPixel()) {
            be = 2;
            if (width <= config.getUnCompressMinPixel() || height <= config.getUnCompressMinPixel()) {
                be = 1;
            }
        }
        // 设置采样率
        newOpts.inSampleSize = be;
        // 该模式是默认的,可不设
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 同时设置才会有效
        newOpts.inPurgeable = true;
        // 当系统内存不够时候图片自动被回收
        newOpts.inInputShareable = true;
        Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imgUri), null, newOpts);
        if (config.isEnableQualityCompress()) {
            // 压缩好比例大小后再进行质量压缩
            compressImageByQuality(bitmap, imgUri, listener);
        } else {
            File thumbnailFile = getThumbnailFile(new File(CommonUtils.getImageFilePath(context)));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(thumbnailFile));

            listener.onCompressSuccess(CommonUtils.fileToUri(context,thumbnailFile));
            listener.onCompressProgress(1);
        }
    }

    private File getThumbnailFile(File file) {
        if (file == null || !file.exists()) {
            return file;
        }
        return getPhotoCacheDir(file);
    }

    private File getPhotoCacheDir(File file) {
        File mCacheDir = new File(context.getCacheDir(), Constants.COMPRESS_CACHE);
        Log.e("compress >>> ", mCacheDir.getAbsolutePath());
        if (!mCacheDir.mkdirs() && (!mCacheDir.exists() || !mCacheDir.isDirectory())) {
            return file;
        } else {
            return new File(mCacheDir, "compress_" + file.getName());
        }
    }

    /**
     * 发送压缩结果的消息
     *
     * @param isSuccess 压缩是否成功
     */
    private void sendMsg(final boolean isSuccess, final Uri imageUri, final String message, final CompressResultListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isSuccess) {
                    listener.onCompressSuccess(imageUri);
                } else {
                    listener.onCompressFailed(imageUri, message);
                }
            }
        });
    }

    private void sendProgress(final int progress, final CompressResultListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onCompressProgress(progress);
            }
        });
    }

}
