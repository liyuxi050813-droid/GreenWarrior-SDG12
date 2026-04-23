package com.example.a207387_liyuxi_ptizwan_lab04.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // 1. 头像和任务图标想变圆，这个数值要大
    extraSmall = RoundedCornerShape(50.dp),

    // 2. 输入框和按钮的圆角
    medium = RoundedCornerShape(12.dp),

    // 3. 任务卡片的圆角
    large = RoundedCornerShape(20.dp),

    // 4. 【关键】Dashboard 和数据圆环
    // 要想变回完美的圆，这个数值必须很大（比如 100.dp）
    // 或者设置为 RoundedCornerShape(28.dp) 恢复你最初的效果
    extraLarge = RoundedCornerShape(100.dp)
)