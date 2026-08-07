/*
 * ╔══ CAPA DE DOMINIO · Modelo (Entidad de negocio) ══╗
 * El "porqué" de una pregunta: resumen corto + desarrollo desplegable (puntos y/o norma
 * citada). Kotlin puro, sin Compose ni JSON. Lo CREAN los mappers de la capa de DATOS a
 * partir de exams.json y lo CONSUME la pantalla de test tras confirmar la respuesta.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.model

/**
 * Why the correct option is the correct one.
 *
 * Split in two layers so the UI can show the gist immediately and unfold the rest on demand:
 * [summary] is always visible, while [points] and [source] live behind a disclosure.
 */
data class Explanation(
    /** One or two lines stating the reason. Always visible under the verdict. */
    val summary: String,
    /** Breakdown of the answer: misspelled words, steps of a calculation, discarded options… */
    val points: List<ExplanationPoint> = emptyList(),
    /** The rule the answer rests on (article of a law, spelling rule…). */
    val source: ExplanationSource? = null,
) {
    init {
        require(summary.isNotBlank()) { "An explanation needs a summary" }
    }

    /** True when there is more than the summary, i.e. the disclosure is worth rendering. */
    val hasDetail: Boolean get() = points.isNotEmpty() || source != null
}

/** One item of the breakdown: an optional highlighted [term] plus its [text]. */
data class ExplanationPoint(
    val text: String,
    /** Word, figure or article the item is about; rendered in bold ahead of [text]. */
    val term: String? = null,
)

/** The rule backing the answer: [label] cites it, [quote] reproduces or paraphrases it. */
data class ExplanationSource(
    val label: String,
    val quote: String? = null,
)
