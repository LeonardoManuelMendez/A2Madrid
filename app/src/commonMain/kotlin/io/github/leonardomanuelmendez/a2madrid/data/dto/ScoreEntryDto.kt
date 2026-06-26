/*
 * ══ CAPA DE DATOS · DTO ══
 * Forma serializada de una puntuación. ScorePreferencesDataSource guarda una lista
 * de estos como JSON dentro de DataStore. ScoreMapper lo traduce a/desde ScoreEntry.
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
)