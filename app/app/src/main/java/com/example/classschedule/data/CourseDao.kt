package com.example.classschedule.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses")
    fun all(): Flow<List<CourseEntity>>

    @Insert
    suspend fun insert(course: CourseEntity): Long

    @Insert
    suspend fun insertAll(courses: List<CourseEntity>)

    @Update
    suspend fun update(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM courses")
    suspend fun clear()

    @Query("SELECT colorIndex FROM courses WHERE name = :name LIMIT 1")
    suspend fun colorOfName(name: String): Int?
}
