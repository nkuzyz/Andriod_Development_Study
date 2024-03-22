package com.example.adv2.ui.dashboard


import android.app.Application
import android.content.ContentValues
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import android.Manifest
import android.content.ContentResolver
import android.net.Uri
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.adv2.SharedViewModel
import com.example.adv2.function.SensorManagerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.Okio
import okio.buffer
import okio.source
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val _previewViewProvider = MutableLiveData<PreviewViewProvider?>()
    val previewViewProvider: LiveData<PreviewViewProvider?> = _previewViewProvider


    private val sensorManagerHelper = SensorManagerHelper(application)
    private val sensorManager = application.getSystemService(Application.SENSOR_SERVICE) as SensorManager
    private val accelerometerValues = FloatArray(3)
    private val magneticValues = FloatArray(3)

    // 需要初始化videoCapture
    private var videoCapture: VideoCapture<Recorder>? = null
    //recording的状态
    private var recording: Recording? = null
    // 记录开始和结束时间，用于计算录制时长
    private var recordingStartTime: Long = 0
    private var recordingEndTime: Long = 0
    // LiveData to communicate with the UI
    // LiveData来通知UI层当前的录制状态
    private val _recordingState = MutableLiveData<Boolean>()
    val recordingState: LiveData<Boolean> = _recordingState
    private val _recordingMessage = MutableLiveData<String?>()
    val recordingMessage: LiveData<String?> = _recordingMessage

    private var videoUri: Uri ?= null
    private var csvUri:Uri?=null

    private val _uploadResult = MutableLiveData<String>()
    val uploadResult: LiveData<String> = _uploadResult

    companion object {
        private const val TAG = "ZYZ"
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(getApplication())
        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()
                val videoCaptureTemp = VideoCapture.withOutput(recorder) // 直接初始化一个局部变量
                videoCapture = videoCaptureTemp // 赋值给类属性
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Need to bind the lifecycle of cameras to the lifecycle owner
                // This will be handled in the Fragment by observing previewViewProvider LiveData
                _previewViewProvider.postValue(PreviewViewProvider(preview, videoCaptureTemp, cameraSelector))
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(getApplication()))
    }

    fun uploadFiles(serverUrl: String) {
        val application = getApplication<Application>()
        val contentResolver = application.contentResolver
        Log.d(TAG, "uploadFiles")
        val videoRequestBody = videoUri?.let { uri ->
            contentResolver.openInputStream(uri)?.source()?.buffer()?.let { sourceBuffered ->
                // 使用Okio 2.x API
                sourceBuffered.readByteString().toRequestBody("video/mp4".toMediaTypeOrNull())
            }
        }

        val csvRequestBody = csvUri?.let { uri ->
            contentResolver.openInputStream(uri)?.source()?.buffer()?.let { sourceBuffered ->
                // 使用Okio 2.x API
                sourceBuffered.readByteString().toRequestBody("text/csv".toMediaTypeOrNull())
            }
        }

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("video", "filename.mp4", videoRequestBody!!)
            .addFormDataPart("csv", "filename.csv", csvRequestBody!!)
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .post(multipartBody)
            .build()

        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS) // 读取超时
            .writeTimeout(60, TimeUnit.SECONDS) // 写入超时
            .connectTimeout(60, TimeUnit.SECONDS) // 连接超时
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 处理请求失败情况
                Log.d(TAG, "请求没发出: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                // 处理请求成功情况
                if (response.isSuccessful) {
                    // 请求成功处理，例如更新UI
                    val responseBody = response.body?.string() // 获取字符串形式的响应体
                    Log.d(TAG, "服务器响应: $responseBody")
                    _uploadResult.postValue(" $responseBody")
                } else {
                    // 请求失败处理
                    Log.d(TAG, "请求失败, HTTP状态码: ${response.code}, 原因: ${response.message}")
                }
            }
        })
    }

    fun captureVideo() {
        val currentRecording = recording
        if (currentRecording != null) {
            // Stop the current recording session.
            Log.d(TAG, "Stopping current recording session.")
            stopRecording()

        } else {
            // Start a new recording session.
            Log.d(TAG, "Starting new recording session.")
            startRecording()
        }
    }

    private fun startRecording() {
        val application = getApplication<Application>()
//        val videoCapture = this.videoCapture ?: return
        val videoCapture = this.videoCapture ?: run {
            Log.d(TAG, "VideoCapture is null, returning from startRecording.")
            return
        }
        // 创建文件名
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        // 配置MediaStoreOutputOptions
        val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
            application.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        // 开始录制
        recording = videoCapture.output.prepareRecording(application, mediaStoreOutputOptions)
            .apply {
                // 检查音频权限
                if (ContextCompat.checkSelfPermission(application,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(application)) { recordEvent ->
                // 处理录制事件
                handleRecordEvent(recordEvent)
            }

    }


    private fun stopRecording() {
        recording?.stop()
        recording = null
        _recordingState.value = false
        sensorManagerHelper.stopSensorListener(sensorEventListener)// 停止传感器监听
        Log.d(TAG, "Recording stopped.")
    }

    private fun handleRecordEvent(recordEvent: VideoRecordEvent) {
        when(recordEvent) {
            is VideoRecordEvent.Start -> {
                Log.d(TAG, "Recording started.")
                // 录制开始的逻辑
                recordingStartTime = System.currentTimeMillis()
                _recordingState.postValue(true)
                csvUri = sensorManagerHelper.startSensorListener(sensorEventListener)// 启动传感器监听

            }
            is VideoRecordEvent.Finalize -> {
                Log.d(TAG, "Recording finalize.")
                // 录制结束的逻辑
                recordingEndTime = System.currentTimeMillis()
                val recordingDuration = recordingEndTime - recordingStartTime
                if (!recordEvent.hasError()) {
                    // 处理成功录制的视频
                    videoUri = recordEvent.outputResults.outputUri
                    val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}, Duration: $recordingDuration ms"
                    postRecordingMessage(msg)
                } else {
                    // 处理录制错误
                    val errorMsg = "Video capture ends with error: ${recordEvent.error}, Duration: $recordingDuration ms"
                    postRecordingMessage(errorMsg)
                }
                _recordingState.postValue(false)
            }
        }
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (recordingStartTime > 0) {
                viewModelScope.launch(Dispatchers.Default) {
                    // Sensor data processing logic here
                    // Similar to your existing logic
                    val relativeTimeMillis = System.currentTimeMillis() - recordingStartTime

                    when (event.sensor.type) {
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
                    angle[0] = Math.toDegrees(values[0].toDouble()) // 方位角(Azimuth) 转换为度
                    if (angle[0]<0){
                        angle[0] = angle[0]+360
                    }
                    angle[1] = Math.toDegrees(values[1].toDouble()) // 俯仰角(Pitch) 转换为度
                    angle[2] = Math.toDegrees(values[2].toDouble()) // 滚转角(Roll) 转换为度

                    val angleString = "${relativeTimeMillis},${angle.joinToString(",")}\n"
                    Log.d("MainActivity", "value[0] is ${angle.joinToString(",")}")
                    sensorManagerHelper.writeDataToFile(angleString)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Handle sensor accuracy changes if needed
        }
    }

    private fun postRecordingMessage(message: String) {
        _recordingMessage.value = message
    }

    fun clearRecordingMessage() {
        _recordingMessage.value = null
    }
    fun unregisterSensorListeners() {
        sensorManager.unregisterListener(sensorEventListener)
    }

}

data class PreviewViewProvider(
    val preview: Preview,
    val videoCapture: VideoCapture<Recorder>,
    val cameraSelector: CameraSelector
)

