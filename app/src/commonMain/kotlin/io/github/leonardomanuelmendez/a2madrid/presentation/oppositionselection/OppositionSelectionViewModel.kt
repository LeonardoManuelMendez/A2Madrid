/*
 * ══ CAPA DE PRESENTACIÓN · ViewModel (MVVM) ══
 * Pide las oposiciones (GetOppositionsUseCase) y las expone como StateFlow<OppositionSelectionUiState>.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.oppositionselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetOppositionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads the available opposition categories for the start screen. */
class OppositionSelectionViewModel constructor(
    private val getOppositions: GetOppositionsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OppositionSelectionUiState())
    val uiState: StateFlow<OppositionSelectionUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { getOppositions() }
                .onSuccess { oppositions ->
                    _uiState.value = OppositionSelectionUiState(isLoading = false, oppositions = oppositions)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message
                                ?: "No se pudieron cargar las oposiciones",
                        )
                    }
                }
        }
    }
}
