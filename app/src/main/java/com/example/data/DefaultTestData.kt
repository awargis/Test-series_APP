package com.example.data

object DefaultTestData {

    fun generateMainsTest(id: String, title: String, coaching: String): TestPaper {
        val configs = mutableListOf<QuestionConfig>()
        val answerKeys = mutableMapOf<Int, String>()

        val subjects = listOf("Physics", "Chemistry", "Mathematics")
        val chapters = mapOf(
            "Physics" to listOf("Electrostatics", "Rotational Dynamics", "Kinematics", "Thermodynamics", "Modern Physics"),
            "Chemistry" to listOf("Chemical Bonding", "Organic GOC", "Thermodynamics", "Coordination Compounds", "Solutions"),
            "Mathematics" to listOf("Calculus & Limits", "Matrices & Det", "Probability", "Coordinate Geometry", "Vectors 3D")
        )

        val optionsList = listOf("A", "B", "C", "D")

        var globalQNo = 1
        for (subj in subjects) {
            val chList = chapters[subj] ?: listOf("General")
            // 20 Single Correct
            for (i in 1..20) {
                val chapter = chList[i % chList.size]
                configs.add(
                    QuestionConfig(
                        qNo = globalQNo,
                        subject = subj,
                        chapter = chapter,
                        type = QuestionType.SINGLE_CORRECT,
                        options = optionsList,
                        marksCorrect = 4,
                        marksIncorrect = -1,
                        description = "Choose the correct option for $subj ($chapter)."
                    )
                )
                // Deterministic answer key
                val ans = optionsList[(globalQNo * 3 + i) % 4]
                answerKeys[globalQNo] = ans
                globalQNo++
            }
            // 10 Numerical Integer (attempt any 5 in Mains, we support entering integers)
            for (i in 1..10) {
                val chapter = chList[i % chList.size]
                configs.add(
                    QuestionConfig(
                        qNo = globalQNo,
                        subject = subj,
                        chapter = chapter,
                        type = QuestionType.NUMERICAL_INTEGER,
                        options = emptyList(), // no options for integer
                        marksCorrect = 4,
                        marksIncorrect = 0, // no negative for numerical section in Mains
                        description = "Enter the correct integer value for $subj ($chapter)."
                    )
                )
                val ans = ((globalQNo * 7) % 99).toString()
                answerKeys[globalQNo] = ans
                globalQNo++
            }
        }

        return TestPaper(
            id = id,
            title = title,
            coaching = coaching,
            testType = "MAINS",
            totalQuestions = configs.size,
            durationMinutes = 180,
            maxMarks = configs.size * 4,
            answerKeysJson = JsonParser.toJsonAnswerKeys(answerKeys),
            questionStructureJson = JsonParser.toJsonQuestionConfigs(configs),
            isDefault = true
        )
    }

    fun generateAdvancedTest(id: String, title: String, coaching: String): TestPaper {
        val configs = mutableListOf<QuestionConfig>()
        val answerKeys = mutableMapOf<Int, String>()

        val subjects = listOf("Physics", "Chemistry", "Mathematics")
        val chapters = mapOf(
            "Physics" to listOf("Electromagnetic Induction", "Fluid Mechanics", "Wave Optics", "Radioactivity", "Work Energy Power"),
            "Chemistry" to listOf("Chemical Kinetics", "Aldehydes & Ketones", "P-Block Elements", "Electrochemistry", "Ionic Equilibrium"),
            "Mathematics" to listOf("Definite Integration", "Permutation & Comb", "Complex Numbers", "Parabola & Hyperbola", "Differential Equations")
        )

        var globalQNo = 1
        for (subj in subjects) {
            val chList = chapters[subj] ?: listOf("General")

            // 1. Multi-correct (Q1 to Q6)
            for (i in 1..6) {
                val chapter = chList[i % chList.size]
                configs.add(
                    QuestionConfig(
                        qNo = globalQNo,
                        subject = subj,
                        chapter = chapter,
                        type = QuestionType.MULTI_CORRECT,
                        options = listOf("A", "B", "C", "D"),
                        marksCorrect = 4,
                        marksIncorrect = -2, // harsh marking in Advanced
                        description = "One or more options may be correct. Marks: Full (+4), Partial (+1 per correct), Zero (0), Incorrect (-2)."
                    )
                )
                // Multi-answers: e.g. "A,C", "B,D", "A,B,D", etc.
                val answers = mutableListOf<String>()
                if ((globalQNo) % 3 == 0) {
                    answers.add("A")
                    answers.add("C")
                } else if ((globalQNo) % 3 == 1) {
                    answers.add("B")
                    answers.add("D")
                } else {
                    answers.add("A")
                    answers.add("B")
                    answers.add("D")
                }
                answerKeys[globalQNo] = answers.joinToString(",")
                globalQNo++
            }

            // 2. Passages / Comprehension (Q7 to Q10) - Single Correct
            for (i in 1..4) {
                val chapter = chList[(i + 1) % chList.size]
                configs.add(
                    QuestionConfig(
                        qNo = globalQNo,
                        subject = subj,
                        chapter = chapter,
                        type = QuestionType.PASSAGE,
                        options = listOf("A", "B", "C", "D"),
                        marksCorrect = 3,
                        marksIncorrect = -1,
                        description = "Based on the given passage scenario for $subj. Choose the correct answer."
                    )
                )
                val ans = listOf("A", "B", "C", "D")[(globalQNo * 2) % 4]
                answerKeys[globalQNo] = ans
                globalQNo++
            }

            // 3. Integer Type (Q11 to Q14)
            for (i in 1..4) {
                val chapter = chList[(i + 2) % chList.size]
                configs.add(
                    QuestionConfig(
                        qNo = globalQNo,
                        subject = subj,
                        chapter = chapter,
                        type = QuestionType.NUMERICAL_INTEGER,
                        options = emptyList(),
                        marksCorrect = 3,
                        marksIncorrect = -1,
                        description = "The answer is a single digit or multi-digit integer."
                    )
                )
                val ans = ((globalQNo * 3) % 9).toString() // Single digit integer
                answerKeys[globalQNo] = ans
                globalQNo++
            }

            // 4. Matching Type (Q15 to Q18)
            for (i in 1..4) {
                val chapter = chList[(i + 3) % chList.size]
                configs.add(
                    QuestionConfig(
                        qNo = globalQNo,
                        subject = subj,
                        chapter = chapter,
                        type = QuestionType.MATCHING,
                        options = listOf("1-P", "1-Q", "1-R", "1-S", "2-P", "2-Q", "2-R", "2-S", "3-P", "3-Q", "3-R", "3-S", "4-P", "4-Q", "4-R", "4-S"),
                        marksCorrect = 3,
                        marksIncorrect = -1,
                        description = "Match List-I (1, 2, 3, 4) with List-II (P, Q, R, S). Format: 1-P,2-Q,3-R,4-S"
                    )
                )
                // Match key: e.g. "1-P,2-Q,3-R,4-S"
                val list1 = listOf("1", "2", "3", "4")
                val list2 = listOf("P", "Q", "R", "S")
                // offset matching
                val matches = list1.mapIndexed { idx, item ->
                    val target = list2[(idx + globalQNo) % list2.size]
                    "$item-$target"
                }
                answerKeys[globalQNo] = matches.joinToString(",")
                globalQNo++
            }
        }

        return TestPaper(
            id = id,
            title = title,
            coaching = coaching,
            testType = "ADVANCED",
            totalQuestions = configs.size,
            durationMinutes = 180,
            maxMarks = 198, // typical JEE Advanced paper score
            answerKeysJson = JsonParser.toJsonAnswerKeys(answerKeys),
            questionStructureJson = JsonParser.toJsonQuestionConfigs(configs),
            isDefault = true
        )
    }

    fun getDefaults(): List<TestPaper> {
        return listOf(
            generateMainsTest("pw_mains_1", "PW All India Test Series (Mains) - Test 01", "PW"),
            generateMainsTest("allen_mains_1", "Allen Leader Test Series (Mains) - Test 01", "Allen"),
            generateAdvancedTest("allen_adv_1", "Allen Score Test Series (Advanced) - Paper 01", "Allen"),
            generateAdvancedTest("fiitjee_adv_1", "FIITJEE AITS (Advanced) - Part Test 01", "FIITJEE"),
            generateMainsTest("fiitjee_mains_1", "FIITJEE AITS (Mains) - Part Test 01", "FIITJEE")
        )
    }
}
