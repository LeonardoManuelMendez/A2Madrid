/*
 * ══ CAPA DE DOMINIO · Modelo ══
 * Qué contestó el usuario en UNA pregunta de un intento ya terminado. Se guarda el id de la
 * pregunta y el índice elegido, nunca el enunciado: el texto se recupera recargando el examen
 * y cruzando por id, de modo que el contenido tiene una sola fuente de verdad (exams.json).
 * Lo persiste ScoreEntry y lo consume GetAttemptReviewUseCase para reconstruir el desglose.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.model

/** One answer given during a finished attempt, stored by question id. */
data class AnsweredQuestion(
    val questionId: Int,
    val selectedOptionIndex: Int,
)
