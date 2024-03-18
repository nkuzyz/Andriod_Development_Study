package com.example.adv2.model

import java.time.LocalDateTime

data class Message(
    // 聊天内容
    val content: String,
    // 发送者
    val sender: User,
    // 接收者
    val receiver: User,
    // 发送时间
    val sendTime: LocalDateTime,
    // 是否是我的
    val mime: Boolean
)
