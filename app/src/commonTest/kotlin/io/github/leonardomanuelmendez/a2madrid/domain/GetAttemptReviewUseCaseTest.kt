package io.github.leonardomanuelmendez.a2madrid.domain

import io.github.leonardomanuelmendez.a2madrid.domain.model.AnsweredQuestion
import io.github.leonardomanuelmendez.a2madrid.domain.model.Exam
import io.github.leonardomanuelmendez.a2madrid.domain.model.Question
import io.github.leonardomanuelmendez.a2madrid.domain.model.ScoreEntry
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetAttemptReviewUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetExamUseCase
import io.github.leonardomanuelmendez.a2madrid.fake.FakeQuizRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * El desglose no se guarda: se RECONSTRUYE cruzando las respuestas persistidas (solo ids) con
 * las preguntas del examen. Estos tests cubren ese cruce y sus casos degradados.
 */
class GetAttemptReviewUseCaseTest {

    private val exam = Exam(
        id = "modelo_a",
        title = "Modelo A",
        questions = listOf(
            Question(1, "Q1", listOf("a", "b"), correctAnswerIndex = 0),
            Question(2, "Q2", listOf("c", "d"), correctAnswerIndex = 1),
            Question(3, "Q3", listOf("e", "f"), correctAnswerIndex = 0),
        ),
    )

    private fun useCaseWith(vararg history: ScoreEntry): GetAttemptReviewUseCase {
        val repository = FakeQuizRepository(exams = listOf(exam), initialHistory = history.toList())
        return GetAttemptReviewUseCase(GetExamUseCase(repository), repository)
    }

    @Test
    fun `reconstruye el detalle del intento cruzando por id de pregunta`() = runTest {
        val useCase = useCaseWith(
            ScoreEntry(
                examId = exam.id,
                examTitle = exam.title,
                correctAnswers = 1,
                totalQuestions = 3,
                timestampMillis = 500L,
                answers = listOf(
                    AnsweredQuestion(1, 0),  // acierto
                    AnsweredQuestion(2, 0),  // fallo
                    AnsweredQuestion(3, 1),  // fallo
                ),
            ),
        )

        val review = useCase(exam.id, attemptMillis = 500L)

        assertEquals(listOf(1, 2, 3), review.map { it.question.id })
        assertEquals(listOf("Q1", "Q2", "Q3"), review.map { it.question.text })
        assertEquals(listOf(true, false, false), review.map { it.isCorrect })
        assertEquals(listOf(0, 0, 1), review.map { it.selectedOptionIndex })
    }

    @Test
    fun `devuelve el detalle en el orden del examen, no en el de respuesta`() = runTest {
        val useCase = useCaseWith(
            ScoreEntry(
                examId = exam.id,
                examTitle = exam.title,
                correctAnswers = 0,
                totalQuestions = 2,
                timestampMillis = 500L,
                answers = listOf(AnsweredQuestion(3, 1), AnsweredQuestion(1, 1)),
            ),
        )

        assertEquals(listOf(1, 3), useCase(exam.id, 500L).map { it.question.id })
    }

    @Test
    fun `un intento inexistente devuelve lista vacia`() = runTest {
        val useCase = useCaseWith(
            ScoreEntry(exam.id, exam.title, 1, 3, 500L, answers = listOf(AnsweredQuestion(1, 0))),
        )

        assertTrue(useCase(exam.id, attemptMillis = 999L).isEmpty())
    }

    @Test
    fun `un intento guardado por la version anterior no tiene desglose`() = runTest {
        // Sin `answers`: es lo que hay en el historial de quien ya tenía la app instalada.
        val useCase = useCaseWith(ScoreEntry(exam.id, exam.title, 2, 3, 500L))

        assertTrue(useCase(exam.id, attemptMillis = 500L).isEmpty())
    }

    @Test
    fun `ignora respuestas que ya no casan con el examen`() = runTest {
        // El contenido de exams.json puede cambiar entre versiones: una pregunta retirada, o una
        // con menos opciones. El desglose debe degradarse, nunca reventar.
        val useCase = useCaseWith(
            ScoreEntry(
                examId = exam.id,
                examTitle = exam.title,
                correctAnswers = 1,
                totalQuestions = 3,
                timestampMillis = 500L,
                answers = listOf(
                    AnsweredQuestion(1, 0),
                    AnsweredQuestion(99, 0),  // pregunta que ya no existe
                    AnsweredQuestion(2, 7),   // opción fuera de rango
                ),
            ),
        )

        assertEquals(listOf(1), useCase(exam.id, 500L).map { it.question.id })
    }
}
