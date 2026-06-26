/*
 * ══ CAPA DE DATOS · Mapper ══
 * Traduce en AMBOS sentidos: ScoreEntryDto → ScoreEntry (al leer) y
 * ScoreEntry → ScoreEntryDto (al guardar en DataStore). Lo usa QuizRepositoryImpl.
 */
package io.github.leonardomanuelmendez.a2madrid.data.mapper

import io.github.leonardomanuelmendez.a2madrid.data.dto.ScoreEntryDto
import io.github.leonardomanuelmendez.a2madrid.domain.model.ScoreEntry

fun ScoreEntryDto.toDomain(): ScoreEntry = ScoreEntry(
    examId = examId,
    examTitle = examTitle,
    correctAnswers = correctAnswers,
    totalQuestions = totalQuestions,
    timestampMillis = timestampMillis,
)

fun ScoreEntry.toDto(): ScoreEntryDto = ScoreEntryDto(
    examId = examId,
    examTitle = examTitle,
    correctAnswers = correctAnswers,
    totalQuestions = totalQuestions,
    timestampMillis = timestampMillis,
)