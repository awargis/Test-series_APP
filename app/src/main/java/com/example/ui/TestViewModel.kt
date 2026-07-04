package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TestRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TestRepository(database.testDao())
    }

    // Registered students for teacher monitoring
    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All available test papers
    val allTestPapers: StateFlow<List<TestPaper>> = repository.allTestPapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All attempts for teacher review
    val allAttempts: StateFlow<List<StudentAttempt>> = repository.allAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current logged in student
    private val _currentMobile = MutableStateFlow<String?>(null)
    val currentMobile: StateFlow<String?> = _currentMobile.asStateFlow()

    val currentStudent: StateFlow<Student?> = _currentMobile
        .flatMapLatest { mobile ->
            if (mobile == null) flowOf(null)
            else repository.getStudentByMobile(mobile)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filtered test papers based on enrolled coaching / coaching selection
    private val _selectedCoaching = MutableStateFlow<String>("All")
    val selectedCoaching: StateFlow<String> = _selectedCoaching.asStateFlow()

    val filteredTestPapers: StateFlow<List<TestPaper>> = combine(
        allTestPapers,
        _selectedCoaching,
        currentStudent
    ) { papers, coaching, student ->
        val filterCoaching = if (coaching == "All") {
            student?.enrolledCoaching ?: "All"
        } else {
            coaching
        }
        if (filterCoaching == "All") papers
        else papers.filter { it.coaching.lowercase() == filterCoaching.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current student's test attempts
    val studentAttempts: StateFlow<List<StudentAttempt>> = _currentMobile
        .flatMapLatest { mobile ->
            if (mobile == null) flowOf(emptyList())
            else repository.getAttemptsForStudent(mobile)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active test solving state
    private val _activeTest = MutableStateFlow<TestPaper?>(null)
    val activeTest: StateFlow<TestPaper?> = _activeTest.asStateFlow()

    private val _activeAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val activeAnswers: StateFlow<Map<Int, String>> = _activeAnswers.asStateFlow()

    private val _testTimeRemaining = MutableStateFlow<Int>(0)
    val testTimeRemaining: StateFlow<Int> = _testTimeRemaining.asStateFlow()

    private var timerJob: Job? = null
    private var testStartTime: Long = 0

    // Admin state for creating a custom test
    val adminTitle = MutableStateFlow("")
    val adminCoaching = MutableStateFlow("PW")
    val adminType = MutableStateFlow("MAINS")
    val adminQuestionCount = MutableStateFlow("15") // short test by default
    val adminAnswerKeyText = MutableStateFlow("") // format: Q1: A, Q2: B, Q3: 15 etc.

    // Navigation state
    private val _currentScreen = MutableStateFlow<String>("home") // home, register, tests, test_solve, test_results, teacher_panel, admin_panel
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // View historical test result
    private val _viewedAttempt = MutableStateFlow<StudentAttempt?>(null)
    val viewedAttempt: StateFlow<StudentAttempt?> = _viewedAttempt.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun selectCoaching(coaching: String) {
        _selectedCoaching.value = coaching
    }

    fun loginOrRegister(name: String, mobile: String, coaching: String) {
        if (name.isBlank() || mobile.isBlank()) return
        viewModelScope.launch {
            val existing = repository.getStudentByMobileDirect(mobile)
            if (existing == null) {
                val newStudent = Student(mobile = mobile, name = name, enrolledCoaching = coaching)
                repository.registerStudent(newStudent)
            }
            _currentMobile.value = mobile
            _currentScreen.value = "home"
        }
    }

    fun logout() {
        _currentMobile.value = null
        _currentScreen.value = "register"
    }

    fun startTest(testPaper: TestPaper) {
        val student = currentStudent.value ?: return
        val attemptId = "${student.mobile}_${testPaper.id}"

        viewModelScope.launch {
            val existingAttempt = repository.getAttemptDirect(attemptId)
            if (existingAttempt != null && existingAttempt.isSubmitted) {
                // Already submitted, can only view
                _viewedAttempt.value = existingAttempt
                _activeTest.value = testPaper
                _currentScreen.value = "test_results"
            } else {
                _activeTest.value = testPaper
                _activeAnswers.value = if (existingAttempt != null) {
                    JsonParser.fromJsonAnswerKeys(existingAttempt.selectedAnswersJson)
                } else {
                    emptyMap()
                }
                _testTimeRemaining.value = testPaper.durationMinutes * 60
                testStartTime = System.currentTimeMillis()

                // Save initial state to DB if not exists
                if (existingAttempt == null) {
                    val initialAttempt = StudentAttempt(
                        id = attemptId,
                        studentMobile = student.mobile,
                        testId = testPaper.id,
                        selectedAnswersJson = "{}",
                        isSubmitted = false,
                        startTime = testStartTime,
                        endTime = 0,
                        score = 0,
                        maxScore = testPaper.maxMarks,
                        subjectScoresJson = "{}",
                        accuracy = 0f,
                        weakChaptersJson = "[]",
                        strongChaptersJson = "[]",
                        responseTimesJson = "{}"
                    )
                    repository.saveAttempt(initialAttempt)
                }

                startTimer()
                _currentScreen.value = "test_solve"
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_testTimeRemaining.value > 0) {
                delay(1000)
                _testTimeRemaining.value -= 1
            }
            // Auto-submit when timer expires
            submitTest()
        }
    }

    fun saveAnswerChoice(qNo: Int, answer: String) {
        val answers = _activeAnswers.value.toMutableMap()
        if (answer.isEmpty()) {
            answers.remove(qNo)
        } else {
            answers[qNo] = answer
        }
        _activeAnswers.value = answers

        // Autosave current answers to DB
        val student = currentStudent.value ?: return
        val test = _activeTest.value ?: return
        val attemptId = "${student.mobile}_${test.id}"
        viewModelScope.launch {
            val current = repository.getAttemptDirect(attemptId) ?: return@launch
            if (!current.isSubmitted) {
                val updated = current.copy(
                    selectedAnswersJson = JsonParser.toJsonAnswerKeys(answers)
                )
                repository.saveAttempt(updated)
            }
        }
    }

    fun submitTest() {
        timerJob?.cancel()
        val student = currentStudent.value ?: return
        val test = _activeTest.value ?: return
        val attemptId = "${student.mobile}_${test.id}"

        viewModelScope.launch {
            val currentAnswers = _activeAnswers.value
            val correctAnswers = JsonParser.fromJsonAnswerKeys(test.answerKeysJson)
            val questionConfigs = JsonParser.fromJsonQuestionConfigs(test.questionStructureJson)

            var totalScore = 0
            val subjectScores = mutableMapOf<String, Int>()
            val chapterAttempts = mutableMapOf<String, MutableList<Boolean>>() // Chapter -> List of Correct/Incorrect status

            questionConfigs.forEach { config ->
                val qNo = config.qNo
                val studentAns = currentAnswers[qNo]
                val correctAns = correctAnswers[qNo] ?: ""

                val subj = config.subject
                val chapter = config.chapter

                if (studentAns != null && studentAns.isNotBlank()) {
                    var isCorrect = false
                    var qScore = 0

                    when (config.type) {
                        QuestionType.SINGLE_CORRECT, QuestionType.PASSAGE -> {
                            if (studentAns.trim().lowercase() == correctAns.trim().lowercase()) {
                                isCorrect = true
                                qScore = config.marksCorrect
                            } else {
                                qScore = config.marksIncorrect
                            }
                        }
                        QuestionType.NUMERICAL_INTEGER -> {
                            if (studentAns.trim() == correctAns.trim()) {
                                isCorrect = true
                                qScore = config.marksCorrect
                            } else {
                                qScore = config.marksIncorrect
                            }
                        }
                        QuestionType.MULTI_CORRECT -> {
                            // Split answers
                            val studentSet = studentAns.split(",").map { it.trim().uppercase() }.toSet()
                            val correctSet = correctAns.split(",").map { it.trim().uppercase() }.toSet()

                            if (studentSet == correctSet) {
                                isCorrect = true
                                qScore = config.marksCorrect // Full marks (+4)
                            } else {
                                // check if subset and no incorrect options chosen
                                val hasIncorrect = studentSet.any { !correctSet.contains(it) }
                                if (!hasIncorrect && studentSet.isNotEmpty()) {
                                    isCorrect = true // Partially correct
                                    qScore = studentSet.size // Partial marks (+1 per option)
                                } else {
                                    qScore = config.marksIncorrect // -2
                                }
                            }
                        }
                        QuestionType.MATCHING -> {
                            // Full match of all components, e.g. "1-P,2-Q,3-R,4-S"
                            val studentSet = studentAns.split(",").map { it.trim() }.toSet()
                            val correctSet = correctAns.split(",").map { it.trim() }.toSet()
                            if (studentSet == correctSet) {
                                isCorrect = true
                                qScore = config.marksCorrect
                            } else {
                                qScore = config.marksIncorrect
                            }
                        }
                    }

                    totalScore += qScore
                    subjectScores[subj] = (subjectScores[subj] ?: 0) + qScore

                    val chStats = chapterAttempts.getOrPut(chapter) { mutableListOf() }
                    chStats.add(isCorrect)
                } else {
                    // Unanswered
                    subjectScores[subj] = (subjectScores[subj] ?: 0) + 0
                }
            }

            // Calculate weak and strong chapters
            val weakChapters = mutableListOf<String>()
            val strongChapters = mutableListOf<String>()

            chapterAttempts.forEach { (chapter, attempts) ->
                val correctCount = attempts.count { it }
                val accuracyRate = correctCount.toFloat() / attempts.size
                if (accuracyRate >= 0.7f) {
                    strongChapters.add(chapter)
                } else if (accuracyRate < 0.5f) {
                    weakChapters.add(chapter)
                }
            }

            val totalAnswered = currentAnswers.filterValues { it.isNotBlank() }.size
            val correctAnswersCount = questionConfigs.count { config ->
                val ans = currentAnswers[config.qNo]
                val correct = correctAnswers[config.qNo] ?: ""
                ans != null && ans.trim().lowercase() == correct.trim().lowercase()
            }
            val accuracy = if (totalAnswered > 0) correctAnswersCount.toFloat() / totalAnswered else 0f

            val completedAttempt = StudentAttempt(
                id = attemptId,
                studentMobile = student.mobile,
                testId = test.id,
                selectedAnswersJson = JsonParser.toJsonAnswerKeys(currentAnswers),
                isSubmitted = true,
                startTime = testStartTime,
                endTime = System.currentTimeMillis(),
                score = totalScore,
                maxScore = test.maxMarks,
                subjectScoresJson = JsonParser.toJsonMap(subjectScores),
                accuracy = accuracy,
                weakChaptersJson = JsonParser.toJsonStringList(weakChapters),
                strongChaptersJson = JsonParser.toJsonStringList(strongChapters),
                responseTimesJson = "{}"
            )

            repository.saveAttempt(completedAttempt)
            _viewedAttempt.value = completedAttempt
            _currentScreen.value = "test_results"
        }
    }

    fun viewAttemptResult(attempt: StudentAttempt) {
        viewModelScope.launch {
            val test = repository.getTestPaperByIdDirect(attempt.testId) ?: return@launch
            _viewedAttempt.value = attempt
            _activeTest.value = test
            _currentScreen.value = "test_results"
        }
    }

    // Creating a customizable test series (PW / Allen format customizable test builder)
    fun createCustomTestPaper() {
        val title = adminTitle.value.ifBlank { "Custom Test Paper" }
        val coaching = adminCoaching.value
        val type = adminType.value
        val qCountStr = adminQuestionCount.value.toIntOrNull() ?: 15
        val answerKeyInput = adminAnswerKeyText.value

        viewModelScope.launch {
            val testId = "custom_${System.currentTimeMillis()}"
            val configs = mutableListOf<QuestionConfig>()
            val answerKeys = mutableMapOf<Int, String>()

            val subjects = listOf("Physics", "Chemistry", "Mathematics")
            val chapters = mapOf(
                "Physics" to listOf("Electrostatics", "Magnetism", "Optics", "Mechanics"),
                "Chemistry" to listOf("Inorganic", "Physical", "Organic GOC"),
                "Mathematics" to listOf("Algebra", "Calculus", "Probability")
            )

            // Parse text answers if provided, or auto-generate keys if empty
            // Expected format: "1: A\n2: B\n3: A,C\n4: 15\n5: 1-P,2-Q"
            val parsedAnswers = mutableMapOf<Int, String>()
            if (answerKeyInput.isNotBlank()) {
                answerKeyInput.lines().forEach { line ->
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2) {
                        val num = parts[0].trim().toIntOrNull()
                        val ans = parts[1].trim()
                        if (num != null && ans.isNotBlank()) {
                            parsedAnswers[num] = ans
                        }
                    }
                }
            }

            var globalQNo = 1
            for (i in 1..qCountStr) {
                val subject = subjects[(i - 1) % subjects.size]
                val chList = chapters[subject] ?: listOf("General")
                val chapter = chList[i % chList.size]

                // Determine question type based on index or parsing
                val parsedAns = parsedAnswers[i]
                val qType = when {
                    parsedAns != null && parsedAns.contains("-") -> QuestionType.MATCHING
                    parsedAns != null && parsedAns.contains(",") -> QuestionType.MULTI_CORRECT
                    parsedAns != null && parsedAns.toIntOrNull() != null -> QuestionType.NUMERICAL_INTEGER
                    type == "ADVANCED" && i % 4 == 0 -> QuestionType.MULTI_CORRECT
                    type == "ADVANCED" && i % 4 == 1 -> QuestionType.PASSAGE
                    type == "ADVANCED" && i % 4 == 2 -> QuestionType.NUMERICAL_INTEGER
                    type == "ADVANCED" && i % 4 == 3 -> QuestionType.MATCHING
                    else -> if (i % 5 == 0) QuestionType.NUMERICAL_INTEGER else QuestionType.SINGLE_CORRECT
                }

                configs.add(
                    QuestionConfig(
                        qNo = i,
                        subject = subject,
                        chapter = chapter,
                        type = qType,
                        options = if (qType == QuestionType.MATCHING) {
                            listOf("1-P", "1-Q", "1-R", "1-S", "2-P", "2-Q", "2-R", "2-S", "3-P", "3-Q", "3-R", "3-S", "4-P", "4-Q", "4-R", "4-S")
                        } else if (qType == QuestionType.NUMERICAL_INTEGER) {
                            emptyList()
                        } else {
                            listOf("A", "B", "C", "D")
                        },
                        marksCorrect = if (type == "ADVANCED") 4 else 4,
                        marksIncorrect = if (type == "ADVANCED") -2 else -1,
                        description = "Custom solver item for $subject ($chapter)."
                    )
                )

                // Fill correct answers
                if (parsedAns != null) {
                    answerKeys[i] = parsedAns
                } else {
                    // Auto generate
                    answerKeys[i] = when (qType) {
                        QuestionType.SINGLE_CORRECT, QuestionType.PASSAGE -> listOf("A", "B", "C", "D")[i % 4]
                        QuestionType.NUMERICAL_INTEGER -> (i * 3 % 20).toString()
                        QuestionType.MULTI_CORRECT -> if (i % 2 == 0) "A,C" else "B,D"
                        QuestionType.MATCHING -> "1-P,2-Q,3-R,4-S"
                    }
                }
            }

            val testPaper = TestPaper(
                id = testId,
                title = title,
                coaching = coaching,
                testType = type,
                totalQuestions = configs.size,
                durationMinutes = if (type == "ADVANCED") 180 else 180,
                maxMarks = configs.size * 4,
                answerKeysJson = JsonParser.toJsonAnswerKeys(answerKeys),
                questionStructureJson = JsonParser.toJsonQuestionConfigs(configs),
                isDefault = false
            )

            repository.insertTestPaper(testPaper)

            // Reset admin state
            adminTitle.value = ""
            adminAnswerKeyText.value = ""

            // Navigate back
            _currentScreen.value = "tests"
        }
    }
}
