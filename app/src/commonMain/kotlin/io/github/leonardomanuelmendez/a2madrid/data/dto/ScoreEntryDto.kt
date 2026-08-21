/*
 * ══ CAPA DE DATOS · DTO ══
 * Forma serializada de una puntuación. ScorePreferencesDataSource guarda una lista
 * de estos como JSON dentro de DataStore. ScoreMapper lo traduce a/desde ScoreEntry.
 *
 * `answers` e `isReview` llevan valor por defecto A PROPÓSITO: es lo que hace que un
 * historial escrito por una versión anterior de la app (sin esos campos) se siga
 * deserializando sin migración. Ver ScoreMapperTest.
 */
package io.github.leonardomanuelmendez.a2madrid.data.dto

import kotlinx.serialization.Serializable

/** Serialized shape of a stored score entry. */
@Serializable
data class ScoreEntryDto(
    val examId: String,
    val examTitle: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val timestampMillis: Long,
    val answers: List<AnsweredQuestionDto> = emptyList(),
    val isReview: Boolean = false,
)
