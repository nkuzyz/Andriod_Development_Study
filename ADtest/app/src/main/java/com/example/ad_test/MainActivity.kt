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
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {
    private lateinit var videoCaptureLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoView: VideoView // 添加视频视图成员变量
    private lateinit var sensorManagerHelper: SensorManagerHelper
//    private lateinit var cameraManagerHelper: CameraManagerHelper


    private var recordingStartTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoView = findViewById(R.id.videoView)

        sensorManagerHelper = SensorManagerHelper(this)
        // 示例：创建文件并注册传感器监听
//        cameraManagerHelper = CameraManagerHelper(this)


        videoCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val videoUri: Uri? = result.data?.data
                videoUri?.let {
                    videoView.setVideoURI(it)
                    videoView.start()
                }
            }

            // 停止监听传感器数据
            sensorManagerHelper.stopSensorListener(sensorEventListener)
        }


        val recordButton: Button = findViewById(R.id.record)
        recordButton.setOnClickListener {
            requestCameraPermission()
        }
    }



    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 权限已经被授予，继续您的操作
                startVideoRecording()
                sensorManagerHelper.startSensorListener(sensorEventListener) // 开始收集9轴数据
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // 提供一个额外的说明，如果需要的话
                // 之后再次请求权限
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                // 直接请求权限
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // 权限被授予，继续操作
            } else {
                // 权限被拒绝，处理拒绝的情况
            }
        }


    private fun startVideoRecording() {
        recordingStartTime = System.currentTimeMillis()
        getVideoUri()?.let { uri ->
            val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri) // 指定视频保存位置
            }
            videoCaptureLauncher.launch(takeVideoIntent)
        }
    }
    private fun getVideoUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "video_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // 如果是Android 10及以上，还需要指定存储位置
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            }
        }

        return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

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