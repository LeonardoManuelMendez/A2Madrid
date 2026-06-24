/*
 * ══ CAPA DE PRESENTACIÓN · ViewModel ══
 * Observa el historial (ObserveScoreHistoryUseCase) y lo expone como StateFlow para que la
 * pantalla de resultado pueda mostrar la mejor marca DE ESE modelo. El resultado puntual del
 * test viaja por la ruta; este ViewModel solo aporta el contexto del historial.
 */
package com.example.a2madrid.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a2madrid.domain.model.ScoreEntry
import com.example.a2madrid.domain.usecase.ObserveScoreHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Exposes the score history so the result screen can show the best mark per exam model. */
class ResultViewModel constructor(
    observeScoreHistory: ObserveScoreHistoryUseCase,
) : ViewModel() {

    val scoreHistory: StateFlow<List<ScoreEntry>> = observeScoreHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}