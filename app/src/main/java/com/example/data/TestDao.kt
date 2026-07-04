package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {

    // Students
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Query("SELECT * FROM students WHERE mobile = :mobile LIMIT 1")
    fun getStudentByMobile(mobile: String): Flow<Student?>

    @Query("SELECT * FROM students WHERE mobile = :mobile LIMIT 1")
    suspend fun getStudentByMobileDirect(mobile: String): Student?

    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<Student>>

    // Test Papers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestPaper(testPaper: TestPaper)

    @Query("SELECT * FROM test_papers")
    fun getAllTestPapers(): Flow<List<TestPaper>>

    @Query("SELECT * FROM test_papers WHERE id = :id LIMIT 1")
    fun getTestPaperById(id: String): Flow<TestPaper?>

    @Query("SELECT * FROM test_papers WHERE id = :id LIMIT 1")
    suspend fun getTestPaperByIdDirect(id: String): TestPaper?

    @Query("SELECT * FROM test_papers WHERE coaching = :coaching")
    fun getTestPapersByCoaching(coaching: String): Flow<List<TestPaper>>

    // Student Attempts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: StudentAttempt)

    @Query("SELECT * FROM student_attempts WHERE id = :id LIMIT 1")
    fun getAttempt(id: String): Flow<StudentAttempt?>

    @Query("SELECT * FROM student_attempts WHERE id = :id LIMIT 1")
    suspend fun getAttemptDirect(id: String): StudentAttempt?

    @Query("SELECT * FROM student_attempts WHERE studentMobile = :mobile")
    fun getAttemptsForStudent(mobile: String): Flow<List<StudentAttempt>>

    @Query("SELECT * FROM student_attempts WHERE testId = :testId")
    fun getAttemptsForTest(testId: String): Flow<List<StudentAttempt>>

    @Query("SELECT * FROM student_attempts")
    fun getAllAttempts(): Flow<List<StudentAttempt>>
}
