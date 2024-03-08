package com.example.ad_test

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsManager(private val activity: Activity) {
    private val PERMISSIONS_REQUEST_CODE = 101

    // 列出所有需要请求的权限
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
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

    // 处理权限请求结果的方法，可以在Activity的onRequestPermissionsResult中调用
    fun handlePermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray, onGranted: () -> Unit, onDenied: () -> Unit) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }
}
