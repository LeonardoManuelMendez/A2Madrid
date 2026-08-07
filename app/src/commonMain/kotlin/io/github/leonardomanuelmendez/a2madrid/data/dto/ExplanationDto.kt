/*
 * ══ CAPA DE DATOS · DTO (Data Transfer Object) ══
 * Forma serializada del "porqué" de una pregunta dentro de exams.json. Se mantiene separada
 * del modelo de dominio (Explanation) para que un cambio en el formato del fichero no
 * contamine el dominio. ExplanationMapper lo traduce; @Serializable = kotlinx.serialization.
 */
package io.github.leonardomanuelmendez.a2madrid.data.dto

import kotlinx.serialization.Serializable

/** Serialized shape of the reasoning behind a question's correct answer. */
@Serializable
data class ExplanationDto(
    val summary: String,
    val points: List<ExplanationPointDto> = emptyList(),
    val source: ExplanationSourceDto? = null,
)

/** One item of the breakdown; [term] is the word/figure/article the item highlights. */
@Serializable
data class ExplanationPointDto(
    val text: String,
    val term: String? = null,
)

/** Citation of the rule backing the answer, with its verbatim text when available. */
@Serializable
data class ExplanationSourceDto(
    val label: String,
    val quote: String? = null,
)
