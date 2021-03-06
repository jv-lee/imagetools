package com.imagetools.compress.listener;


import com.imagetools.compress.bean.Photo;

import java.util.ArrayList;

/**
 * 图片集合的压缩监听
 *
 * @author jv.lee
 */
public interface CompressImage {
    /**
     * 开始压缩
     */
    void compress();

    interface CompressListener {
        /**
         * 成功
         *
         * @param images
         */
        void onCompressSuccess(ArrayList<Photo> images);

        /**
         * 失败
         *
         * @param images
         * @param error
         */
        void onCompressFailed(ArrayList<Photo> images, String error);

        void onCompressProgress(int progress);
    }
}
