package com.example.a207387_liyuxi_ptizwan_lab04

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import com.example.a207387_liyuxi_ptizwan_lab04.ui.theme.A207387_LiYuxi_PtIzwan_Lab04Theme

object Routes {
    const val HOME = "home"
    const val EDIT_PROFILE = "edit_profile"
    const val PREVIEW = "preview"
    const val SET_TARGET = "set_target"
    const val ADD_LOG = "add_log"
    const val LOG_LIST = "log_list"
}//1我的项目有四个屏幕，定义了每个屏幕的路由常量

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A207387_LiYuxi_PtIzwan_Lab04Theme(darkTheme = false) {
                val navController = rememberNavController()
                val viewModel: ProfileViewModel = viewModel()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(//2 composable 把路由和界面绑定
                        navController = navController,
                        startDestination = Routes.HOME//startDestination 设为 HOME，所以一打开就是首页。
                    ) {
                        composable(Routes.HOME) {
                            HomeScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable(Routes.EDIT_PROFILE) {
                            EditProfileScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable(Routes.PREVIEW) {
                            PreviewScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable(Routes.SET_TARGET) {
                            SetTargetScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable(Routes.ADD_LOG) {
                            AddLogScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.LOG_LIST) {
                            LogListScreen(navController = navController, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ProfileViewModel//3
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val savedCO2 = userProfile.savedCO2

    // 检查目标是否达成
    val isAchieved = viewModel.isTargetAchieved()
    if (isAchieved) {
        var showDialog by remember { mutableStateOf(true) }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("🎉 Congratulations!") },
                text = { Text("You've reached your carbon reduction target! Keep up the great work!") },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Thanks!")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
                textContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景图片
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        // 渐变遮罩
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 顶部欢迎栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Hello,",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = userProfile.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 设置目标图标
                    IconButton(
                        onClick = { navController.navigate(Routes.SET_TARGET) }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Set Target",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // 头像（点击进入编辑页）
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { navController.navigate(Routes.EDIT_PROFILE) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            userProfile.displayName.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                // 移除 border，直接居中内容
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "CARBON SAVED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val savedCO2 = userProfile.savedCO2
                        Text(
                            text = "$savedCO2",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "kg CO₂",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(modifier = Modifier.padding(horizontal = 32.dp))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 目标行
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🚩",
                                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.secondary)
                            )
                            Text(
                                "Target: ${userProfile.targetCO2} kg",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 进度条
                        val progress = if (userProfile.targetCO2 > 0) {
                            savedCO2.toFloat() / userProfile.targetCO2
                        } else 0f

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(8.dp),
                            color = if (isAchieved) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${(progress * 100).toInt()}% to goal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 一键清零按钮
                        OutlinedButton(
                            onClick = { viewModel.resetAll() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("Reset All to Zero")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 预览按钮
            // 替换原有的 Preview 按钮为两个按钮：
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate(Routes.ADD_LOG) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Add Log")
                }
                Button(
                    onClick = { navController.navigate(Routes.LOG_LIST) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("View Logs")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 今日任务
            Text(
                "TODAY'S MISSIONS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                TaskRow("🚲", "Cycle to Work", "Save 1.5kg CO2! Choose cycling over driving.") {
                    viewModel.reduceCO2(2)
                }
                TaskRow("🥗", "Plant-based Meal", "Earn 50 points. Reducing water consumption.") {
                    viewModel.reduceCO2(3)
                }
                TaskRow("🛍️", "No Plastic Bag", "Small steps matter! Bring your own reusable bag.") {
                    viewModel.reduceCO2(1)
                }
                TaskRow("💡", "Save Energy", "Turn off the AC for 2 hours today.") {
                    viewModel.reduceCO2(4)
                }
            }
        }
    }
}

@Composable
fun TaskRow(
    icon: String,
    title: String,
    detail: String,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                expanded = !expanded
                if (!expanded) {
                    onClick()
                }
            }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }
    }
}

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    var tempName by remember { mutableStateOf(userProfile.displayName) }
    var tempCurrent by remember { mutableStateOf(userProfile.currentCO2.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Edit Profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = tempName,
            onValueChange = { tempName = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = tempCurrent,
            onValueChange = { tempCurrent = it },
            label = { Text("Current CO₂ Emission (kg)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.updateDisplayName(tempName)
                tempCurrent.toIntOrNull()?.let { viewModel.updateCurrentCO2(it) }
                navController.navigateUp()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}

@Composable
fun PreviewScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("👤 Profile Preview", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Name: ${userProfile.displayName}", style = MaterialTheme.typography.bodyLarge)
                Text("Target: ${userProfile.targetCO2} kg", style = MaterialTheme.typography.bodyLarge)
                Text("Current Emission: ${userProfile.currentCO2} kg", style = MaterialTheme.typography.bodyLarge)
                Text("Saved: ${userProfile.savedCO2} kg", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "✅ RSVP Confirmed!",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack(Routes.HOME, inclusive = false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}

@Composable
fun SetTargetScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    var tempTarget by remember { mutableStateOf(userProfile.targetCO2.toString()) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Set Your Carbon Target",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "You will be congratulated when your saved CO₂ reaches the target.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = tempTarget,
            onValueChange = { tempTarget = it },
            label = { Text("Target CO₂ (kg)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage.isNotBlank()
        )
        if (errorMessage.isNotBlank()) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val targetInt = tempTarget.toIntOrNull()
                if (targetInt == null || targetInt <= 0) {
                    errorMessage = "Please enter a valid positive number"
                } else {
                    viewModel.setTargetCO2(targetInt)
                    navController.navigateUp()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Target")
        }
    }
}
@Composable
fun AddLogScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    var activityName by remember { mutableStateOf("") }
    var emissionAmount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Add Emission Log",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = activityName,
            onValueChange = { activityName = it },
            label = { Text("Activity Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = emissionAmount,
            onValueChange = { emissionAmount = it },
            label = { Text("Emission Amount (kg)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage.isNotBlank()
        )
        if (errorMessage.isNotBlank()) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val amountInt = emissionAmount.toIntOrNull()
                if (activityName.isBlank() || amountInt == null || amountInt <= 0) {
                    errorMessage = "Please enter valid name and positive number"
                } else {
                    viewModel.addEmissionLog(activityName, amountInt)
                    navController.navigateUp()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Save Log")
        }
    }
}
@Composable
fun LogListScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val logList by viewModel.logList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        Text(
            "Emission Logs",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (logList.isEmpty()) {
            Text(
                "No logs yet. Add your first activity!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logList, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                log.activityName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${log.emissionAmount} kg",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack(Routes.HOME, inclusive = false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}