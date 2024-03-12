package com.example.ad_test

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.NonCancellable.start
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CameraManagerHelper (private val context: Context, private val lifecycleOwner: LifecycleOwner, private val previewView: PreviewView) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null
    var isRecording = false  // 用于跟踪录制状态
    companion object {
        // default Quality selection if no input from UI
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            //预览配置：Preview用于相机预览的时候显示预览画面。
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }



//            // 创建 QualitySelector，这里选择最高支持的质量
            val qualitySelector = QualitySelector.from(Quality.HIGHEST, FallbackStrategy.higherQualityOrLowerThan(Quality.SD))
//
            val recorder = Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll() // 解除所有之前的绑定
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview)
            } catch (exc: Exception) {
                exc.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun startRecording() {

//        val videoCapture = videoCapture ?: return
//        viewBinding.videoCaptureButton.isEnabled = false

        // 创建MediaStore的ContentValues，用于视频文件的保存
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()))
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            // 对于Android 10及以上版本，指定视频文件保存在公共的Pictures目录下的YourAppName文件夹
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/YourAppName")
            }
        }

        // 使用ContentValues和当前上下文创建MediaStoreOutputOptions
        val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        // 使用mediaStoreOutputOptions开始录制
        currentRecording = videoCapture?.output?.prepareRecording(context, mediaStoreOutputOptions)
            ?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        // 录制开始事件
                        isRecording = true  // 更新录制状态
                        Log.d("CameraManagerHelper", "is recording...")
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            // 录制成功完成
                            Log.d("CameraManagerHelper", "after recording...")
                            isRecording = false  // 更新录制状态
                        } else {
                            // 处理录制错误
                        }
                    }
                    // 可以根据需要处理其他事件
                }
            }
//        isRecording = true
        Log.d("CameraManagerHelper", "Starting recording...")

    }



    fun stopRecording() {
        // 停止录制
        currentRecording?.stop()
        currentRecording = null
        isRecording = false  // 更新录制状态
        Log.d("CameraManagerHelper", "Stopping recording...")

    }


}