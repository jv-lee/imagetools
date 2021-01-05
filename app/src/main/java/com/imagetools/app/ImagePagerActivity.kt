package com.imagetools.app

import android.os.Bundle
import com.imagetools.app.base.BaseActivity
import com.imagetools.select.ui.fragment.ImagePagerFragment

/**
 * @author jv.lee
 * @date 2021/1/5
 * @description
 */
class ImagePagerActivity : BaseActivity(R.layout.activity_image_pager) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, ImagePagerFragment())
            .commit()
    }

}