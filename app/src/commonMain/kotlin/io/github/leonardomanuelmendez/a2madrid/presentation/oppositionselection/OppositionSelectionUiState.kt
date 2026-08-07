/*
 * ══ CAPA DE PRESENTACIÓN · UiState ══
 * Estado inmutable que describe todo lo que la pantalla de selección de oposición debe pintar.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.oppositionselection

import io.github.leonardomanuelmendez.a2madrid.domain.model.Opposition

/** State for the opposition-selection screen. */
data class OppositionSelectionUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val oppositions: List<Opposition> = emptyList(),
)
