package com.example.classschedule.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classschedule.ScheduleViewModel
import java.time.LocalDate

/** 设置页：学期设置 + 数据管理 */
@Composable
fun SettingsScreen(vm: ScheduleViewModel) {
    var startText by remember { mutableStateOf(vm.semesterStart.toString()) }
    var weeksText by remember { mutableStateOf(vm.totalWeeks.toString()) }
    var message by remember { mutableStateOf<String?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    val courses by vm.courses.collectAsState()
    val week by vm.week.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("设置", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Text("学期设置", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF666666))
        OutlinedTextField(
            value = startText,
            onValueChange = { startText = it },
            label = { Text("学期开始日期（yyyy-MM-dd，自动对齐到周一）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = weeksText,
            onValueChange = { weeksText = it },
            label = { Text("学期总周数（1-30）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = {
                val date = runCatching { LocalDate.parse(startText.trim()) }.getOrNull()
                val weeks = weeksText.toIntOrNull()
                when {
                    date == null -> message = "日期格式无效，应为 yyyy-MM-dd"
                    weeks == null || weeks < 1 || weeks > 30 -> message = "总周数应在 1-30 之间"
                    else -> {
                        vm.setTotalWeeks(weeks)
                        vm.setSemesterStart(date)
                        startText = vm.semesterStart.toString()
                        message = "已保存：学期从 ${vm.semesterStart}（周一）开始，共 ${vm.totalWeeks} 周"
                    }
                }
            }) { Text("保存学期设置") }
        }
        Text(
            "当前显示：第${week}周 · ${vm.semesterLabel()} · 已有 ${courses.size} 条课程记录",
            fontSize = 12.sp,
            color = Color(0xFF888888)
        )

        HorizontalDivider()

        Text("数据", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF666666))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = {
                vm.insertSamples()
                message = "已导入示例课程"
            }) { Text("导入示例课程") }
            TextButton(onClick = { confirmClear = true }) {
                Text("清空全部课程", color = Color(0xFFD32F2F))
            }
        }

        message?.let { Text(it, fontSize = 12.sp, color = Color(0xFF666666)) }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清空全部课程") },
            text = { Text("将删除所有课程记录，且不可恢复。确定继续吗？") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAll()
                    confirmClear = false
                    message = "已清空全部课程"
                }) { Text("清空", color = Color(0xFFD32F2F)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消") }
            }
        )
    }
}
