/*
 * ══ CAPA DE DOMINIO · UseCase ══
 * Borra TODO el historial. → lo usa ScoreHistoryViewModel ("Borrar historial").
 */
package com.example.a2madrid.domain.usecase

import com.example.a2madrid.domain.repository.QuizRepository
import javax.inject.Inject

/** Removes every stored score. */
class ClearScoresUseCase @Inject constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke() = repository.clearScores()
}