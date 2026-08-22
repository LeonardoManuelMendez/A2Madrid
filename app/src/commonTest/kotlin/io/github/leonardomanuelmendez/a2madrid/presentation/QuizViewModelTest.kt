package io.github.leonardomanuelmendez.a2madrid.presentation

import io.github.leonardomanuelmendez.a2madrid.domain.model.AnsweredQuestion
import io.github.leonardomanuelmendez.a2madrid.domain.model.Exam
import io.github.leonardomanuelmendez.a2madrid.domain.model.Question
import io.github.leonardomanuelmendez.a2madrid.domain.model.ScoreEntry
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.EvaluateAnswerUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetAttemptReviewUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetExamUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.SaveScoreUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.ShuffleQuestionsUseCase
import io.github.leonardomanuelmendez.a2madrid.fake.FakeQuizRepository
import io.github.leonardomanuelmendez.a2madrid.presentation.quiz.QuizUiState
import io.github.leonardomanuelmendez.a2madrid.presentation.quiz.QuizViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.random.Random
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
        Question(1, "Q1", listOf("a1", "b1", "c1", "d1"), correctAnswerIndex = 0),
        Question(2, "Q2", listOf("a2", "b2", "c2", "d2"), correctAnswerIndex = 1),
    )
    private val exam = Exam("modelo_a", "Modelo A", questions)

    /**
     * Como la sesión se baraja, los tests no pueden razonar en «la opción 0»: eligen por
     * CONTENIDO y dejan que el ViewModel traduzca. Es justo lo que hace la pantalla.
     */
    private fun QuizViewModel.selectByText(text: String) {
        val state = uiState.value
        val question = state.currentQuestion ?: error("no hay pregunta actual")
        val displayed = state.displayedOptions(question).indexOf(text)
        check(displayed >= 0) { "la opción '$text' no se está mostrando" }
        selectOption(displayed)
    }

    private fun QuizViewModel.selectCorrect() {
        val question = uiState.value.currentQuestion ?: error("no hay pregunta actual")
        selectByText(question.correctAnswer)
    }

    private fun QuizViewModel.selectWrong() {
        val question = uiState.value.currentQuestion ?: error("no hay pregunta actual")
        selectByText(question.options.first { it != question.correctAnswer })
    }

    private fun buildViewModel(
        repository: FakeQuizRepository,
        shuffle: ShuffleQuestionsUseCase = ShuffleQuestionsUseCase(Random(0)),
    ): QuizViewModel = QuizViewModel(
        getExam = GetExamUseCase(repository),
        evaluateAnswer = EvaluateAnswerUseCase(),
        saveScore = SaveScoreUseCase(repository),
        getAttemptReview = GetAttemptReviewUseCase(GetExamUseCase(repository), repository),
        shuffleQuestions = shuffle,
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

        viewModel.selectCorrect()
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

        viewModel.selectCorrect()
        viewModel.confirmAnswer()
        val elegida = viewModel.uiState.value.selectedOptionIndex
        viewModel.selectWrong()

        assertEquals(elegida, viewModel.uiState.value.selectedOptionIndex)
    }

    @Test
    fun `completing the quiz produces a result with the final score`() = runTest {
        val viewModel = buildViewModel(FakeQuizRepository(exams = listOf(exam)))
        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        repeat(questions.size) {
            viewModel.selectCorrect()
            viewModel.confirmAnswer()
            viewModel.nextQuestion()
        }
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

        viewModel.selectCorrect()
        viewModel.confirmAnswer()
        viewModel.nextQuestion()
        viewModel.selectWrong()
        viewModel.confirmAnswer()
        advanceUntilIdle()

        val answers = viewModel.uiState.value.answers
        assertEquals(2, answers.size)
        assertEquals(setOf(1, 2), answers.map { it.question.id }.toSet())
        assertEquals(listOf(true, false), answers.map { it.isCorrect })
        // El índice guardado apunta siempre al orden canónico del examen.
        answers.forEach { answer ->
            val esperado = answer.question.options[answer.selectedOptionIndex]
            val canonica = questions.first { it.id == answer.question.id }
                .options[answer.selectedOptionIndex]
            assertEquals(esperado, canonica)
        }
    }

    @Test
    fun `expone solo las falladas para poder repasarlas`() = runTest {
        val viewModel = buildViewModel(FakeQuizRepository(exams = listOf(exam)))
        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        val primeraId = viewModel.uiState.value.currentQuestion!!.id
        viewModel.selectCorrect()
        viewModel.confirmAnswer()
        viewModel.nextQuestion()
        val segundaId = viewModel.uiState.value.currentQuestion!!.id
        viewModel.selectWrong()
        viewModel.confirmAnswer()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(segundaId), state.wrongAnswers.map { it.question.id })
        assertEquals(1, state.correctAnswers)
        assertTrue(primeraId != segundaId)
    }

    @Test
    fun `al terminar persiste el detalle del intento y su identificador`() = runTest {
        val repository = FakeQuizRepository(exams = listOf(exam))
        val viewModel = buildViewModel(repository)
        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        repeat(questions.size) {
            viewModel.selectCorrect()
            viewModel.confirmAnswer()
            viewModel.nextQuestion()
        }
        advanceUntilIdle()

        val stored = repository.scoreHistory.first().single()
        assertEquals(setOf(1, 2), stored.answers.map { it.questionId }.toSet())

        val result = viewModel.uiState.value.result
        assertNotNull(result)
        assertEquals(stored.timestampMillis, result!!.attemptMillis)
    }

    // ---- Barajado ----

    @Test
    fun `guarda el indice canonico aunque las opciones se muestren barajadas`() = runTest {
        val viewModel = buildViewModel(FakeQuizRepository(exams = listOf(exam)), ShuffleQuestionsUseCase(Random(5)))
        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val question = state.currentQuestion!!
        val textoPulsado = state.displayedOptions(question)[0]

        viewModel.selectOption(0) // el usuario pulsa la PRIMERA opción que ve
        viewModel.confirmAnswer()

        val answer = viewModel.uiState.value.answers.single()
        // Lo guardado indexa el orden canónico, no la posición de pantalla.
        assertEquals(textoPulsado, question.options[answer.selectedOptionIndex])
    }

    @Test
    fun `el desglose de un intento barajado senala la opcion que se pulso`() = runTest {
        // Este es el test que protege el repaso de fallos ya publicado: si el barajado se colara
        // en lo persistido, el desglose del día siguiente señalaría otra opción distinta.
        val repository = FakeQuizRepository(exams = listOf(exam))
        val viewModel = buildViewModel(repository, ShuffleQuestionsUseCase(Random(11)))
        viewModel.loadExam(exam.id)
        advanceUntilIdle()

        val pulsado = mutableMapOf<Int, String>()
        repeat(questions.size) {
            val state = viewModel.uiState.value
            val question = state.currentQuestion!!
            pulsado[question.id] = state.displayedOptions(question)[0]
            viewModel.selectOption(0)
            viewModel.confirmAnswer()
            viewModel.nextQuestion()
        }
        advanceUntilIdle()

        val entry = repository.scoreHistory.first().single()
        val desglose = GetAttemptReviewUseCase(GetExamUseCase(repository), repository)(
            examId = exam.id,
            attemptMillis = entry.timestampMillis,
        )

        assertEquals(
            pulsado,
            desglose.associate { it.question.id to it.question.options[it.selectedOptionIndex] },
        )
    }

    @Test
    fun `un examen sin barajar muestra las opciones en su orden original`() = runTest {
        // Sin permutación registrada, el estado debe degradar a la identidad.
        val state = QuizUiState(questions = questions, isLoading = false)

        assertEquals(questions[0].options, state.displayedOptions(questions[0]))
        assertEquals(2, state.canonicalOptionIndex(questions[0], 2))
    }

    // ---- Modo repaso ----

    /** Historial con un intento del examen completo, con las respuestas indicadas. */
    private fun repositoryWithAttempt(vararg answers: AnsweredQuestion) = FakeQuizRepository(
        exams = listOf(exam),
        initialHistory = listOf(
            ScoreEntry(
                examId = exam.id,
                examTitle = exam.title,
                correctAnswers = answers.count { answer ->
                    questions.first { it.id == answer.questionId }
                        .isCorrect(answer.selectedOptionIndex)
                },
                totalQuestions = 2,
                timestampMillis = 700L,
                answers = answers.toList(),
            ),
        ),
    )

    @Test
    fun `el repaso carga solo las preguntas falladas`() = runTest {
        // Q1 acertada (0 es la correcta), Q2 fallada (eligió 0, la correcta es 1).
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
        viewModel.selectCorrect()
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
        val repository = repositoryWithAttempt(AnsweredQuestion(1, 0), AnsweredQuestion(2, 1))
        val viewModel = buildViewModel(repository)

        viewModel.loadReview(exam.id, attemptMillis = 700L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.questions.isEmpty())
        assertTrue(state.isEmptyReview)
    }
}
