package com.example.ad_test

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.lifecycleScope
import com.example.ad_test.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.toDegrees
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var sensorManagerHelper: SensorManagerHelper
    private var recordingStartTime: Long = 0
    private var recordingEndTime: Long = 0
    private lateinit var sensorManager: SensorManager
//    private var latestAccData: FloatArray? = null
//    private var latestGyroData: FloatArray? = null
//    private var latestMagData: FloatArray? = null


    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Request camera permissions
        // 获得权限
        permissionsManager = PermissionsManager(this)
        sensorManagerHelper = SensorManagerHelper(this)

        if (!permissionsManager.allPermissionsGranted()) {
            permissionsManager.requestPermissions()
        }
        startCamera()

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.videoCaptureButton.setOnClickListener { captureVideo() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {}

    // Implements VideoCapture use case, including start and stop capturing.
    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return


        viewBinding.videoCaptureButton.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(this@MainActivity,
                        Manifest.permission.RECORD_AUDIO) ==
                    PermissionChecker.PERMISSION_GRANTED)
                {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        recordingStartTime = System.currentTimeMillis() // 记录开始时间
                        sensorManagerHelper.startSensorListener(sensorEventListener)// 启动传感器监听
                        viewBinding.videoCaptureButton.apply {
                            text = getString(R.string.stop_capture)
                            isEnabled = true
                        }
                    }
                    is VideoRecordEvent.Finalize -> {
                        sensorManagerHelper.stopSensorListener(sensorEventListener)// 停止传感器监听
                        recordingEndTime = System.currentTimeMillis() // 记录结束时间
                        val recordingDuration = recordingEndTime - recordingStartTime // 计算录制时长
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}, Duration: $recordingDuration ms"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " +
                                    "${recordEvent.error}, Duration: $recordingDuration ms")
                        }
                        viewBinding.videoCaptureButton.apply {
                            text = getString(R.string.start_capture)
                            isEnabled = true
                        }
                    }
                }
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
//            val qualitySelector = QualitySelector.from(Quality.HIGHEST, FallbackStrategy.higherQualityOrLowerThan(Quality.SD))
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider
                    .bindToLifecycle(this, cameraSelector, preview, videoCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        sensorManager.unregisterListener(sensorEventListener)
    }

    private val sensorEventListener = object : SensorEventListener {
        val accelerometerValues = FloatArray(3)
        val magneticValues = FloatArray(3)
        override fun onSensorChanged(event: SensorEvent) {
            if (recordingStartTime > 0) {
                lifecycleScope.launch(Dispatchers.Default) { // 在后台线程处理数据
                    val relativeTimeMillis = System.currentTimeMillis() - recordingStartTime
//                    val dataString = "${relativeTimeMillis},${event.values.joinToString(",")}\n"

                    when (event.sensor.type) {
//                        Sensor.TYPE_ACCELEROMETER -> sensorManagerHelper.writeDataToFileAcc(dataString)
//                        Sensor.TYPE_GYROSCOPE -> sensorManagerHelper.writeDataToFileGyro(dataString)
//                        Sensor.TYPE_MAGNETIC_FIELD -> sensorManagerHelper.writeDataToFileMag(dataString)
                        Sensor.TYPE_ACCELEROMETER -> {
                            accelerometerValues[0] = event.values[0]
                            accelerometerValues[1] =  -event.values[2]
                            accelerometerValues[2] = event.values[1]
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            magneticValues[0] = event.values[0]
                            magneticValues[1] = -event.values[2]
                            magneticValues[2] =  event.values[1]
                        }

                    }
                    val R = FloatArray(9)
                    val values = FloatArray(3)
                    SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues)
                    SensorManager.getOrientation(R, values)
                    val angle = DoubleArray(3) // 创建一个双精度浮点数数组来存储转换后的角度值

                    // 分别将values数组中的每个弧度值转换为度数
                    angle[0] = toDegrees(values[0].toDouble()) // 方位角(Azimuth) 转换为度
                    angle[1] = toDegrees(values[1].toDouble()) // 俯仰角(Pitch) 转换为度
                    angle[2] = toDegrees(values[2].toDouble()) // 滚转角(Roll) 转换为度

                    val angleString = "${relativeTimeMillis},${angle.joinToString(",")}\n"
                    Log.d("MainActivity", "value[0] is ${angle.joinToString(",")}")
                    sensorManagerHelper.writeDataToFile(angleString)

                }

            }

        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // 可以在这里处理传感器精度变化，如果需要的话
        }
    }


}

