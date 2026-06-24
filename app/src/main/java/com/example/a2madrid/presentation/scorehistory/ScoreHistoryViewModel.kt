/*
 * ══ CAPA DE PRESENTACIÓN · ViewModel ══
 * Expone el historial ordenado (ObserveScoreHistoryUseCase) como StateFlow y delega el
 * borrado en DeleteScoreUseCase / ClearScoresUseCase. La View solo llama a deleteScore() /
 * clearScores(); el ViewModel lanza la corrutina y el repositorio persiste el cambio.
 */
package com.example.a2madrid.presentation.scorehistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a2madrid.domain.model.ScoreEntry
import com.example.a2madrid.domain.usecase.ClearScoresUseCase
import com.example.a2madrid.domain.usecase.DeleteScoreUseCase
import com.example.a2madrid.domain.usecase.ObserveScoreHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Exposes the score history, most recent first, for the score-list screen. */
@HiltViewModel
class ScoreHistoryViewModel @Inject constructor(
    observeScoreHistory: ObserveScoreHistoryUseCase,
    private val deleteScoreUseCase: DeleteScoreUseCase,
    private val clearScoresUseCase: ClearScoresUseCase,
) : ViewModel() {

    val history: StateFlow<List<ScoreEntry>> = observeScoreHistory()
        .map { entries -> entries.sortedByDescending(ScoreEntry::timestampMillis) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun deleteScore(entry: ScoreEntry) {
        viewModelScope.launch {
            deleteScoreUseCase(entry)
        }
    }

    fun clearScores() {
        viewModelScope.launch {
            clearScoresUseCase()
        }
    }
}
