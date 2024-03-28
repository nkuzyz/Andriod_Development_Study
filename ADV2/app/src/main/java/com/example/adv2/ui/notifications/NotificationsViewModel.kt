package com.example.adv2.ui.notifications


import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.adv2.function.SensorManagerHelper
import com.example.adv2.model.Message
import com.example.adv2.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Okio
import okio.buffer
import okio.source
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class NotificationsViewModel (application: Application) : AndroidViewModel(application) {


    // 私有变量，防止外部直接访问修改
    private var _lastUploadResult: String? = null

    // 公开方法，用于修改 _lastUploadResult 的值
    fun updateLastUploadResult(newResult: String?) {
        _lastUploadResult = newResult
    }

    // 如果需要，也可以提供一个获取当前值的方法
    fun getLastUploadResult(): String? = _lastUploadResult

    private var lastImageAzimuth:Pair<Uri?,Float?>? = null
    fun updateLastImageAzimuth(uri: Uri?, azimuth: Float?) {
        lastImageAzimuth = Pair(uri, azimuth)
    }
    // 获取 lastImageAzimuth 的当前值
    fun getLastImageAzimuth(): Pair<Uri?, Float?>? = lastImageAzimuth

    private var _justAddImage:Boolean?=false



    private val sensorManagerHelper = SensorManagerHelper(application)
    private val sensorManager = application.getSystemService(Application.SENSOR_SERVICE) as SensorManager
    private val accelerometerValues = FloatArray(3)
    private val magneticValues = FloatArray(3)
    private val _messages = MutableLiveData<List<Message>>(listOf(Message(
        "你好，请拍摄一段视频，这将会自动上传并得到一段caption，你可以问一些相关的问题",
        getUser(),
        getAssistant(),
        LocalDateTime.now(),
        false
    )))
    val messages: LiveData<List<Message>> = _messages
    // 添加新消息的方法

    fun addMessage(newMessage: Message) {
        // 当前消息列表
//        val currentList = _messages.value ?: mutableListOf()
//        currentList.add(message)
//        _messages.value = currentList
        val updatedList = _messages.value.orEmpty().toMutableList()
        updatedList.add(newMessage)
        _messages.value = updatedList
    }

    fun addAssistantMessageString(result: String){
        val newMessage = Message(
                result,
                getAssistant(),
                getUser(),
                LocalDateTime.now(),
                false
            )
        val updatedList = _messages.value.orEmpty().toMutableList()
        updatedList.add(newMessage)
        _messages.value = updatedList

    }

    fun addImageMessage(){
        val newMessage = Message(
            "我添加了一张图片",
            getUser(),
            getAssistant(),
            LocalDateTime.now(),
            true
        )
        val updatedList = _messages.value.orEmpty().toMutableList()
        updatedList.add(newMessage)
        _messages.value = updatedList
        _justAddImage = true
    }

    fun SendText(requestBody: RequestBody, serverUrl: String) {
        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()
        val okhttpClient = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS) // 读取超时
            .writeTimeout(60, TimeUnit.SECONDS) // 写入超时
            .connectTimeout(60, TimeUnit.SECONDS) // 连接超时
            .build()
        okhttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 处理请求失败情况
                Log.e(TAG, "请求失败", e)
            }

            override fun onResponse(call: Call, response: Response) {
                // 处理请求成功情况
                response.use { resp ->
                    if (resp.isSuccessful) {
                        // 获取响应体的字符串
                        val responseBody = resp.body?.string()

                        // 解析响应，或者直接使用响应内容
                        // 这里假设我们直接使用响应内容作为消息内容
                        responseBody?.let { body ->
                            // 在主线程中更新 LiveData
                            val successMessage = Message(
                                content = body, // 使用服务器响应更新消息内容
                                sender = getAssistant(), // 假设的发送者
                                receiver = getUser(), // 假设的接收者
                                sendTime = LocalDateTime.now(),
                                mime = false
                            )

                            // 使用 postValue 安全地在后台线程更新 LiveData
//                            _messages.postValue(_messages.value?.apply { add(successMessage) })
                            _messages.postValue(_messages.value.orEmpty() + successMessage)

                        }
                    } else {
                        // 处理错误响应
                        Log.e(TAG, "请求失败，HTTP状态码: ${resp.code}")
                    }
                }
            }
        })
    }

    fun SendMessage(editingText:String){
        // 在这里执行OkHttp请求发送消息
        val requestBody = FormBody.Builder()
            .add("question", editingText)
            .build()

        val serverUrl = "http://116.205.128.125:8000/test/"
        SendText(requestBody = requestBody, serverUrl = serverUrl)
    }


    fun SendMessageWithAzimuth(editingText:String){
        //用sensor捕捉此时的信息，然后和editingText一起发出去
        val R = FloatArray(9)
        val values = FloatArray(3)
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues)
        SensorManager.getOrientation(R, values)
        val angle = DoubleArray(3) // 创建一个双精度浮点数数组来存储转换后的角度值

        // 分别将values数组中的每个弧度值转换为度数
        angle[0] = Math.toDegrees(values[0].toDouble()) // 方位角(Azimuth) 转换为度
//        angle[1] = Math.toDegrees(values[1].toDouble()) // 俯仰角(Pitch) 转换为度
//        angle[2] = Math.toDegrees(values[2].toDouble()) // 滚转角(Roll) 转换为度
        if (angle[0]<0){
            angle[0] = angle[0]+360
        }

        val azimuth = angle[0].toFloat()


        // 判断刚才是不是刚刚添加了图片
        if (_justAddImage == true)
        {
            val application = getApplication<Application>()
            val contentResolver = application.contentResolver
            val imageRequestBody = lastImageAzimuth?.first?.let { uri ->
                // 确保 Uri 不为 null
                contentResolver.openInputStream(uri)?.source()?.buffer()?.let { sourceBuffered ->
                    // 使用Okio 2.x API
                    sourceBuffered.readByteString().toRequestBody("image/jpeg".toMediaTypeOrNull())
                }
            }


            //构建消息体发送
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image","fimename.jpeg",imageRequestBody!!)
                .addFormDataPart("azimuth", lastImageAzimuth?.second.toString())
                .addFormDataPart("question", editingText)
                .build()

            val serverUrl = "http://116.205.128.125:8000/upload-image/"
            SendText(requestBody = requestBody, serverUrl = serverUrl)

            _justAddImage = false
        }
        else {
            //构建消息体发送
            val requestBody = FormBody.Builder()
                .add("azimuth", azimuth.toString())
                .add("question", editingText)
                .build()
            val serverUrl = "http://116.205.128.125:8000/ask-question/"
            SendText(requestBody = requestBody, serverUrl = serverUrl)
        }

    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
                viewModelScope.launch(Dispatchers.Default) {
                    // Sensor data processing logic here
                    // Similar to your existing logic
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

                }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Handle sensor accuracy changes if needed
        }
    }

    fun registerSensorListeners(){
        sensorManagerHelper.startSensorListener(sensorEventListener)// 启动传感器监听
    }
    fun unregisterSensorListeners() {
        sensorManagerHelper.stopSensorListener(sensorEventListener)// 停止传感器监听
    }
    //用户发消息，okhttp上传然后返回值
}
