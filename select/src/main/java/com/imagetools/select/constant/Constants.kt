package com.imagetools.select.constant

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal object Constants {
    //默认相册ID - 使用该ID获取所有照片数据
    const val DEFAULT_ALBUM_ID = -1L

    //intent 透传图片选择器配置key
    const val CONFIG_KEY = "config_key"

    //intent 透传图片数据 key
    const val IMAGE_DATA_KEY = "image_data"

    //intent activityResult 回传数据成功code码
    const val IMAGE_DATA_RESULT_CODE = 0x30

    //系统数据库查询图片分页count
    const val PAGE_COUNT = 200
}