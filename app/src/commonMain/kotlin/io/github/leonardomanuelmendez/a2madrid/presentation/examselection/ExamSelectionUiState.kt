/*
 * ══ CAPA DE PRESENTACIÓN · UiState ══
 * Estado inmutable que describe TODO lo que la pantalla de selección debe pintar
 * (cargando / error / lista de exámenes). Lo emite el ViewModel; la View solo lo observa.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.examselection

import io.github.leonardomanuelmendez.a2madrid.domain.model.Exam

/** State for the exam-selection (start) screen. */
data class ExamSelectionUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val exams: List<Exam> = emptyList(),
)