package com.example.adv2.ui.notifications

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.adv2.R
import com.example.adv2.model.Message
import com.example.adv2.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


const val TAG = "ZYZ"




@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(navController: NavController,viewModel:NotificationsViewModel) {
    val coroutineScope = rememberCoroutineScope()

    // 使用mutableStateOf包装messages列表，确保UI能够自动更新
    val messages:List<Message> by viewModel.messages.observeAsState(listOf(Message(
        "你好，请拍摄一段视频，这将会自动上传并得到一段caption，你可以问一些相关的问题",
        getUser(),
        getAssistant(),
        LocalDateTime.now(),
        false
    )))
//    var messages by remember { mutableStateOf(viewModel.messages.value ?: mutableListOf()) }
    // 直接观察LiveData的变化
//    val messages :MutableList<Message> by viewModel.messages.observeAsState(initial = mutableListOf())
    val listState = rememberLazyListState(messages.size - 1)


    // 观察消息列表的变化，并在消息列表发生变化时执行滚动到底部的操作
    LaunchedEffect(messages) {
        coroutineScope.launch {
            if (messages.isNotEmpty()) {
//                delay(100) // 仅示例，实际延时根据需要调整
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }



    Column(
        modifier = Modifier
            .background(Color(0xffededed))
            .fillMaxSize()
    ) {
        TopAppBar(title = getAssistant().nickname)

        MessageList(state = listState, messages = messages, modifier = Modifier.weight(1f))
        // 在这里添加录音按钮
//        RecorderButtonComposable()
        ChatBottomBar(navController = navController,viewModel = viewModel)
    }
}

/**
 * 标题栏
 */
@Composable
private fun TopAppBar(title: String) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        Icon(
//            painter = painterResource(R.drawable.ic_back),
//            contentDescription = null,
//            modifier = Modifier.size(20.dp)
//        )
        Text(
            text = title,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
//        Icon(
//            painter = painterResource(R.drawable.ic_more),
//            contentDescription = null,
//            modifier = Modifier.size(24.dp)
//        )
    }
}

/**
 * 输入栏
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ChatBottomBar(navController: NavController,viewModel:NotificationsViewModel ) {
    var editingText by remember { mutableStateOf("") }
    // 在Compose中获取NavController实例
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xfff7f7f7))
            .padding(4.dp, 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_voice),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(4.dp)
                .size(28.dp)
        )
        BasicTextField(
            value = editingText,
            onValueChange = { editingText = it },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .padding(start = 8.dp, top = 10.dp, end = 8.dp)
        )
//        Icon(
//            painter = painterResource(R.drawable.ic_smile),
//            contentDescription = null,
//            modifier = Modifier
//                .align(Alignment.CenterVertically)
//                .padding(4.dp)
//                .size(28.dp)
//        )
        if (editingText.isBlank()) {
            Button(onClick = {
                try {
                    navController.navigate(R.id.action_notifications_to_dashboard)
                } catch (e: Exception) {
                    // 记录异常信息
                    Log.e("Navigation", "Failed to navigate", e)
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(4.dp)
                        .size(28.dp)
                )
                
            }

        } else {
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                        viewModel.addMessage(
                            Message(
                            content = editingText,
                            sender = getUser(),
                            receiver = getAssistant(),
                            sendTime = LocalDateTime.now(),
                            mime = true
                            )
                        )
                    Log.d(TAG, "发了 $editingText")
                    viewModel.SendMessageWithAzimuth(editingText)
                    editingText = ""
                },
                colors = ButtonDefaults.buttonColors(Color(0xff07c160)),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = "发送", color = Color.White)
            }
        }
    }
}

/**
 * 消息列表
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MessageList(
    state: LazyListState,
    messages: List<Message>,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        itemsIndexed(items = messages) { index, item ->
            if (index == 0 || messages[index - 1].sendTime.plusMinutes(3).isBefore(item.sendTime)) {
                // 显示消息发送时间
                Text(
                    text = formatter.format(item.sendTime),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                )
            }

            // 显示聊天气泡
            MessageBubble(message = item)
        }
    }
}

/**
 * 聊天气泡
 */
@Composable
fun MessageBubble(message: Message) {
    val bubbleColor = if (message.mime) Color(0xff95ec69) else Color.White
    val bubbleArrangement = if (message.mime) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = bubbleArrangement
    ) {
        if (!message.mime) {
            // 显示接收者的头像
            Image(
                painter = painterResource(message.receiver.avatar),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        // 显示聊天气泡
        Text(
            text = message.content,
            modifier = Modifier
                .drawBehind {
                    val bubble = Path().apply {
                        // 创建消息气泡的路径
                        val rect = RoundRect(
                            left = 10.dp.toPx(),
                            top = 0f,
                            right = size.width - 10.dp.toPx(),
                            bottom = size.height,
                            radiusX = 4.dp.toPx(),
                            radiusY = 4.dp.toPx()
                        )
                        addRoundRect(rect)
                        if (message.mime) {
                            // 如果是发送者的消息，添加尾巴
                            moveTo(x = size.width - 10.dp.toPx(), y = 15.dp.toPx())
                            lineTo(x = size.width - 5.dp.toPx(), y = 20.dp.toPx())
                            lineTo(x = size.width - 10.dp.toPx(), y = 25.dp.toPx())
                        }
                        if (!message.mime) {
                            // 如果是接收者的消息，添加尾巴
                            moveTo(x = 10.dp.toPx(), y = 15.dp.toPx())
                            lineTo(x = 5.dp.toPx(), y = 20.dp.toPx())
                            lineTo(x = 10.dp.toPx(), y = 25.dp.toPx())
                        }
                        close()
                    }
                    drawPath(path = bubble, color = bubbleColor)
                }
                .padding(horizontal = 20.dp, vertical = 10.dp)
        )

        if (message.mime) {
            // 显示发送者的头像
            Image(
                painter = painterResource(message.sender.avatar),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * 构造当前用户
 */
fun getUser(): User {
    return User("老陈", R.drawable.avatar1)
}

/**
 * 构造聊天用户
 */
fun getAssistant(): User {
    return User("Assistant", R.drawable.avatar2)
}

//@Composable
//fun RecorderButtonComposable() {
//    val context = LocalContext.current
//
//    AndroidView(
//        factory = { ctx ->
//            // 使用context创建您的AudioRecorderButton
//            AudioRecorderButton(ctx).apply {
//                // 在这里配置您的按钮，例如设置监听器等
//            }
//        },
//        update = { view ->
//            // 如果需要，根据Compose的状态更新视图
//        }
//    )
//}
