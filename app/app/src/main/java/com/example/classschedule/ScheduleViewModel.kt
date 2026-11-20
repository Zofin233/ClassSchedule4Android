package com.example.classschedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.classschedule.cdl.CdlParser
import com.example.classschedule.cdl.CdlRecord
import com.example.classschedule.data.AppDatabase
import com.example.classschedule.data.CourseEntity
import com.example.classschedule.data.CourseRepository
import com.example.classschedule.data.SettingsStore
import com.example.classschedule.data.WeekSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class ScheduleViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = SettingsStore(app)
    private val repo = CourseRepository(AppDatabase.get(app).courseDao())

    val courses: StateFlow<List<CourseEntity>> =
        repo.all.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _week = MutableStateFlow(settings.currentWeek())
    val week: StateFlow<Int> = _week

    val totalWeeks: Int get() = settings.totalWeeks
    val semesterStart: LocalDate get() = settings.semesterStart

    /** 选中周所在周的周一日期 */
    fun mondayOf(week: Int): LocalDate = semesterStart.plusWeeks((week - 1).toLong())

    /** 学期标签，如 "2026-2027 第1学期"（按开始月份推断：下半年入学为第1学期） */
    fun semesterLabel(): String {
        val s = semesterStart
        val term = if (s.monthValue >= 7) 1 else 2
        val y = if (term == 1) s.year else s.year - 1
        return "$y-${y + 1} 第${term}学期"
    }

    fun setWeek(w: Int) {
        _week.value = w.coerceIn(1, settings.totalWeeks)
    }

    fun setSemesterStart(date: LocalDate) {
        settings.semesterStart = date
        _week.value = settings.currentWeek()
    }

    fun setTotalWeeks(n: Int) {
        settings.totalWeeks = n
        _week.value = _week.value.coerceIn(1, settings.totalWeeks)
    }

    fun addCourse(
        name: String, location: String, teacher: String, dayOfWeek: Int,
        startSection: Int, endSection: Int, weeks: String, colorIndex: Int
    ) {
        viewModelScope.launch {
            repo.add(
                CourseEntity(
                    name = name.trim(),
                    location = location.trim(),
                    teacher = teacher.trim(),
                    dayOfWeek = dayOfWeek,
                    startSection = startSection,
                    endSection = endSection,
                    weeks = WeekSpec.normalize(weeks),
                    colorIndex = colorIndex
                )
            )
        }
    }

    fun updateCourse(course: CourseEntity) {
        viewModelScope.launch {
            repo.update(course.copy(weeks = WeekSpec.normalize(course.weeks)))
        }
    }

    fun import(records: List<CdlRecord>) {
        viewModelScope.launch { repo.import(records) }
    }

    fun deleteCourse(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }

    fun clearAll() {
        viewModelScope.launch { repo.clear() }
    }

    /** 导入内置示例课程（复用 CDL 解析器） */
    fun insertSamples() {
        viewModelScope.launch { repo.import(CdlParser.parse(SAMPLE_CDL).records) }
    }

    companion object {
        val SAMPLE_CDL = """
            # 2026-2027 第1学期示例课表
            数据结构与算法II @ 品学楼A302 | 1-16 | 周一 | 3-4 | 李老师
            大学物理II @ 立人楼B107 | 1-16 | 周二 | 3-4 | 付传技
            大学物理II @ 立人楼B107 | 1-16 | 周四 | 5-6 | 付传技
            游泳C @ 游泳馆 | 1-8 | 周一 | 5-6
            概率论与数理统计 @ 品学楼A309 | 1-16 | 周二 | 5-6
            计算机组成与系统结构II @ 品学楼B208 | 1-16 | 周三 | 5-6
            电装实习 @ 研究院大楼4楼电装基地 | 3,5,7 | 周三 | 7-8
            网络与系统安全I @ 立人楼B304 | 1-16 | 周四 | 7-8
        """.trimIndent()
    }
}
