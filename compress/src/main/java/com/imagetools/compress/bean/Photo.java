package com.imagetools.compress.bean;

import android.net.Uri;

import java.io.Serializable;

/**
 * @author jv.lee
 */
public class Photo implements Serializable {
    /**
     * 图片原始路径
     */
    private Uri originalUri;
    /**
     * 图片是否压缩过
     */
    private boolean compressed;
    /**
     * 图片压缩后路径
     */
    private Uri compressUri;


    public Uri getOriginalUri() {
        return originalUri;
    }

    public void setOriginalUri(Uri originalUri) {
        this.originalUri = originalUri;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public Uri getCompressUri() {
        return compressUri;
    }

    public void setCompressUri(Uri compressUri) {
        this.compressUri = compressUri;
    }

    public Photo(Uri originalPath) {
        this.originalUri = originalPath;
    }
}
