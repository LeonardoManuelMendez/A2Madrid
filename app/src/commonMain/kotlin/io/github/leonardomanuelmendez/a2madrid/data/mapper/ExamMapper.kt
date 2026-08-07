/*
 * ══ CAPA DE DATOS · Mapper ══
 * Traduce ExamDto → Exam (dominio) y, al mapear cada pregunta, le inyecta el mapa de
 * contextos para resolver los contextId. Lo usa QuizRepositoryImpl.
 */
package io.github.leonardomanuelmendez.a2madrid.data.mapper

import io.github.leonardomanuelmendez.a2madrid.data.dto.ExamDto
import io.github.leonardomanuelmendez.a2madrid.domain.model.Exam

/** Maps the serialized [ExamDto] (and its questions) into the domain [Exam]. */
fun ExamDto.toDomain(): Exam = Exam(
    id = id,
    title = title,
    questions = questions.map { it.toDomain(contexts) },
)