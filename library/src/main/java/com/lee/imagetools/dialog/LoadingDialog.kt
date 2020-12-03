package com.lee.imagetools.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.KeyEvent
import com.lee.imagetools.R

/**
 * @author jv.lee
 * @date 2020/12/3
 * @description
 */
class LoadingDialog(context: Context) : Dialog(context, R.style.TranslucentDialog) {

    init {
        setContentView(R.layout.layout_loading)
        setCanceledOnTouchOutside(false)
        setOnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_BACK }
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

}