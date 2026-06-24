/*
 * ══ CAPA DE DOMINIO · UseCase ══
 * Guarda la puntuación en el historial y devuelve si es récord DE ESE modelo.
 * Orquesta lógica (lee mejor previo + persiste) sobre el repositorio.
 * → lo usa QuizViewModel al terminar el test.
 */
package com.example.a2madrid.domain.usecase

import com.example.a2madrid.domain.model.ScoreEntry
import com.example.a2madrid.domain.repository.QuizRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Persists a finished quiz score to history, reporting whether it set a new personal best
 * **for that exam model**.
 */
class SaveScoreUseCase @Inject constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(
        examId: String,
        examTitle: String,
        correctAnswers: Int,
        totalQuestions: Int,
    ): Boolean {
        val previousBest = repository.scoreHistory.first()
            .filter { it.examId == examId }
            .maxOfOrNull { it.correctAnswers } ?: 0

        repository.addScore(
            ScoreEntry(
                examId = examId,
                examTitle = examTitle,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                timestampMillis = System.currentTimeMillis(),
            ),
        )
        return correctAnswers > previousBest
    }
}