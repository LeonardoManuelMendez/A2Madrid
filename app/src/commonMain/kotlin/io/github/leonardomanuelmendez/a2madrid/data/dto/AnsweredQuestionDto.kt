/*
 * ══ CAPA DE DATOS · DTO ══
 * Forma serializada de una respuesta dentro de ScoreEntryDto. Separada del modelo de dominio
 * para que un cambio de formato en disco no se filtre al dominio. La traduce ScoreMapper.
 */
package io.github.leonardomanuelmendez.a2madrid.data.dto

import kotlinx.serialization.Serializable

/** Serialized shape of a single stored answer. */
@Serializable
data class AnsweredQuestionDto(
    val questionId: Int,
    val selectedOptionIndex: Int,
)
