package com.example.adv2.ui.notifications


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.adv2.model.Message
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class NotificationsViewModel (application: Application) : AndroidViewModel(application) {
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

    fun SendMessage(editingText:String){
        // 在这里执行OkHttp请求发送消息
        val requestBody = FormBody.Builder()
            .add("question", editingText)
            .build()

        val request = Request.Builder()
            .url("http://116.205.128.125:8000/test/")
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

    //用户发消息，okhttp上传然后返回值
}
