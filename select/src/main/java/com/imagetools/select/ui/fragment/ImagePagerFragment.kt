package com.imagetools.select.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.imagetools.select.R
import com.imagetools.select.constant.SharedConstants
import com.imagetools.select.entity.Image
import com.imagetools.select.tools.UriTools
import com.imagetools.select.ui.adapter.ImagePagerAdapter
import com.imagetools.select.ui.widget.DragImageView

/**
 * @author jv.lee
 * @date 2020/12/12
 * @description
 */
class ImagePagerFragment : Fragment(R.layout.fragment_image_pager_imagetools) {

    companion object {

        fun newInstance(
            position: Int,
            transitionName: String,
            size: Int,
            data: ArrayList<String>
        ): Fragment {
            return ImagePagerFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(
                        SharedConstants.KEY_DATA,
                        data.run {
                            val images = arrayListOf<Image>()
                            for ((index, item) in this.withIndex()) {
                                images.add(
                                    Image(
                                        index.toLong(),
                                        UriTools.pathToUri(requireContext(), item)
                                    )
                                )
                            }
                            return@run images
                        }
                    )
                    putInt(SharedConstants.KEY_POSITION, position)
                    putString(SharedConstants.KEY_TRANSITION_NAME, transitionName)
                    putInt(SharedConstants.KEY_SIZE, size)
                }
            }
        }
    }

    private val data by lazy<ArrayList<Image>> {
        arguments?.getParcelableArrayList(SharedConstants.KEY_DATA) ?: arrayListOf()
    }
    private val position by lazy { arguments?.getInt(SharedConstants.KEY_POSITION) ?: 0 }
    private val transitionName by lazy {
        arguments?.getString(SharedConstants.KEY_TRANSITION_NAME) ?: ""
    }
    private val size by lazy { arguments?.getInt(SharedConstants.KEY_SIZE) ?: 0 }

    private val adapter by lazy {
        ImagePagerAdapter(data).also {
            it.setDragCallback(object : DragImageView.Callback {
                override fun onClicked() {
                    //单击事件
                    requireActivity().supportFinishAfterTransition()
                }

                override fun onDragClose() {
                    //关闭当前activity 执行共享动画关闭
                    requireActivity().supportFinishAfterTransition()
                }

                override fun changeAlpha(alpha: Float) {
                    //根据下拉修改activity透明度
                    it.setBackgroundAlphaCompat(
                        requireActivity().window.decorView,
                        (255 * alpha).toInt()
                    )
                }

            })
        }
    }

    private val vpContainer:ViewPager2 by lazy { requireView().findViewById(R.id.vp_container) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAnimation()
        initPager()
    }

    private fun initAnimation() {
//        //暂时阻止共享元素过渡
//        requireActivity().supportPostponeEnterTransition()
//        //占位图加载完成后 开启共享元素共享动画
//        requireActivity().supportStartPostponedEnterTransition()

        //设置回调共享元素通信
        requireActivity().setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                val position = vpContainer.currentItem
                val view = vpContainer.findViewById<View>(R.id.drag_image)
                view?.run {
                    sharedElements.put(data[position].uri.path ?: "", this)
                }
            }
        })

        //设置共享元素执行时长
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.sharedElementEnterTransition.duration = 200
            requireActivity().window.sharedElementExitTransition.duration = 200
        }
    }

    private fun initPager() {
        //初始化加载详情图Pager页面.
        vpContainer.adapter = adapter
        //每次切换页面动态更改回调值
        vpContainer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                parseResult()
            }
        })
        //定位到选中位置
        vpContainer.setCurrentItem(position, false)
    }

    private fun parseResult() {
        requireActivity().setResult(
            Activity.RESULT_OK,
            Intent().putExtra(SharedConstants.KEY_POSITION, vpContainer.currentItem)
        )
    }

}