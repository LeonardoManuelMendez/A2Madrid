/*
 * ══ CAPA DE DATOS · Mapper ══
 * Traduce ExamDto → Exam (dominio) y, al mapear cada pregunta, le inyecta el mapa de
 * contextos para resolver los contextId. Lo usa QuizRepositoryImpl.
 */
package com.example.a2madrid.data.mapper

import com.example.a2madrid.data.dto.ExamDto
import com.example.a2madrid.domain.model.Exam

/** Maps the serialized [ExamDto] (and its questions) into the domain [Exam]. */
fun ExamDto.toDomain(): Exam = Exam(
    id = id,
    title = title,
    questions = questions.map { it.toDomain(contexts) },
)