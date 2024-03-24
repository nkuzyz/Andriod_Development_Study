package com.example.adv2.function

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsManager(private val activity: Activity) {
    private val PERMISSIONS_REQUEST_CODE = 10

    // 列出所有需要请求的权限
    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE // 注意: 对于API级别 >= 29 (Android 10) 的设备, 可以不需要这个权限
    )

    // 检查是否所有的权限都已被授予
    fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
    }

    // 请求尚未被授予的权限
    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            activity,
            REQUIRED_PERMISSIONS,
            PERMISSIONS_REQUEST_CODE
        )
    }

}
