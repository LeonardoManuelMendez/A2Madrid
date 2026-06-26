/*
 * ══ CAPA DE PRESENTACIÓN · ViewModel (MVVM) ══
 * Pide los exámenes (GetExamsUseCase) y los expone como StateFlow<ExamSelectionUiState>.
 * No conoce Compose: solo produce estado. Koin lo provee y la View lo obtiene
 * con koinViewModel(); sobrevive a rotaciones.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.examselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetExamsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads the available exam models for the start screen. */
class ExamSelectionViewModel constructor(
    private val getExams: GetExamsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamSelectionUiState())
    val uiState: StateFlow<ExamSelectionUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { getExams() }
                .onSuccess { exams ->
                    _uiState.value = ExamSelectionUiState(isLoading = false, exams = exams)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message
                                ?: "No se pudieron cargar los exámenes",
                        )
                    }
                }
        }
    }
}