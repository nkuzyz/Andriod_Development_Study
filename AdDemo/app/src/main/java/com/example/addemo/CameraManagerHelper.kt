package com.example.addemo
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class CameraManagerHelper(private val context: Context, private val lifecycleOwner: LifecycleOwner, private val previewView: PreviewView) {

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            // 用于将相机的生命周期绑定到应用的生命周期
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            try {
                // 默认选择后置相机
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // 在尝试绑定之前解绑所有用途
                cameraProvider.unbindAll()

                // 绑定用途到相机
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview)
            } catch(exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
