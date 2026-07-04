package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class TestRepository(private val testDao: TestDao) {

    val allStudents: Flow<List<Student>> = testDao.getAllStudents()

    val allTestPapers: Flow<List<TestPaper>> = testDao.getAllTestPapers()
        .onStart {
            // Guard against empty DB by forcing pre-population
            try {
                val current = testDao.getAllTestPapers().first()
                if (current.isEmpty()) {
                    DefaultTestData.getDefaults().forEach {
                        testDao.insertTestPaper(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    val allAttempts: Flow<List<StudentAttempt>> = testDao.getAllAttempts()

    fun getStudentByMobile(mobile: String): Flow<Student?> = testDao.getStudentByMobile(mobile)

    suspend fun getStudentByMobileDirect(mobile: String): Student? = withContext(Dispatchers.IO) {
        testDao.getStudentByMobileDirect(mobile)
    }

    suspend fun registerStudent(student: Student) = withContext(Dispatchers.IO) {
        testDao.insertStudent(student)
    }

    fun getTestPapersByCoaching(coaching: String): Flow<List<TestPaper>> = testDao.getTestPapersByCoaching(coaching)

    fun getTestPaperById(id: String): Flow<TestPaper?> = testDao.getTestPaperById(id)

    suspend fun getTestPaperByIdDirect(id: String): TestPaper? = withContext(Dispatchers.IO) {
        testDao.getTestPaperByIdDirect(id)
    }

    suspend fun insertTestPaper(testPaper: TestPaper) = withContext(Dispatchers.IO) {
        testDao.insertTestPaper(testPaper)
    }

    fun getAttemptsForStudent(mobile: String): Flow<List<StudentAttempt>> = testDao.getAttemptsForStudent(mobile)

    fun getAttempt(id: String): Flow<StudentAttempt?> = testDao.getAttempt(id)

    suspend fun getAttemptDirect(id: String): StudentAttempt? = withContext(Dispatchers.IO) {
        testDao.getAttemptDirect(id)
    }

    suspend fun saveAttempt(attempt: StudentAttempt) = withContext(Dispatchers.IO) {
        testDao.insertAttempt(attempt)
    }
}
