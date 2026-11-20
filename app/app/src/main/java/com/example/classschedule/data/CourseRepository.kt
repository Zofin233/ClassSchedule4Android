package com.example.classschedule.data

import com.example.classschedule.cdl.CdlRecord
import com.example.classschedule.ui.theme.CourseColors
import kotlinx.coroutines.flow.Flow
import kotlin.math.abs

class CourseRepository(private val dao: CourseDao) {

    val all: Flow<List<CourseEntity>> = dao.all()

    suspend fun add(course: CourseEntity): Long = dao.insert(course)

    suspend fun update(course: CourseEntity) = dao.update(course)

    /** 批量导入 CDL 记录：同名课程沿用已有颜色，新课程按名称散列取色 */
    suspend fun import(records: List<CdlRecord>) {
        val entities = records.map { r ->
            val color = dao.colorOfName(r.name)
                ?: (abs(r.name.hashCode()) % CourseColors.palette.size)
            CourseEntity(
                name = r.name,
                location = r.location,
                teacher = r.teacher,
                dayOfWeek = r.dayOfWeek,
                startSection = r.startSection,
                endSection = r.endSection,
                weeks = r.weeks,
                colorIndex = color
            )
        }
        dao.insertAll(entities)
    }

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun clear() = dao.clear()
}
