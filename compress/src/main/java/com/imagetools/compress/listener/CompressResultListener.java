package com.imagetools.compress.listener;

import android.net.Uri;

/**
 * 单张图片压缩监听
 *
 * @author jv.lee
 */
public interface CompressResultListener {
    /**
     * 成功
     *
     * @param imgUri
     */
    void onCompressSuccess(Uri imgUri);

    /**
     * 失败
     *
     * @param imgUri
     * @param error
     */
    void onCompressFailed(Uri imgUri, String error);

    void onCompressProgress(int progress);
}
