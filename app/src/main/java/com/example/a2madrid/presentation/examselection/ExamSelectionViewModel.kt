/*
 * ══ CAPA DE PRESENTACIÓN · ViewModel (MVVM) ══
 * Pide los exámenes (GetExamsUseCase) y los expone como StateFlow<ExamSelectionUiState>.
 * No conoce Compose: solo produce estado. Hilt lo crea (@HiltViewModel) y la View lo obtiene
 * con hiltViewModel(); sobrevive a rotaciones.
 */
package com.example.a2madrid.presentation.examselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a2madrid.domain.usecase.GetExamsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Loads the available exam models for the start screen. */
@HiltViewModel
class ExamSelectionViewModel @Inject constructor(
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