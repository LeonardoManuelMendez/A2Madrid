/*
 * ══ CAPA DE PRESENTACIÓN · ViewModel (corazón del patrón MVVM) ══
 * Contiene la lógica de PRESENTACIÓN del test (no la de negocio, que vive en los UseCase):
 *   1) Recibe eventos de la View: loadExam, selectOption, confirmAnswer, nextQuestion, restart.
 *   2) Llama a casos de uso: GetExamUseCase, EvaluateAnswerUseCase, SaveScoreUseCase.
 *   3) Publica un StateFlow<QuizUiState> que la View observa y repinta automáticamente.
 * No importa nada de Compose ni de Android UI → se puede testear con un repositorio fake
 * (ver QuizViewModelTest). Hilt lo provee con @HiltViewModel.
 */
package com.example.a2madrid.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a2madrid.domain.model.QuizResult
import com.example.a2madrid.domain.usecase.EvaluateAnswerUseCase
import com.example.a2madrid.domain.usecase.GetExamUseCase
import com.example.a2madrid.domain.usecase.SaveScoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the quiz flow for a chosen exam model: loading its questions, tracking the selected
 * option, confirming answers with immediate feedback, advancing, and producing [QuizResult].
 */
@HiltViewModel
class QuizViewModel @Inject constructor(
    private val getExam: GetExamUseCase,
    private val evaluateAnswer: EvaluateAnswerUseCase,
    private val saveScore: SaveScoreUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var loadedExamId: String? = null

    /** Loads the given exam once; ignores repeat calls for the same exam (e.g. config changes). */
    fun loadExam(examId: String) {
        if (examId == loadedExamId) return
        reload(examId)
    }

    /** Restarts the current exam from scratch. */
    fun restart() {
        loadedExamId?.let { reload(it) }
    }

    private fun reload(examId: String) {
        loadedExamId = examId
        _uiState.value = QuizUiState(isLoading = true)
        viewModelScope.launch {
            runCatching { getExam(examId) }
                .onSuccess { exam ->
                    _uiState.value = if (exam == null) {
                        QuizUiState(isLoading = false, errorMessage = "Examen no encontrado")
                    } else {
                        QuizUiState(
                            isLoading = false,
                            examId = exam.id,
                            examTitle = exam.title,
                            questions = exam.questions,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = QuizUiState(
                        isLoading = false,
                        errorMessage = throwable.message ?: "No se pudo cargar el examen",
                    )
                }
        }
    }

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
                correctAnswers = it.correctAnswers + if (answer.isCorrect) 1 else 0,
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
            val isNewBest = saveScore(
                examId = examId,
                examTitle = state.examTitle,
                correctAnswers = state.correctAnswers,
                totalQuestions = state.totalQuestions,
            )
            _uiState.update {
                it.copy(
                    result = QuizResult(
                        correctAnswers = state.correctAnswers,
                        totalQuestions = state.totalQuestions,
                        isNewBestScore = isNewBest,
                    ),
                )
            }
        }
    }
}