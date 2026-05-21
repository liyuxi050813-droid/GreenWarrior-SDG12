package com.example.a207387_liyuxi_ptizwan_lab04

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.a207387_liyuxi_ptizwan_lab04.ui.theme.A207387_LiYuxi_PtIzwan_Lab04Theme

object Routes {
    const val HOME = "home"
    const val EDIT_PROFILE = "edit_profile"
    const val SET_TARGET = "set_target"
    const val ADD_LOG = "add_log"
    const val REPORT = "report"
    const val CHALLENGES = "challenges"   // 新增
}

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A207387_LiYuxi_PtIzwan_Lab04Theme(darkTheme = false) {
                val navController = rememberNavController()
                val viewModel: ProfileViewModel = viewModel()

                val bottomNavItems = listOf(
                    BottomNavItem("Home", Icons.Filled.Home, Routes.HOME),
                    BottomNavItem("Challenges", Icons.Filled.EmojiEvents, Routes.CHALLENGES),  // 新功能
                    BottomNavItem("Report", Icons.Filled.BarChart, Routes.REPORT),
                    BottomNavItem("Profile", Icons.Filled.Person, Routes.EDIT_PROFILE)
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            bottomNavItems.forEach { item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) },
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.HOME,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.HOME) {
                            HomeScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.EDIT_PROFILE) {
                            EditProfileScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.REPORT) {
                            ReportScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.ADD_LOG) {
                            AddLogScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.SET_TARGET) {
                            SetTargetScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.CHALLENGES) {
                            ChallengesScreen(navController = navController, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

// ------------------- HomeScreen -------------------
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val customTasks by viewModel.customTasks.collectAsState()
    val savedCO2 = userProfile.savedCO2

    var showAddTaskDialog by remember { mutableStateOf(false) }

    val isAchieved = viewModel.isTargetAchieved()
    if (isAchieved) {
        var showDialog by remember { mutableStateOf(true) }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("🎉 Congratulations!") },
                text = { Text("You've reached your carbon reduction target! Keep up the great work!") },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Thanks!") }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
                textContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
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
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hello,", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = userProfile.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.navigate(Routes.SET_TARGET) }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Set Target",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CARBON SAVED", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$savedCO2",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("kg CO₂", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row {
                        Text("🚩 Target: ${userProfile.targetCO2} kg")
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val progress = if (userProfile.targetCO2 > 0) {
                        (savedCO2.toFloat() / userProfile.targetCO2).coerceIn(0f, 1f)
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
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${(progress * 100).toInt()}% to goal")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("TODAY'S MISSIONS", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskRow("🚲", "Cycle to Work", "Save 1.5kg CO2!") { viewModel.reduceCO2(2, "Cycle to Work") }
                TaskRow("🥗", "Plant-based Meal", "Earn 50 points.") { viewModel.reduceCO2(3, "Plant-based Meal") }
                TaskRow("🛍️", "No Plastic Bag", "Small steps matter!") { viewModel.reduceCO2(1, "No Plastic Bag") }
                TaskRow("💡", "Save Energy", "Turn off the AC for 2 hours.") { viewModel.reduceCO2(4, "Save Energy") }
            }

            if (customTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("YOUR CUSTOM TASKS", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    customTasks.forEach { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.reduceCO2(task.co2Reduction, task.name)
                                },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌟", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Reduce ${task.co2Reduction} kg CO₂",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = {
                                    viewModel.deleteCustomTask(task)
                                }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete task",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showAddTaskDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Your Own Task")
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // 美化弹窗
        if (showAddTaskDialog) {
            var taskName by remember { mutableStateOf("") }
            var reductionAmount by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddTaskDialog = false },
                title = { Text("New Custom Task", color = MaterialTheme.colorScheme.primary) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = taskName,
                            onValueChange = { taskName = it },
                            label = { Text("Task Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reductionAmount,
                            onValueChange = { reductionAmount = it },
                            label = { Text("CO₂ Reduction (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val amount = reductionAmount.toIntOrNull()
                        if (taskName.isNotBlank() && amount != null && amount > 0) {
                            viewModel.addCustomTask(taskName, amount)
                            showAddTaskDialog = false
                        }
                    }) { Text("Add", color = MaterialTheme.colorScheme.primary) }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary,
                textContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ------------------- ReportScreen -------------------
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val activityHistory by viewModel.activityHistory.collectAsState()
    val context = LocalContext.current

    val progress = if (userProfile.targetCO2 > 0) {
        userProfile.savedCO2.toFloat() / userProfile.targetCO2
    } else 0f

    // 明确类型：List<Triple<String, Int, Int>>
    val taskStats: List<Triple<String, Int, Int>> = remember(activityHistory) {
        activityHistory
            .filter { it.type == "Task" }
            .groupBy { it.description }
            .map { (name, records) ->
                val count = records.size
                val totalReduction = records.sumOf { -it.co2Change }
                Triple(name, count, totalReduction)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        Text("Your Report", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Actions: ${userProfile.totalActions}")
                Text("Total Reduced: ${userProfile.totalReduced} kg")
                Text("Total Emitted: ${userProfile.totalEmitted} kg")
                Text("Net Carbon Saved: ${userProfile.savedCO2} kg")
                Text("Target: ${userProfile.targetCO2} kg")
                LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )
                Text("${(progress * 100).toInt()}% to goal")
            }
        }

        if (taskStats.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Activity Breakdown", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            // 显式指定参数类型，避免 componentN 歧义
            taskStats.forEach { (name: String, count: Int, total: Int) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$name ×$count", modifier = Modifier.weight(1f))
                        Text("$total kg", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // === Recent Activities (with delete) ===
        if (activityHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Recent Activities", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }

            activityHistory.forEach { record ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 图标：减排🟢 / 排放🔴
                        Text(
                            if (record.co2Change < 0) "🟢" else "🔴",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                record.description.ifBlank { "Activity" },
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                "${dateFormat.format(Date(record.timestamp))}  ·  ${if (record.co2Change < 0) "-" else "+"}${Math.abs(record.co2Change)} kg CO₂",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (record.co2Change < 0)
                                    Color(0xFF2E7D32)  // green
                                else
                                    Color(0xFFC62828)  // red
                            )
                        }
                        // 删除按钮
                        IconButton(onClick = {
                            viewModel.deleteActivity(record)
                        }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete record",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val report = """
                    Carbon Footprint Report
                    ------------------------
                    Total Actions: ${userProfile.totalActions}
                    Total Reduced: ${userProfile.totalReduced} kg
                    Total Emitted: ${userProfile.totalEmitted} kg
                    Net Saved: ${userProfile.savedCO2} kg
                    Target: ${userProfile.targetCO2} kg
                    Progress: ${(progress * 100).toInt()}%
                """.trimIndent()

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, report)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Report"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export / Share Report")
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
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.updateDisplayName(tempName)
                navController.navigateUp()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Changes") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navController.navigate(Routes.SET_TARGET) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Set Carbon Target") }
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
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Set Your Carbon Target", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("You will be congratulated when your total reduced reaches this target.")
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
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
        ) { Text("Save Target") }
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
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Add Emission Log", style = MaterialTheme.typography.headlineSmall)
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
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Log") }
    }
}
@Composable
fun ChallengesScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    // 挑战列表：名称、图标、CO2 减少量、描述
    data class Challenge(
        val icon: String,
        val name: String,
        val co2Reduction: Int,
        val description: String
    )

    val challenges = listOf(
        Challenge("🥗", "Veggie Day", 3, "Eat only plant‑based meals today."),
        Challenge("💡", "Lights Off", 2, "Turn off all unnecessary lights for 2 hours."),
        Challenge("🚲", "Bike Trip", 4, "Use a bike instead of a car for one trip."),
        Challenge("🛍️", "No Plastic", 1, "Avoid single‑use plastics all day."),
        Challenge("🌳", "Plant a Tree", 5, "Plant or donate a tree (manual action)."),
    )

    // 已完成状态，用 mutableStateMapOf 跟踪哪些挑战已完成
    val completedChallenges = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = "Weekly Challenges",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Complete challenges to earn carbon reduction!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        challenges.forEach { challenge ->
            val isCompleted = completedChallenges[challenge.name] ?: false
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCompleted)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 图标 + 文字区域
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(challenge.icon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    challenge.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Reduce ${challenge.co2Reduction} kg CO₂",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            challenge.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 完成按钮
                    Button(
                        onClick = {
                            if (!isCompleted) {
                                viewModel.reduceCO2(challenge.co2Reduction, challenge.name)
                                completedChallenges[challenge.name] = true
                            }
                        },
                        enabled = !isCompleted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted)
                                MaterialTheme.colorScheme.outline
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isCompleted) "✓ Done" else "Do it!")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = { completedChallenges.clear() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Challenges (for demo)")
        }
    }
}