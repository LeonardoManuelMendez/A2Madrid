/*
 * ══ CAPA DE DATOS · Mapper ══
 * Traduce el DTO (forma del JSON) al modelo de dominio (Question), resolviendo el
 * contextId contra el mapa de contextos del examen. Mantener esta frontera evita que
 * los detalles de serialización se filtren al dominio. Lo usan ExamMapper / Repository.
 */
package io.github.leonardomanuelmendez.a2madrid.data.mapper

import io.github.leonardomanuelmendez.a2madrid.data.dto.QuestionDto
import io.github.leonardomanuelmendez.a2madrid.domain.model.Question

/** Maps the serialized [QuestionDto] into the domain [Question], resolving its shared context. */
fun QuestionDto.toDomain(contexts: Map<String, String> = emptyMap()): Question = Question(
    id = id,
    text = text,
    options = options,
    correctAnswerIndex = correctAnswerIndex,
    explanation = explanation,
    context = contextId?.let { contexts[it] },
)
