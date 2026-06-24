/*
 * ══ CAPA DE DATOS · Mapper ══
 * Traduce en AMBOS sentidos: ScoreEntryDto → ScoreEntry (al leer) y
 * ScoreEntry → ScoreEntryDto (al guardar en DataStore). Lo usa QuizRepositoryImpl.
 */
package com.example.a2madrid.data.mapper

import com.example.a2madrid.data.dto.ScoreEntryDto
import com.example.a2madrid.domain.model.ScoreEntry

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