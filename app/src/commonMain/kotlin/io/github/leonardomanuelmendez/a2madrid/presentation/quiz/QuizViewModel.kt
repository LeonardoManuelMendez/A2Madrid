/*
 * ══ CAPA DE PRESENTACIÓN · ViewModel (corazón del patrón MVVM) ══
 * Contiene la lógica de PRESENTACIÓN del test (no la de negocio, que vive en los UseCase):
 *   1) Recibe eventos de la View: loadExam, loadReview, loadExamSimulation, selectOption,
 *      confirmAnswer, nextQuestion, skipQuestion, restart.
 *   2) Llama a casos de uso: GetExam(s), GetAttemptReview, ShuffleQuestions, EvaluateAnswer,
 *      SaveScore.
 *   3) Publica un StateFlow<QuizUiState> que la View observa y repinta automáticamente.
 * No importa nada de Compose ni de Android UI → se puede testear con un repositorio fake.
 *
 * Tiene TRES modos (ver QuizMode). Práctica y repaso solo se diferencian en de qué preguntas se
 * compone la sesión. El SIMULACRO cambia además las reglas: cronómetro, sin corrección hasta el
 * final, y se puede dejar en blanco.
 *
 * El barajado es solo de PRESENTACIÓN: `selectOption` recibe la posición que el usuario ve y
 * guarda en el estado el índice canónico, que es el que acaba en el historial.
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
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.ShuffleQuestionsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Drives the quiz flow: loading a session's questions, tracking the selected option, confirming
 * answers, advancing, timing a mock exam, and producing [QuizResult].
 */
@OptIn(ExperimentalTime::class)
class QuizViewModel constructor(
    private val getExam: GetExamUseCase,
    private val evaluateAnswer: EvaluateAnswerUseCase,
    private val saveScore: SaveScoreUseCase,
    private val getAttemptReview: GetAttemptReviewUseCase,
    private val shuffleQuestions: ShuffleQuestionsUseCase,
    /** Reloj inyectable: los tests del cronómetro corren con tiempo virtual, sin esperas reales. */
    private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    /** Qué sesión está cargada, para poder reiniciarla conservando su modo. */
    private var currentSession: SessionRequest? = null
    private var timerJob: Job? = null
    private var deadlineMillis: Long = 0

    /** Loads the given exam once; ignores repeat calls for the same exam (e.g. config changes). */
    fun loadExam(examId: String) = load(SessionRequest.Practice(examId))

    /** Loads a session made only of the questions failed in the attempt at [attemptMillis]. */
    fun loadReview(examId: String, attemptMillis: Long) =
        load(SessionRequest.Review(examId, attemptMillis))

    /** Loads [examId] as a timed mock exam, under the real marking rules. */
    fun loadExamSimulation(examId: String) = load(SessionRequest.Simulation(examId))

    private fun load(request: SessionRequest) {
        if (request == currentSession) return
        currentSession = request
        reload(request)
    }

    /** Restarts the current session from scratch, keeping its mode. */
    fun restart() {
        currentSession?.let(::reload)
    }

    private fun reload(request: SessionRequest) {
        timerJob?.cancel()
        val mode = request.mode
        _uiState.value = QuizUiState(isLoading = true, mode = mode)
        viewModelScope.launch {
            runCatching { loadSession(request) }
                .onSuccess { session ->
                    _uiState.value = if (session == null) {
                        QuizUiState(isLoading = false, errorMessage = "Examen no encontrado", mode = mode)
                    } else {
                        QuizUiState(
                            isLoading = false,
                            examId = session.examId,
                            examTitle = session.examTitle,
                            questions = session.questions,
                            optionOrder = session.optionOrder,
                            mode = mode,
                        )
                    }
                    if (mode == QuizMode.EXAM) startCountdown(_uiState.value.totalQuestions)
                }
                .onFailure { throwable ->
                    _uiState.value = QuizUiState(
                        isLoading = false,
                        errorMessage = throwable.message ?: "No se pudo cargar el examen",
                        mode = mode,
                    )
                }
        }
    }

    private suspend fun loadSession(request: SessionRequest): Session? = when (request) {
        is SessionRequest.Practice -> {
            val exam = getExam(request.examId)
            exam?.let { session(it.id, it.title, shuffleQuestions(it.questions)) }
        }

        is SessionRequest.Review -> {
            val exam = getExam(request.examId)
            exam?.let {
                val failed = getAttemptReview(request.examId, request.attemptMillis)
                    .filterNot { answer -> answer.isCorrect }
                    .map { answer -> answer.question }
                session(it.id, it.title, shuffleQuestions(failed))
            }
        }

        // El simulacro es de UN modelo. Encadenar los tres en una sola sentada serían más de dos
        // horas, que no se parece a ningún examen: el primer ejercicio real son 90 minutos.
        is SessionRequest.Simulation -> {
            val exam = getExam(request.examId)
            exam?.let { session(it.id, it.title, shuffleQuestions(it.questions)) }
        }
    }

    private fun session(
        examId: String,
        examTitle: String,
        shuffled: io.github.leonardomanuelmendez.a2madrid.domain.model.ShuffledQuestions,
    ) = Session(examId, examTitle, shuffled.questions, shuffled.optionOrder)

    /**
     * Un minuto por pregunta, la proporción del examen real (90 preguntas en 90 minutos).
     *
     * El restante se calcula contra un INSTANTE DE FIN, no acumulando ticks: si la app pasa a
     * segundo plano y pierde latidos, al volver el tiempo restante es el correcto y no se han
     * regalado minutos.
     */
    private fun startCountdown(totalQuestions: Int) {
        deadlineMillis = nowMillis() + totalQuestions * MILLIS_PER_QUESTION
        timerJob = viewModelScope.launch {
            while (true) {
                val remaining = ((deadlineMillis - nowMillis()) / 1000L).coerceAtLeast(0L).toInt()
                _uiState.update { it.copy(remainingSeconds = remaining) }
                if (remaining <= 0) {
                    finishOnTimeout()
                    return@launch
                }
                delay(1000)
            }
        }
    }

    /** Se acabó el tiempo: cuenta lo que estuviera marcado y cierra el ejercicio. */
    private fun finishOnTimeout() {
        val state = _uiState.value
        if (state.result != null) return
        val question = state.currentQuestion
        val selected = state.selectedOptionIndex
        val withPending = if (question != null && selected != null) {
            state.copy(answers = state.answers + evaluateAnswer(question, selected))
        } else {
            state
        }
        _uiState.value = withPending
        finishQuiz(withPending)
    }

    /** @param displayedOptionIndex position the user tapped on screen, not the canonical index. */
    fun selectOption(displayedOptionIndex: Int) {
        val state = _uiState.value
        if (state.isAnswerConfirmed) return
        val question = state.currentQuestion ?: return
        val canonical = state.canonicalOptionIndex(question, displayedOptionIndex)
        _uiState.update { it.copy(selectedOptionIndex = canonical) }
    }

    fun confirmAnswer() {
        val state = _uiState.value
        if (!state.showsFeedback) return
        val question = state.currentQuestion ?: return
        val selected = state.selectedOptionIndex ?: return
        if (state.isAnswerConfirmed) return

        val answer = evaluateAnswer(question, selected)
        _uiState.update { it.copy(isAnswerConfirmed = true, answers = it.answers + answer) }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.isExam) {
            advanceExam(state, record = true)
            return
        }
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

    /** Deja la pregunta sin contestar y avanza. Solo en simulacro: el blanco no puntúa ni penaliza. */
    fun skipQuestion() {
        val state = _uiState.value
        if (!state.isExam) return
        advanceExam(state, record = false)
    }

    private fun advanceExam(state: QuizUiState, record: Boolean) {
        val question = state.currentQuestion ?: return
        val selected = state.selectedOptionIndex
        val answers = if (record && selected != null) {
            state.answers + evaluateAnswer(question, selected)
        } else {
            state.answers
        }
        val advanced = state.copy(answers = answers, selectedOptionIndex = null)
        if (state.isLastQuestion) {
            _uiState.value = advanced
            finishQuiz(advanced)
        } else {
            _uiState.value = advanced.copy(currentIndex = state.currentIndex + 1)
        }
    }

    private fun finishQuiz(state: QuizUiState) {
        val examId = state.examId ?: return
        timerJob?.cancel()
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
                isExam = state.isExam,
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

    /** The questions this run is made of, in session order, plus the exam they belong to. */
    private data class Session(
        val examId: String,
        val examTitle: String,
        val questions: List<Question>,
        val optionOrder: Map<Int, List<Int>>,
    )

    /** What to load. Se guarda para poder reiniciar la sesión conservando su modo. */
    private sealed interface SessionRequest {
        val mode: QuizMode

        data class Practice(val examId: String) : SessionRequest {
            override val mode get() = QuizMode.PRACTICE
        }

        data class Review(val examId: String, val attemptMillis: Long) : SessionRequest {
            override val mode get() = QuizMode.REVIEW
        }

        data class Simulation(val examId: String) : SessionRequest {
            override val mode get() = QuizMode.EXAM
        }
    }

    companion object {
        /** Un minuto por pregunta: la proporción del examen real, 90 preguntas en 90 minutos. */
        private const val MILLIS_PER_QUESTION = 60_000L
    }
}
