package com.imagetools.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import com.imagetools.app.base.BaseActivity
import com.imagetools.select.ui.fragment.ImagePagerFragment

/**
 * @author jv.lee
 * @date 2021/1/5
 * @description
 */
class ImagePagerActivity : BaseActivity(R.layout.activity_image_pager) {

    companion object {
        fun startActivity(
            activity: FragmentActivity, view: View,
            position: Int,
            transitionName: String,
            size: Int,
            data: ArrayList<String>
        ) {
            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    view,
                    transitionName
                )
            activity.startActivity(
                Intent(activity, ImagePagerActivity::class.java)
                    .putExtra(ImagePagerFragment.KEY_POSITION, position)
                    .putExtra(ImagePagerFragment.KEY_TRANSITION_NAME, transitionName)
                    .putExtra(ImagePagerFragment.KEY_SIZE, size)
                    .putExtra(ImagePagerFragment.KEY_DATA, data)
                , optionsCompat.toBundle()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frame_container,
                ImagePagerFragment.newInstance(
                    intent.getIntExtra(ImagePagerFragment.KEY_POSITION, 0),
                    intent.getStringExtra(ImagePagerFragment.KEY_TRANSITION_NAME) ?: "",
                    intent.getIntExtra(ImagePagerFragment.KEY_SIZE, 0),
                    intent.getSerializableExtra(ImagePagerFragment.KEY_DATA) as ArrayList<String>
                )
            )
            .commit()

    }

}