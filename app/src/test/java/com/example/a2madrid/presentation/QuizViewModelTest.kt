package com.example.a2madrid.presentation

import com.example.a2madrid.domain.model.Exam
import com.example.a2madrid.domain.model.Question
import com.example.a2madrid.domain.usecase.EvaluateAnswerUseCase
import com.example.a2madrid.domain.usecase.GetExamUseCase
import com.example.a2madrid.domain.usecase.SaveScoreUseCase
import com.example.a2madrid.fake.FakeQuizRepository
import com.example.a2madrid.presentation.quiz.QuizViewModel
import com.example.a2madrid.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
}
