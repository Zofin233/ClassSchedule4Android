package com.example.classschedule.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classschedule.cdl.CdlParser
import com.example.classschedule.cdl.CdlRecord
import com.example.classschedule.cdl.CdlResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 导入课程对话框：选择 CDL 文本文件或粘贴文本 → 解析预览 → 确认导入。
 */
@Composable
fun ImportDialog(
    onDismiss: () -> Unit,
    onImport: (List<CdlRecord>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<CdlResult?>(null) }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val content = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        input.bufferedReader().readText()
                    } ?: ""
                }
                result = CdlParser.parse(content)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入课程") },
        text = {
            val r = result
            if (r == null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "每行一门课的一个时段：\n课程名 @ 地点 | 周次 | 星期 | 节次",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("粘贴课程文本") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TextButton(onClick = { fileLauncher.launch(arrayOf("text/*")) }) {
                            Text("选择文件")
                        }
                        TextButton(
                            onClick = { result = CdlParser.parse(text) },
                            enabled = text.isNotBlank()
                        ) { Text("解析文本") }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "解析出 ${r.records.size} 条记录" +
                            if (r.errors.isNotEmpty()) "，${r.errors.size} 行有误" else "",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LazyColumn(Modifier.heightIn(max = 300.dp)) {
                        items(r.records) { rec ->
                            Column(Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    "${rec.name}" + if (rec.location.isNotBlank()) "@${rec.location}" else "",
                                    fontSize = 13.sp
                                )
                                Text(
                                    "第${rec.weeks}周 · 周${"一二三四五六日"[rec.dayOfWeek - 1]} · ${rec.startSection}-${rec.endSection}节",
                                    fontSize = 11.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                        }
                        items(r.errors) { err ->
                            Text(
                                "第${err.line}行：${err.reason}\n${err.content}",
                                fontSize = 11.sp,
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val r = result
            if (r == null) {
                TextButton(onClick = onDismiss) { Text("取消") }
            } else {
                TextButton(
                    onClick = { onImport(r.records) },
                    enabled = r.records.isNotEmpty()
                ) { Text("确认导入") }
            }
        },
        dismissButton = {
            if (result != null) {
                TextButton(onClick = { result = null }) { Text("重新选择") }
            }
        }
    )
}
