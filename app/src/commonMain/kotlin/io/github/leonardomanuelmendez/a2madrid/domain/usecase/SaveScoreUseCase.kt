/*
 * ══ CAPA DE DOMINIO · UseCase ══
 * Guarda la puntuación en el historial y devuelve la entrada persistida + si es récord DE ESE
 * modelo. Orquesta lógica (lee mejor previo + persiste) sobre el repositorio.
 * → lo usa QuizViewModel al terminar el test.
 *
 * Los REPASOS y los SIMULACROS se guardan igual que un intento normal, pero quedan fuera del
 * cálculo de la mejor marca: el repaso cubre un subconjunto (un 6/6 no es comparable con un
 * 30/45 del modelo completo) y el simulacro abarca todos los modelos a la vez.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.usecase

import io.github.leonardomanuelmendez.a2madrid.domain.model.AnsweredQuestion
import io.github.leonardomanuelmendez.a2madrid.domain.model.SaveScoreResult
import io.github.leonardomanuelmendez.a2madrid.domain.model.ScoreEntry
import io.github.leonardomanuelmendez.a2madrid.domain.repository.QuizRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Persists a finished quiz score to history, reporting the stored entry and whether it set a new
 * personal best **for that exam model**. Review attempts never set a personal best.
 */
class SaveScoreUseCase(
    private val repository: QuizRepository,
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(
        examId: String,
        examTitle: String,
        correctAnswers: Int,
        totalQuestions: Int,
        answers: List<AnsweredQuestion> = emptyList(),
        isReview: Boolean = false,
        isExam: Boolean = false,
    ): SaveScoreResult {
        val previousBest = repository.scoreHistory.first()
            .filter { it.examId == examId && !it.isReview && !it.isExam }
            .maxOfOrNull { it.correctAnswers } ?: 0

        val entry = ScoreEntry(
            examId = examId,
            examTitle = examTitle,
            correctAnswers = correctAnswers,
            totalQuestions = totalQuestions,
            timestampMillis = Clock.System.now().toEpochMilliseconds(),
            answers = answers,
            isReview = isReview,
            isExam = isExam,
        )
        repository.addScore(entry)

        return SaveScoreResult(
            entry = entry,
            isNewBestScore = !isReview && !isExam && correctAnswers > previousBest,
        )
    }
}
