/*
 * ══ CAPA DE DOMINIO · UseCase ══
 * Borra TODO el historial. → lo usa ScoreHistoryViewModel ("Borrar historial").
 */
package io.github.leonardomanuelmendez.a2madrid.domain.usecase

import io.github.leonardomanuelmendez.a2madrid.domain.repository.QuizRepository

/** Removes every stored score. */
class ClearScoresUseCase constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke() = repository.clearScores()
}