package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey val mobile: String,
    val name: String,
    val enrolledCoaching: String, // "PW", "Allen", "FIITJEE", "Other"
    val registrationTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_papers")
data class TestPaper(
    @PrimaryKey val id: String,
    val title: String,
    val coaching: String, // "PW", "Allen", "FIITJEE", "Other"
    val testType: String, // "MAINS" or "ADVANCED"
    val totalQuestions: Int,
    val durationMinutes: Int,
    val maxMarks: Int,
    val answerKeysJson: String, // Map<Int, String> (QNo -> Correct Answer String)
    val questionStructureJson: String, // List<QuestionConfig>
    val isDefault: Boolean = false
)

@Entity(tableName = "student_attempts")
data class StudentAttempt(
    @PrimaryKey val id: String, // "${studentMobile}_${testId}"
    val studentMobile: String,
    val testId: String,
    val selectedAnswersJson: String, // Map<Int, String> (QNo -> Student Answer)
    val isSubmitted: Boolean,
    val startTime: Long,
    val endTime: Long,
    val score: Int,
    val maxScore: Int,
    val subjectScoresJson: String, // Map<String, Int> (Subject Name -> Score)
    val accuracy: Float, // Correct Answers / Total Answered Questions
    val weakChaptersJson: String, // List<String>
    val strongChaptersJson: String, // List<String>
    val responseTimesJson: String // Map<Int, Int> (QNo -> Time Spent in seconds)
)
