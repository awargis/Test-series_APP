package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

enum class TestType {
    MAINS, ADVANCED
}

enum class QuestionType {
    SINGLE_CORRECT,
    MULTI_CORRECT,
    NUMERICAL_INTEGER,
    MATCHING,
    PASSAGE
}

data class QuestionConfig(
    val qNo: Int,
    val subject: String, // "Physics", "Chemistry", "Mathematics"
    val chapter: String, // e.g. "Electrostatics", "Chemical Bonding", "Calculus"
    val type: QuestionType,
    val options: List<String> = listOf("A", "B", "C", "D"),
    val description: String = "", // Optional text representation for passage etc.
    val marksCorrect: Int = 4,
    val marksIncorrect: Int = -1
)

data class AnswerKeyItem(
    val qNo: Int,
    val correctAnswer: String, // For multi: "A,B", single: "C", integer: "15", matching: "1-P,2-Q,3-R,4-S"
    val type: QuestionType
)

object JsonParser {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun toJsonQuestionConfigs(configs: List<QuestionConfig>): String {
        val type = Types.newParameterizedType(List::class.java, QuestionConfig::class.java)
        return moshi.adapter<List<QuestionConfig>>(type).toJson(configs) ?: "[]"
    }

    fun fromJsonQuestionConfigs(json: String): List<QuestionConfig> {
        val type = Types.newParameterizedType(List::class.java, QuestionConfig::class.java)
        return try {
            moshi.adapter<List<QuestionConfig>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toJsonAnswerKeys(keys: Map<Int, String>): String {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        val stringMap = keys.mapKeys { it.key.toString() }
        return moshi.adapter<Map<String, String>>(type).toJson(stringMap) ?: "{}"
    }

    fun fromJsonAnswerKeys(json: String): Map<Int, String> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        return try {
            val map = moshi.adapter<Map<String, String>>(type).fromJson(json) ?: emptyMap()
            map.mapKeys { it.key.toIntOrNull() ?: 0 }.filterKeys { it != 0 }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun toJsonStringList(list: List<String>): String {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(list) ?: "[]"
    }

    fun fromJsonStringList(json: String): List<String> {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return try {
            moshi.adapter<List<String>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toJsonMap(map: Map<String, Int>): String {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Int::class.javaObjectType)
        return moshi.adapter<Map<String, Int>>(type).toJson(map) ?: "{}"
    }

    fun fromJsonMap(json: String): Map<String, Int> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Int::class.javaObjectType)
        return try {
            moshi.adapter<Map<String, Int>>(type).fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
