package com.example.addemo
import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.view.TextureView
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var textView: TextView // 添加视频视图成员变量
    private lateinit var previewView: PreviewView
    private lateinit var sensorManagerHelper: SensorManagerHelper
    private lateinit var cameraManagerHelper: CameraManagerHelper
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
//        private const val REQUEST_CODE_PERMISSIONS = 10
    }
    private var recordingStartTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.text_view)
//        setContentView(R.layout.activity_my_camera) // 确保你的布局文件中有PreviewView
        previewView = findViewById(R.id.previewView)

        cameraManagerHelper = CameraManagerHelper(this, this, previewView)
        sensorManagerHelper = SensorManagerHelper(this)
        // 示例：创建文件并注册传感器监听

        if (allPermissionsGranted()) {
            cameraManagerHelper.startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        val recordButton: Button = findViewById(R.id.record_button)



//    recordButton.setOnClickListener
//    {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestCameraPermission()
//        } else {
////                requestCameraPermission()
//            toggleRecording(recordButton)
//        }
//    }
}

        private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

        private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                cameraManagerHelper.startCamera()
            } else {
                finish()
            }
        }

//        private fun toggleRecording(recordButton: Button) {
//        if (cameraManagerHelper.isRecordingVideo) {
//            recordButton.setText(R.string.record_button)
//            // Consider stopping sensor data collection here as well
//            cameraManagerHelper.stopVideoRecording()
////            sensorManagerHelper.stopSensorListener(sensorEventListener)
//        } else {
//            recordButton.setText(R.string.stop_button)
//            // Start collecting sensor data here
//            cameraManagerHelper.startVideoRecording(textureView)
////            sensorManagerHelper.startSensorListener(sensorEventListener) // 开始收集9轴数据
//        }
//    }


    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // 只有在开始记录后才处理数据
            if (recordingStartTime > 0) {
                val relativeTimeMillis = System.currentTimeMillis() - recordingStartTime
                val dataString =
                    "${event.values.joinToString(", ")}, Relative Timestamp: ${relativeTimeMillis}\n"

                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> sensorManagerHelper.writeDataToFileAcc(dataString)
                    Sensor.TYPE_GYROSCOPE -> sensorManagerHelper.writeDataToFileGyro(dataString)
                    Sensor.TYPE_MAGNETIC_FIELD -> sensorManagerHelper.writeDataToFileMag(dataString)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // 这里可以处理传感器精度变化，如果需要的话
        }
    }



//    private fun requestCameraPermission() {
//        when {
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CAMERA
//            ) == PackageManager.PERMISSION_GRANTED -> {
//                // 权限已经被授予，继续您的操作
////                findViewById<Button>(R.id.record_button).performClick() // Simulate a button click to retry the operation after permission is granted
////                cameraManagerHelper.startVideoRecording()
////                sensorManagerHelper.startSensorListener(sensorEventListener) // 开始收集9轴数据
//            }
//
//            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
//                // 提供一个额外的说明，如果需要的话
//                // 之后再次请求权限
//                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
//            }
//
//            else -> {
//                // 直接请求权限
//                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
//            }
//        }
//    }
//
//    private val requestPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
//            if (isGranted) {
//                // 权限被授予，继续操作
//            } else {
//                // 权限被拒绝，处理拒绝的情况
//            }
//        }
}