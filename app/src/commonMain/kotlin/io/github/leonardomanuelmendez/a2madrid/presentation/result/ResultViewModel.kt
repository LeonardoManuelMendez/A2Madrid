/*
 * ══ CAPA DE PRESENTACIÓN · ViewModel ══
 * Aporta a la pantalla de resultado el CONTEXTO que no cabe en la ruta:
 *   1) el historial (ObserveScoreHistoryUseCase), para la mejor marca de ese modelo;
 *   2) el desglose del intento (GetAttemptReviewUseCase), que se reconstruye a partir del
 *      identificador que sí viaja por la ruta (attemptMillis).
 * El marcador puntual del intento llega por la ruta; aquí no se recalcula.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.leonardomanuelmendez.a2madrid.domain.model.AnswerResult
import io.github.leonardomanuelmendez.a2madrid.domain.model.ScoreEntry
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetAttemptReviewUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.ObserveScoreHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Exposes the score history and the per-question breakdown of the attempt just finished. */
class ResultViewModel constructor(
    observeScoreHistory: ObserveScoreHistoryUseCase,
    private val getAttemptReview: GetAttemptReviewUseCase,
) : ViewModel() {

    val scoreHistory: StateFlow<List<ScoreEntry>> = observeScoreHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _breakdown = MutableStateFlow<List<AnswerResult>>(emptyList())

    /** Per-question detail of the attempt; empty while loading, or if it was not kept. */
    val breakdown: StateFlow<List<AnswerResult>> = _breakdown.asStateFlow()

    private var loadedAttempt: Pair<String, Long>? = null

    /** Loads the breakdown once per attempt; repeat calls (recomposition) are ignored. */
    fun loadBreakdown(examId: String, attemptMillis: Long) {
        val key = examId to attemptMillis
        if (key == loadedAttempt) return
        loadedAttempt = key
        viewModelScope.launch {
            _breakdown.value = runCatching { getAttemptReview(examId, attemptMillis) }
                .getOrDefault(emptyList())
        }
    }
}
