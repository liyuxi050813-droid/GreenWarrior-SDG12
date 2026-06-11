package com.example.a207387_liyuxi_ptizwan_lab04.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // 1. For round avatars and task icons, use a large value
    extraSmall = RoundedCornerShape(50.dp),

    // 2. Input fields and button corner radius
    medium = RoundedCornerShape(12.dp),

    // 3. Task card corner radius
    large = RoundedCornerShape(20.dp),

    // 4. [KEY] Dashboard and data rings
    // To make a perfect circle, this value must be large (e.g. 100.dp)
    // Or set RoundedCornerShape(28.dp) to restore your original effect
    extraLarge = RoundedCornerShape(100.dp)
)