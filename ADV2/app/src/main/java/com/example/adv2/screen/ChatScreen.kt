package com.example.adv2.screen


import android.os.Build
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adv2.R
import com.example.adv2.model.Message
import com.example.adv2.model.User
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen() {
    val coroutineScope = rememberCoroutineScope()

    val messages = remember {
        mutableStateListOf(
            Message(
                "你好，欢迎关注小陈！",
                getCurrentUser(),
                getReceiverUser(),
                LocalDateTime.now(),
                false
            )
        )
    }
    val listState = rememberLazyListState(messages.size - 1)

    Column(
        modifier = Modifier
            .background(Color(0xffededed))
            .fillMaxSize()
    ) {
        TopAppBar(title = getReceiverUser().nickname)

        MessageList(state = listState, messages = messages, modifier = Modifier.weight(1f))

        ChatBottomBar(onClick = {
            messages.add(it)
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        })
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
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            painter = painterResource(R.drawable.ic_more),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 输入栏
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ChatBottomBar(onClick: (Message) -> Unit) {
    var editingText by remember { mutableStateOf("") }

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
        Icon(
            painter = painterResource(R.drawable.ic_smile),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(4.dp)
                .size(28.dp)
        )
        if (editingText.isBlank()) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(4.dp)
                    .size(28.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onClick(
                        Message(
                            content = editingText,
                            sender = getCurrentUser(),
                            receiver = getReceiverUser(),
                            sendTime = LocalDateTime.now(),
                            mime = true
                        )
                    )
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
private fun getCurrentUser(): User {
    return User("老陈", R.drawable.avatar1)
}

/**
 * 构造聊天用户
 */
private fun getReceiverUser(): User {
    return User("小陈", R.drawable.avatar2)
}
