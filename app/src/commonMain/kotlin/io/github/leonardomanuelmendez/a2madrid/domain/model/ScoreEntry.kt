/*
 * ══ CAPA DE DOMINIO · Modelo ══
 * Una puntuación del historial, ligada a un modelo de examen (examId/examTitle).
 * La capa de DATOS la persiste (DataStore) vía su DTO; el dominio solo conoce esta
 * versión limpia. La consumen SaveScore/ObserveScoreHistory y las pantallas de
 * resultado e historial.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.model

/** A single completed-quiz score, kept in the player's history and tied to an exam model. */
data class ScoreEntry(
    val examId: String,
    val examTitle: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val timestampMillis: Long,
) {
    val percentage: Int
        get() = if (totalQuestions == 0) 0 else (correctAnswers * 100) / totalQuestions
}