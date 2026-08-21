package io.github.leonardomanuelmendez.a2madrid.presentation

import io.github.leonardomanuelmendez.a2madrid.domain.model.Exam
import io.github.leonardomanuelmendez.a2madrid.domain.model.Question
import io.github.leonardomanuelmendez.a2madrid.domain.model.AnsweredQuestion
import io.github.leonardomanuelmendez.a2madrid.domain.model.ScoreEntry
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.EvaluateAnswerUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetAttemptReviewUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetExamUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.SaveScoreUseCase
import io.github.leonardomanuelmendez.a2madrid.fake.FakeQuizRepository
import io.github.leonardomanuelmendez.a2madrid.presentation.quiz.QuizViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(mainDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val questions = listOf(
        Question(1, "Q1", listOf("a", "b"), correctAnswerIndex = 0),
        Question(2, "Q2", listOf("c", "d"), correctAnswerIndex = 1),
    )
    private val exam = Exam("modelo_a", "Modelo A", questions)

    private fun buildViewModel(repository: FakeQuizRepository): QuizViewModel =
        QuizViewModel(
            getExam = GetExamUseCase(repository),
            evaluateAnswer = EvaluateAnswerUseCase(),
            saveScore = SaveScoreUseCase(repository),
            getAttemptReview = GetAttemptReviewUseCase(GetExamUseCase(repository), repository),
        )

    @Test
    fun `loads questions for selected exam`() = runTest {
        val viewModel = buildViewModel(FakeQuizRepository(exams = listOf(exam)))

        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(exam.id, state.examId)
        assertEquals(exam.title, state.examTitle)
        assertEquals(2, state.totalQuestions)
        assertNull(state.errorMessage)
    }

    @Test
    fun `surfaces an error message when loading fails`() = runTest {
        val viewModel = buildViewModel(FakeQuizRepository(failOnLoad = true))

        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `confirming the correct answer increments the score`() = runTest {
        val viewModel = buildViewModel(FakeQuizRepository(exams = listOf(exam)))
        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        viewModel.selectOption(0)
        viewModel.confirmAnswer()

        val state = viewModel.uiState.value
        assertTrue(state.isAnswerConfirmed)
        assertEquals(1, state.correctAnswers)
    }

    @Test
    fun `cannot change selection after confirming`() = runTest {
        val viewModel = buildViewModel(FakeQuizRepository(exams = listOf(exam)))
        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        viewModel.selectOption(0)
        viewModel.confirmAnswer()
        viewModel.selectOption(1)

        assertEquals(0, viewModel.uiState.value.selectedOptionIndex)
    }

    @Test
    fun `completing the quiz produces a result with the final score`() = runTest {
        val viewModel = buildViewModel(FakeQuizRepository(exams = listOf(exam)))
        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        // Q1 correct
        viewModel.selectOption(0)
        viewModel.confirmAnswer()
        viewModel.nextQuestion()
        // Q2 correct
        viewModel.selectOption(1)
        viewModel.confirmAnswer()
        viewModel.nextQuestion()
        advanceUntilIdle()

        val result = viewModel.uiState.value.result
        assertNotNull(result)
        assertEquals(2, result!!.correctAnswers)
        assertEquals(2, result.totalQuestions)
        assertTrue(result.isNewBestScore)
    }

    @Test
    fun `guarda una respuesta por pregunta con la opcion elegida`() = runTest {
        val viewModel = buildViewModel(FakeQuizRepository(exams = listOf(exam)))
        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        // Q1 acertada (índice 0), Q2 fallada (elige 0, la correcta es 1).
        viewModel.selectOption(0)
        viewModel.confirmAnswer()
        viewModel.nextQuestion()
        viewModel.selectOption(0)
        viewModel.confirmAnswer()
        advanceUntilIdle()

        val answers = viewModel.uiState.value.answers
        assertEquals(2, answers.size)
        assertEquals(listOf(1, 2), answers.map { it.question.id })
        assertEquals(listOf(0, 0), answers.map { it.selectedOptionIndex })
        assertEquals(listOf(true, false), answers.map { it.isCorrect })
    }

    @Test
    fun `expone solo las falladas para poder repasarlas`() = runTest {
        val viewModel = buildViewModel(FakeQuizRepository(exams = listOf(exam)))
        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        viewModel.selectOption(0)   // Q1 correcta
        viewModel.confirmAnswer()
        viewModel.nextQuestion()
        viewModel.selectOption(0)   // Q2 fallada
        viewModel.confirmAnswer()
        advanceUntilIdle()

        val wrong = viewModel.uiState.value.wrongAnswers
        assertEquals(listOf(2), wrong.map { it.question.id })
        assertEquals(1, viewModel.uiState.value.correctAnswers)
    }

    @Test
    fun `al terminar persiste el detalle del intento y su identificador`() = runTest {
        val repository = FakeQuizRepository(exams = listOf(exam))
        val viewModel = buildViewModel(repository)
        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        viewModel.selectOption(0)
        viewModel.confirmAnswer()
        viewModel.nextQuestion()
        viewModel.selectOption(0)
        viewModel.confirmAnswer()
        viewModel.nextQuestion()
        advanceUntilIdle()

        val stored = repository.scoreHistory.first().single()
        assertEquals(listOf(1, 2), stored.answers.map { it.questionId })
        assertEquals(listOf(0, 0), stored.answers.map { it.selectedOptionIndex })

        // El resultado lleva el identificador del intento para poder recuperarlo luego.
        val result = viewModel.uiState.value.result
        assertNotNull(result)
        assertEquals(stored.timestampMillis, result!!.attemptMillis)
    }

    // ---- Modo repaso ----

    /** Historial con un intento en el que se falló solo la pregunta 2. */
    private fun repositoryWithAttempt(vararg answers: AnsweredQuestion) = FakeQuizRepository(
        exams = listOf(exam),
        initialHistory = listOf(
            ScoreEntry(
                examId = exam.id,
                examTitle = exam.title,
                correctAnswers = answers.size - answers.count { it.selectedOptionIndex != 0 },
                totalQuestions = 2,
                timestampMillis = 700L,
                answers = answers.toList(),
            ),
        ),
    )

    @Test
    fun `el repaso carga solo las preguntas falladas`() = runTest {
        // Q1 acertada (0 es correcta), Q2 fallada (eligió 0, la correcta es 1).
        val repository = repositoryWithAttempt(AnsweredQuestion(1, 0), AnsweredQuestion(2, 0))
        val viewModel = buildViewModel(repository)

        viewModel.loadReview(exam.id, attemptMillis = 700L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isReview)
        assertEquals(listOf(2), state.questions.map { it.id })
        assertEquals(1, state.totalQuestions)
    }

    @Test
    fun `un repaso no compite por el record del modelo`() = runTest {
        val repository = repositoryWithAttempt(AnsweredQuestion(1, 0), AnsweredQuestion(2, 0))
        val viewModel = buildViewModel(repository)

        viewModel.loadReview(exam.id, attemptMillis = 700L)
        advanceUntilIdle()
        viewModel.selectOption(1)   // ahora sí acierta la 2
        viewModel.confirmAnswer()
        viewModel.nextQuestion()
        advanceUntilIdle()

        val result = viewModel.uiState.value.result
        assertNotNull(result)
        assertEquals(1, result!!.correctAnswers)
        assertFalse(result.isNewBestScore)

        val saved = repository.scoreHistory.first().last()
        assertTrue(saved.isReview)
    }

    @Test
    fun `un repaso sin fallos no abre un test vacio`() = runTest {
        // Intento perfecto: no queda nada que repasar.
        val repository = repositoryWithAttempt(AnsweredQuestion(1, 0), AnsweredQuestion(2, 1))
        val viewModel = buildViewModel(repository)

        viewModel.loadReview(exam.id, attemptMillis = 700L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.questions.isEmpty())
        assertTrue(state.isEmptyReview)
    }
}
