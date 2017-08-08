package com.ai.base.okHttp.callback;

/**
 * Created by dusk on 2017/8/7.
 */


/**
 * 下载监听接口
 */
public interface OnDownloadListener {
    /**
     * 下载成功
     */
    void onDownloadSucceed();

    /**
     * @param progress 下载进度
     */
    void onDownloading(int progress);

    /**
     * 下载失败
     */
    void onDownloadFailed();
}

