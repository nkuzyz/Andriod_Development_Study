package com.example.ad_test
import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {
    private lateinit var previewView: PreviewView // 添加视频视图成员变量
    private lateinit var sensorManagerHelper: SensorManagerHelper
    private lateinit var cameraManagerHelper: CameraManagerHelper
    private lateinit var permissionsManager: PermissionsManager

    private var recordingStartTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        //注册传感器类
        sensorManagerHelper = SensorManagerHelper(this)
        //注册相机类
        cameraManagerHelper = CameraManagerHelper(this, this, previewView)

        // 获得权限
        permissionsManager = PermissionsManager(this)

        if (!permissionsManager.allPermissionsGranted()) {
            permissionsManager.requestPermissions()
        } else {
            // 执行需要权限的操作
        }
        //打开摄像头预览
        cameraManagerHelper.startCamera()

        //按钮设置
        val recordButton: Button = findViewById(R.id.record)
        recordButton.setOnClickListener {
//            requestCameraPermission()
            if (cameraManagerHelper.isRecording) {
                cameraManagerHelper.stopRecording()
                recordButton.text = "RECORD"
                sensorManagerHelper.stopSensorListener(sensorEventListener)
            } else {
                cameraManagerHelper.startRecording()
                recordButton.text = "STOP"
                sensorManagerHelper.startSensorListener(sensorEventListener) // 开始收集9轴数据
            }
        }
    }



//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        permissionsManager.handlePermissionsResult(requestCode, permissions, grantResults,
//            onGranted = {
//                // 所有权限已被授予，继续执行需要权限的操作
//            },
//            onDenied = {
//                // 用户拒绝了一些权限，可以在这里处理权限被拒绝的情况
//            }
//        )
//    }
//
//    private fun requestCameraPermission() {
//        when {
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CAMERA
//            ) == PackageManager.PERMISSION_GRANTED -> {
//                // 权限已经被授予，继续您的操作
//                cameraManagerHelper.startRecording()//开始录像
//                sensorManagerHelper.startSensorListener(sensorEventListener) // 开始收集9轴数据
//            }
//            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
//                // 提供一个额外的说明，如果需要的话
//                // 之后再次请求权限
//                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
//            }
//            else -> {
//                // 直接请求权限
//                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
//            }
//        }
//    }
//    private val requestPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
//            if (isGranted) {
//                // 权限被授予，继续操作
//            } else {
//                // 权限被拒绝，处理拒绝的情况
//            }
//        }




    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // 只有在开始记录后才处理数据
            if (recordingStartTime > 0) {
                val relativeTimeMillis = System.currentTimeMillis() - recordingStartTime
                val dataString = "${event.values.joinToString(", ")}, Relative Timestamp: ${relativeTimeMillis}\n"

                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> sensorManagerHelper.writeDataToFileAcc (dataString)
                    Sensor.TYPE_GYROSCOPE -> sensorManagerHelper.writeDataToFileGyro( dataString)
                    Sensor.TYPE_MAGNETIC_FIELD -> sensorManagerHelper.writeDataToFileMag(dataString)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // 这里可以处理传感器精度变化，如果需要的话
        }
    }



}