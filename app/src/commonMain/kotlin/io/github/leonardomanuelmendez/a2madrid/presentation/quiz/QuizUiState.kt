/*
 * ══ CAPA DE PRESENTACIÓN · UiState ══
 * Foto inmutable de la pantalla de test (examen, pregunta actual, opción elegida,
 * confirmada, respuestas dadas, resultado...). Incluye propiedades derivadas (progress,
 * canConfirm, hasProgress) para que la View sea "tonta" y no calcule nada.
 * Lo emite QuizViewModel.
 *
 * `answers` es la memoria del intento: cada respuesta confirmada se acumula aquí en vez de
 * descartarse. De ella se derivan los aciertos (una sola fuente de verdad, sin contador
 * paralelo) y la lista de fallos que alimenta el repaso.
 *
 * `optionOrder` es el barajado de opciones de ESTA sesión. Vive aquí, en presentación, y no en
 * el dominio: `selectedOptionIndex` se persiste en el historial y el desglose de un intento se
 * reconstruye contra exams.json, así que el índice guardado tiene que seguir siendo el canónico.
 * La pantalla pinta `displayedOptions(...)` y el ViewModel traduce con `canonicalOptionIndex(...)`.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.quiz

import io.github.leonardomanuelmendez.a2madrid.domain.model.AnswerResult
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
    /** True when this session only covers the questions failed in an earlier attempt. */
    val isReview: Boolean = false,
    /** Every answer confirmed so far, in the order they were given. */
    val answers: List<AnswerResult> = emptyList(),
    /** questionId → displayed option position → canonical option index, for this session. */
    val optionOrder: Map<Int, List<Int>> = emptyMap(),
    val result: QuizResult? = null,
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val questionNumber: Int get() = currentIndex + 1
    val totalQuestions: Int get() = questions.size
    val isLastQuestion: Boolean get() = questions.isNotEmpty() && currentIndex == questions.lastIndex
    val canConfirm: Boolean get() = selectedOptionIndex != null && !isAnswerConfirmed

    /** A review that has nothing left to review: the previous attempt has no pending failures. */
    val isEmptyReview: Boolean
        get() = isReview && !isLoading && errorMessage == null && questions.isEmpty()

    val correctAnswers: Int get() = answers.count { it.isCorrect }

    /** [question]'s options in the order this session must show them. */
    fun displayedOptions(question: Question): List<String> =
        optionPositions(question).map { question.options[it] }

    /** Canonical index of the option shown at [displayedIndex]. */
    fun canonicalOptionIndex(question: Question, displayedIndex: Int): Int =
        optionPositions(question).getOrElse(displayedIndex) { displayedIndex }

    /** Displayed position of the option whose canonical index is [canonicalIndex]. */
    fun displayedOptionIndex(question: Question, canonicalIndex: Int): Int =
        optionPositions(question).indexOf(canonicalIndex).takeIf { it >= 0 } ?: canonicalIndex

    /** Falls back to the original order, so an unshuffled session behaves exactly as before. */
    private fun optionPositions(question: Question): List<Int> =
        optionOrder[question.id] ?: question.options.indices.toList()

    /** The questions answered wrongly so far — what a review session is built from. */
    val wrongAnswers: List<AnswerResult> get() = answers.filterNot { it.isCorrect }

    val progress: Float
        get() = if (questions.isEmpty()) 0f else questionNumber.toFloat() / totalQuestions

    /** True once the user has made any progress worth confirming before a restart. */
    val hasProgress: Boolean
        get() = !isLoading && questions.isNotEmpty() &&
            (currentIndex > 0 || selectedOptionIndex != null || isAnswerConfirmed)
}
