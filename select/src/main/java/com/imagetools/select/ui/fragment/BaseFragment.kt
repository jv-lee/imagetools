package com.imagetools.select.ui.fragment

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

/**
 * @author jv.lee
 * @date 2021/1/5
 * @description
 */
open class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    fun Fragment.toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun Fragment.checkPermission(permission: String) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            throw RuntimeException("Please apply for '$permission' permission first")
        }
    }
}