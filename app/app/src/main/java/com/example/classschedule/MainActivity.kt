package com.example.classschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classschedule.ui.ScheduleScreen
import com.example.classschedule.ui.SettingsScreen
import com.example.classschedule.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val vm: ScheduleViewModel = viewModel()
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Color.White,
        bottomBar = { BottomBar(selected = tab, onSelect = { tab = it }) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> ScheduleScreen(vm)
                else -> SettingsScreen(vm)
            }
        }
    }
}

/** 底部导航：课程表 / 设置 两个 Tab */
@Composable
private fun BottomBar(selected: Int, onSelect: (Int) -> Unit) {
    Column {
        HorizontalDivider(color = Color(0xFFEEEEEE))
        Row(
            Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.White)
        ) {
            NavItem(
                modifier = Modifier.weight(1f),
                selected = selected == 0,
                selectedIcon = Icons.Filled.DateRange,
                unselectedIcon = Icons.Outlined.DateRange,
                label = "课程表",
                onClick = { onSelect(0) }
            )
            NavItem(
                modifier = Modifier.weight(1f),
                selected = selected == 1,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                label = "设置",
                onClick = { onSelect(1) }
            )
        }
    }
}

@Composable
private fun NavItem(
    modifier: Modifier,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = if (selected) Color(0xFF111111) else Color(0xFF999999),
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.height(4.dp))
        // 选中态小圆点指示器
        Box(
            Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (selected) Color(0xFF111111) else Color.Transparent)
        )
        Spacer(Modifier.weight(1f))
    }
}
