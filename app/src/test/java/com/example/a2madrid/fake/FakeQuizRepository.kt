package com.example.a2madrid.fake

import com.example.a2madrid.domain.model.Exam
import com.example.a2madrid.domain.model.ScoreEntry
import com.example.a2madrid.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [QuizRepository] for unit tests. */
class FakeQuizRepository(
    private val exams: List<Exam> = emptyList(),
    private val failOnLoad: Boolean = false,
    initialHistory: List<ScoreEntry> = emptyList(),
) : QuizRepository {

    private val _history = MutableStateFlow(initialHistory)

    override val scoreHistory: Flow<List<ScoreEntry>> = _history

    override suspend fun getExams(): List<Exam> {
        if (failOnLoad) error("Failed to load exams")
        return exams
    }

    override suspend fun addScore(entry: ScoreEntry) {
        _history.value = _history.value + entry
    }

    override suspend fun deleteScore(entry: ScoreEntry) {
        _history.value = _history.value.toMutableList().apply { remove(entry) }
    }

    override suspend fun clearScores() {
        _history.value = emptyList()
    }
}
