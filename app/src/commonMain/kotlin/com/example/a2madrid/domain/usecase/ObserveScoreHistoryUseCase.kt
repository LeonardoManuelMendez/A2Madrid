/*
 * ══ CAPA DE DOMINIO · UseCase ══
 * Expone el historial como Flow observable (datos reactivos: la UI se actualiza sola).
 * → lo usan ResultViewModel (mejor marca) y ScoreHistoryViewModel (lista).
 */
package com.example.a2madrid.domain.usecase

import com.example.a2madrid.domain.model.ScoreEntry
import com.example.a2madrid.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow

/** Exposes the player's score history as an observable stream. */
class ObserveScoreHistoryUseCase constructor(
    private val repository: QuizRepository,
) {
    operator fun invoke(): Flow<List<ScoreEntry>> = repository.scoreHistory
}