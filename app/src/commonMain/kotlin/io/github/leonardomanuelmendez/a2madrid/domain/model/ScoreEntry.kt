/*
 * ══ CAPA DE DOMINIO · Modelo ══
 * Una puntuación del historial, ligada a un modelo de examen (examId/examTitle).
 * La capa de DATOS la persiste (DataStore) vía su DTO; el dominio solo conoce esta
 * versión limpia. La consumen SaveScore/ObserveScoreHistory y las pantallas de
 * resultado e historial.
 *
 * Además del marcador, guarda el DETALLE del intento (`answers`), que es lo que permite
 * repasar los fallos días después. Los intentos guardados por versiones anteriores de la
 * app no lo tienen: llegan con la lista vacía y simplemente no ofrecen desglose.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.model

/** A single completed-quiz score, kept in the player's history and tied to an exam model. */
data class ScoreEntry(
    val examId: String,
    val examTitle: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val timestampMillis: Long,
    /** What the user answered, question by question. Empty for attempts stored before this existed. */
    val answers: List<AnsweredQuestion> = emptyList(),
    /**
     * True when this attempt was a review of previously failed questions rather than a full run.
     * Reviews cover a subset, so they are excluded from the personal best of the exam model.
     */
    val isReview: Boolean = false,
) {
    val percentage: Int
        get() = if (totalQuestions == 0) 0 else (correctAnswers * 100) / totalQuestions

    /** True when this attempt kept enough detail to rebuild a per-question breakdown. */
    val hasBreakdown: Boolean get() = answers.isNotEmpty()

    val wrongAnswers: Int get() = totalQuestions - correctAnswers

    /** True when this attempt left failures behind and kept the detail needed to revisit them. */
    val canReview: Boolean get() = hasBreakdown && wrongAnswers > 0
}
