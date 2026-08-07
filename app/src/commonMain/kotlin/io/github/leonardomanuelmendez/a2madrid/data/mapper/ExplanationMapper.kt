/*
 * ══ CAPA DE DATOS · Mapper ══
 * Traduce el DTO del "porqué" (forma del JSON) al modelo de dominio Explanation.
 * Mantener esta frontera evita que los detalles de serialización se filtren al dominio.
 * Lo usa QuestionMapper al construir cada Question.
 */
package io.github.leonardomanuelmendez.a2madrid.data.mapper

import io.github.leonardomanuelmendez.a2madrid.data.dto.ExplanationDto
import io.github.leonardomanuelmendez.a2madrid.domain.model.Explanation
import io.github.leonardomanuelmendez.a2madrid.domain.model.ExplanationPoint
import io.github.leonardomanuelmendez.a2madrid.domain.model.ExplanationSource

/** Maps the serialized [ExplanationDto] into the domain [Explanation]. */
fun ExplanationDto.toDomain(): Explanation = Explanation(
    summary = summary,
    points = points.map { ExplanationPoint(text = it.text, term = it.term) },
    source = source?.let { ExplanationSource(label = it.label, quote = it.quote) },
)
