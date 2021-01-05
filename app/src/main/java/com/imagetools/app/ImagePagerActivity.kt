package com.imagetools.app

import android.content.Intent
import android.os.Build
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

    private var isReset = false
    private var position = 0

    companion object {
        fun startActivity(activity: FragmentActivity, view: View, transitionName: String) {
            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    view,
                    transitionName
                )
            activity.startActivity(
                Intent(activity, ImagePagerActivity::class.java), optionsCompat.toBundle()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, ImagePagerFragment.newInstance())
            .commit()


//        setExitSharedElementCallback(object : SharedElementCallback() {
//            override fun onMapSharedElements(
//                names: MutableList<String>,
//                sharedElements: MutableMap<String, View>
//            ) {
//                //防止重复设置动画元素效果.
//                if (!isReset) {
//                    return
//                }
//                isReset = false
//                sharedElements.put(
//                    "transitionName",
//                    iv_image_one
//                )
//            }
//        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition.duration = 200
            window.sharedElementExitTransition.duration = 200
        }

    }

//    /**
//     * 共享元素回调设置
//     * @param resultCode 返回code
//     * @param data 返回数据 动态更改当前共享元素
//     */
//    override fun onActivityReenter(resultCode: Int, data: Intent?) {
//        if (resultCode == Activity.RESULT_OK && data != null) {
//            data.extras?.let {
//                isReset = true
//                position = data.getIntExtra("position", 0)
//            }
//        }
//        super.onActivityReenter(resultCode, data)
//    }

}