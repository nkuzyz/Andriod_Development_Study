package com.example.ad_test

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher

class CameraManagerHelper(private val context: Context) {
    private lateinit var videoCaptureLauncher: ActivityResultLauncher<Intent>
    var onVideoCaptured: ((Uri?) -> Unit)? = null

//    fun init(activityResultLauncher: ActivityResultLauncher<Intent>) {
//        this.videoCaptureLauncher = activityResultLauncher
//    }

    fun startVideoCapture() {
        // 创建一个录制视频的Intent
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

        // 确保设备上有相机应用可以处理这个Intent
        takeVideoIntent.resolveActivity(context.packageManager)?.also {
            videoCaptureLauncher.launch(takeVideoIntent)
        }
    }
//    private fun startVideoRecording() {
//        recordingStartTime = System.currentTimeMillis()
//        getVideoUri()?.let { uri ->
//            val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
//                putExtra(MediaStore.EXTRA_OUTPUT, uri) // 指定视频保存位置
//            }
//            videoCaptureLauncher.launch(takeVideoIntent)
//        }
//    }
}