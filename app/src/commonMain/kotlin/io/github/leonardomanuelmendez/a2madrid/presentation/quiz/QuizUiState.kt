/*
 * ══ CAPA DE PRESENTACIÓN · UiState ══
 * Foto inmutable de la pantalla de test (examen, pregunta actual, opción elegida,
 * confirmada, aciertos, resultado...). Incluye propiedades derivadas (progress, canConfirm,
 * hasProgress) para que la View sea "tonta" y no calcule nada. Lo emite QuizViewModel.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.quiz

import io.github.leonardomanuelmendez.a2madrid.domain.model.Question
import io.github.leonardomanuelmendez.a2madrid.domain.model.QuizResult

/** Immutable snapshot of everything the Quiz screen needs to render. */
data class QuizUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val examId: String? = null,
    val examTitle: String = "",
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerConfirmed: Boolean = false,
    val correctAnswers: Int = 0,
    val result: QuizResult? = null,
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val questionNumber: Int get() = currentIndex + 1
    val totalQuestions: Int get() = questions.size
    val isLastQuestion: Boolean get() = questions.isNotEmpty() && currentIndex == questions.lastIndex
    val canConfirm: Boolean get() = selectedOptionIndex != null && !isAnswerConfirmed

    val progress: Float
        get() = if (questions.isEmpty()) 0f else questionNumber.toFloat() / totalQuestions

    /** True once the user has made any progress worth confirming before a restart. */
    val hasProgress: Boolean
        get() = !isLoading && questions.isNotEmpty() &&
            (currentIndex > 0 || selectedOptionIndex != null || isAnswerConfirmed)
}