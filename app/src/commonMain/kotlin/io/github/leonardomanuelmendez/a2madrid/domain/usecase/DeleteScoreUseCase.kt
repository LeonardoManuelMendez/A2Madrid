/*
 * ══ CAPA DE DOMINIO · UseCase ══
 * Borra UNA puntuación del historial. → lo usa ScoreHistoryViewModel.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.usecase

import io.github.leonardomanuelmendez.a2madrid.domain.model.ScoreEntry
import io.github.leonardomanuelmendez.a2madrid.domain.repository.QuizRepository

/** Removes a single score entry from the history. */
class DeleteScoreUseCase constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(entry: ScoreEntry) = repository.deleteScore(entry)
}