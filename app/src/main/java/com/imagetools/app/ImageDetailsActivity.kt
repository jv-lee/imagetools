package com.imagetools.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import com.imagetools.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_image.*

/**
 * @author jv.lee
 * @date 2020/12/14
 * @description
 */
class ImageDetailsActivity : BaseActivity(R.layout.activity_image_details),
    GestureDetector.OnGestureListener {

    companion object {
        const val TAG = "IMAGE_DETAILS"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gd = GestureDetector(this, this)

        iv_image.setOnTouchListener { v, event ->
            v.translationX = event.x
            v.translationY = event.y
            true
//            gd.onTouchEvent(event)
        }
    }

    override fun onShowPress(e: MotionEvent?) {
        Log.i(TAG, "onShowPress: ")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.i(TAG, "onSingleTapUp: ")
        return true
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.i(TAG, "onDown: ")
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.i(TAG, "onFling: ")
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.i(TAG, "onScroll: ")
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.i(TAG, "onLongPress: ")
    }

}