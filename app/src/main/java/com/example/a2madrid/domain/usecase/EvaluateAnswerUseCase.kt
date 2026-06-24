/*
 * ══ CAPA DE DOMINIO · UseCase ══
 * Regla de negocio PURA: decide si la opción elegida es correcta (no usa repositorio).
 * → lo usa QuizViewModel al confirmar una respuesta.
 */
package com.example.a2madrid.domain.usecase

import com.example.a2madrid.domain.model.AnswerResult
import com.example.a2madrid.domain.model.Question
import javax.inject.Inject

/** Encapsulates the rule that decides whether a selected option is correct. */
class EvaluateAnswerUseCase @Inject constructor() {

    operator fun invoke(question: Question, selectedOptionIndex: Int): AnswerResult =
        AnswerResult(
            question = question,
            selectedOptionIndex = selectedOptionIndex,
            isCorrect = question.isCorrect(selectedOptionIndex),
        )
}