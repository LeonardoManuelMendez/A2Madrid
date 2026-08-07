/*
 * ══ CAPA DE DOMINIO · Modelo ══
 * Resultado de evaluar UNA respuesta. Lo produce EvaluateAnswerUseCase y lo
 * consume el QuizViewModel para actualizar el estado (acierto/fallo).
 */
package io.github.leonardomanuelmendez.a2madrid.domain.model

/** Outcome of answering a single [Question]. */
data class AnswerResult(
    val question: Question,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
)