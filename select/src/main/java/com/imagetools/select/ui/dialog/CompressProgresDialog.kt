package com.imagetools.select.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.KeyEvent
import android.widget.ProgressBar
import com.imagetools.select.R

/**
 * @author jv.lee
 * @date 2020/12/3
 * @description
 */
class CompressProgresDialog(context: Context) : Dialog(context, R.style.TranslucentDialog) {

    private val progressView: ProgressBar

    init {
        setContentView(R.layout.dialog_compress_progress)
        setCanceledOnTouchOutside(false)
        setOnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_BACK }
        progressView = findViewById(R.id.progress)
    }

    override fun show() {
        if (context is Activity) {
            if ((context as Activity).isFinishing) {
                return
            }
        }
        super.show()
    }

    override fun dismiss() {
        if (context is Activity) {
            if ((context as Activity).isFinishing) {
                return
            }
        }
        super.dismiss()
    }

    override fun onBackPressed() {
        if (isShowing) {
            dismiss()
            return
        }
        super.onBackPressed()
    }

    fun setProgress(progress: Int) {
        progressView.progress = progressView.progress + progress
    }

}