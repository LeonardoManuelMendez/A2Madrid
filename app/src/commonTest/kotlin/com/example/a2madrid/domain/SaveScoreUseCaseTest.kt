package com.example.a2madrid.domain

import com.example.a2madrid.domain.model.ScoreEntry
import com.example.a2madrid.domain.usecase.SaveScoreUseCase
import com.example.a2madrid.fake.FakeQuizRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveScoreUseCaseTest {

    private val examId = "modelo_a"
    private val examTitle = "Modelo A"

    @Test
    fun `returns true and stores score when it beats previous best`() = runTest {
        val repository = FakeQuizRepository(
            initialHistory = listOf(ScoreEntry(examId, examTitle, 2, 10, 0L)),
        )
        val useCase = SaveScoreUseCase(repository)

        val isNewBest = useCase(
            examId = examId,
            examTitle = examTitle,
            correctAnswers = 5,
            totalQuestions = 10,
        )

        assertTrue(isNewBest)
        assertEquals(5, repository.scoreHistory.first().bestFor(examId))
    }

    @Test
    fun `returns false and keeps best when score does not improve`() = runTest {
        val repository = FakeQuizRepository(
            initialHistory = listOf(ScoreEntry(examId, examTitle, 7, 10, 0L)),
        )
        val useCase = SaveScoreUseCase(repository)

        val isNewBest = useCase(
            examId = examId,
            examTitle = examTitle,
            correctAnswers = 4,
            totalQuestions = 10,
        )

        assertFalse(isNewBest)
        assertEquals(7, repository.scoreHistory.first().bestFor(examId))
    }

    @Test
    fun `appends every score to the history`() = runTest {
        val repository = FakeQuizRepository()
        val useCase = SaveScoreUseCase(repository)

        useCase(examId = examId, examTitle = examTitle, correctAnswers = 5, totalQuestions = 10)
        useCase(examId = examId, examTitle = examTitle, correctAnswers = 8, totalQuestions = 10)

        val history = repository.scoreHistory.first()
        assertEquals(2, history.size)
        assertEquals(8, history.bestFor(examId))
    }

    @Test
    fun `compares scores only within the same exam model`() = runTest {
        val repository = FakeQuizRepository(
            initialHistory = listOf(ScoreEntry("modelo_b", "Modelo B", 10, 10, 0L)),
        )
        val useCase = SaveScoreUseCase(repository)

        val isNewBest = useCase(
            examId = examId,
            examTitle = examTitle,
            correctAnswers = 4,
            totalQuestions = 10,
        )

        assertTrue(isNewBest)
        assertEquals(4, repository.scoreHistory.first().bestFor(examId))
    }

    private fun List<ScoreEntry>.bestFor(examId: String): Int =
        filter { it.examId == examId }.maxOfOrNull { it.correctAnswers } ?: 0
}
