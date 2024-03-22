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
    private val _messages = MutableLiveData<MutableList<Message>>(mutableListOf(Message(
        "你好，请拍摄一段视频，这将会自动上传并得到一段caption，你可以问一些相关的问题",
        getUser(),
        getAssistant(),
        LocalDateTime.now(),
        false
    )))
    val messages: LiveData<MutableList<Message>> get()= _messages
    // 添加新消息的方法

    fun addMessage(message: Message) {
        // 当前消息列表
        val currentList = _messages.value ?: mutableListOf()
        currentList.add(message)
        _messages.value = currentList
    }

    fun addAssistantMessageString(result: String){
        val newMessage = Message(
                result,
                getAssistant(),
                getUser(),
                LocalDateTime.now(),
                false
            )
        _messages.value?.add(newMessage)
        // 更新 LiveData 的值以通知观察者
        _messages.postValue(_messages.value)
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
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() // 获取字符串形式的响应体
                    Log.d(TAG, "服务器响应: $responseBody")
                    // 在这里处理服务器响应
                } else {
                    // 请求失败处理
                    Log.d(TAG, "请求失败, HTTP状态码: ${response.code}, 原因: ${response.message}")
                }
            }
        })
    }

    //用户发消息，okhttp上传然后返回值
}
