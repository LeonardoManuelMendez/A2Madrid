/*
 * ══ CAPA DE PRESENTACIÓN · ViewModel (corazón del patrón MVVM) ══
 * Contiene la lógica de PRESENTACIÓN del test (no la de negocio, que vive en los UseCase):
 *   1) Recibe eventos de la View: loadExam, loadReview, selectOption, confirmAnswer,
 *      nextQuestion, restart.
 *   2) Llama a casos de uso: GetExamUseCase, GetAttemptReviewUseCase, EvaluateAnswerUseCase,
 *      SaveScoreUseCase.
 *   3) Publica un StateFlow<QuizUiState> que la View observa y repinta automáticamente.
 * No importa nada de Compose ni de Android UI → se puede testear con un repositorio fake
 * (ver QuizViewModelTest). Koin lo provee (viewModelOf).
 *
 * Tiene dos modos, y la única diferencia entre ellos es de QUÉ preguntas se compone la sesión:
 * el test completo carga el examen entero; el repaso carga solo las falladas en un intento
 * anterior. Todo lo demás (confirmar, avanzar, guardar) es idéntico.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.leonardomanuelmendez.a2madrid.domain.model.AnsweredQuestion
import io.github.leonardomanuelmendez.a2madrid.domain.model.Question
import io.github.leonardomanuelmendez.a2madrid.domain.model.QuizResult
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.EvaluateAnswerUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetAttemptReviewUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetExamUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.SaveScoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the quiz flow for a chosen exam model: loading its questions, tracking the selected
 * option, confirming answers with immediate feedback, advancing, and producing [QuizResult].
 */
class QuizViewModel constructor(
    private val getExam: GetExamUseCase,
    private val evaluateAnswer: EvaluateAnswerUseCase,
    private val saveScore: SaveScoreUseCase,
    private val getAttemptReview: GetAttemptReviewUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var loadedExamId: String? = null

    /** Attempt being reviewed, or `null` when this is a full run of the exam. */
    private var reviewAttemptMillis: Long? = null

    /** Loads the given exam once; ignores repeat calls for the same exam (e.g. config changes). */
    fun loadExam(examId: String) {
        if (examId == loadedExamId && reviewAttemptMillis == null) return
        reviewAttemptMillis = null
        reload(examId)
    }

    /** Loads a session made only of the questions failed in the attempt at [attemptMillis]. */
    fun loadReview(examId: String, attemptMillis: Long) {
        if (examId == loadedExamId && attemptMillis == reviewAttemptMillis) return
        reviewAttemptMillis = attemptMillis
        reload(examId)
    }

    /** Restarts the current session from scratch, keeping its mode. */
    fun restart() {
        loadedExamId?.let { reload(it) }
    }

    private fun reload(examId: String) {
        loadedExamId = examId
        val attemptMillis = reviewAttemptMillis
        val isReview = attemptMillis != null
        _uiState.value = QuizUiState(isLoading = true, isReview = isReview)
        viewModelScope.launch {
            runCatching { loadSession(examId, attemptMillis) }
                .onSuccess { session ->
                    _uiState.value = if (session == null) {
                        QuizUiState(
                            isLoading = false,
                            errorMessage = "Examen no encontrado",
                            isReview = isReview,
                        )
                    } else {
                        QuizUiState(
                            isLoading = false,
                            examId = session.examId,
                            examTitle = session.examTitle,
                            questions = session.questions,
                            isReview = isReview,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = QuizUiState(
                        isLoading = false,
                        errorMessage = throwable.message ?: "No se pudo cargar el examen",
                        isReview = isReview,
                    )
                }
        }
    }

    private suspend fun loadSession(examId: String, attemptMillis: Long?): Session? {
        val exam = getExam(examId) ?: return null
        val questions = if (attemptMillis == null) {
            exam.questions
        } else {
            getAttemptReview(examId, attemptMillis)
                .filterNot { it.isCorrect }
                .map { it.question }
        }
        return Session(exam.id, exam.title, questions)
    }

    /** The questions this run is made of, plus the exam they belong to. */
    private data class Session(
        val examId: String,
        val examTitle: String,
        val questions: List<Question>,
    )

    fun selectOption(optionIndex: Int) {
        if (_uiState.value.isAnswerConfirmed) return
        _uiState.update { it.copy(selectedOptionIndex = optionIndex) }
    }

    fun confirmAnswer() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        val selected = state.selectedOptionIndex ?: return
        if (state.isAnswerConfirmed) return

        val answer = evaluateAnswer(question, selected)
        _uiState.update {
            it.copy(
                isAnswerConfirmed = true,
                answers = it.answers + answer,
            )
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (!state.isAnswerConfirmed) return
        if (state.isLastQuestion) {
            finishQuiz(state)
        } else {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedOptionIndex = null,
                    isAnswerConfirmed = false,
                )
            }
        }
    }

    private fun finishQuiz(state: QuizUiState) {
        val examId = state.examId ?: return
        viewModelScope.launch {
            val saved = saveScore(
                examId = examId,
                examTitle = state.examTitle,
                correctAnswers = state.correctAnswers,
                totalQuestions = state.totalQuestions,
                answers = state.answers.map {
                    AnsweredQuestion(it.question.id, it.selectedOptionIndex)
                },
                isReview = state.isReview,
            )
            _uiState.update {
                it.copy(
                    result = QuizResult(
                        correctAnswers = state.correctAnswers,
                        totalQuestions = state.totalQuestions,
                        isNewBestScore = saved.isNewBestScore,
                        attemptMillis = saved.entry.timestampMillis,
                    ),
                )
            }
        }
    }
}
