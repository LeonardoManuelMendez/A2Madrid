package io.github.leonardomanuelmendez.a2madrid.domain

import io.github.leonardomanuelmendez.a2madrid.domain.model.AnsweredQuestion
import io.github.leonardomanuelmendez.a2madrid.domain.model.ScoreEntry
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.SaveScoreUseCase
import io.github.leonardomanuelmendez.a2madrid.fake.FakeQuizRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SaveScoreUseCaseTest {

    private val examId = "modelo_a"
    private val examTitle = "Modelo A"

    @Test
    fun `returns true and stores score when it beats previous best`() = runTest {
        val repository = FakeQuizRepository(
            initialHistory = listOf(ScoreEntry(examId, examTitle, 2, 10, 0L)),
        )
        val useCase = SaveScoreUseCase(repository)

        val saved = useCase(
            examId = examId,
            examTitle = examTitle,
            correctAnswers = 5,
            totalQuestions = 10,
        )

        assertTrue(saved.isNewBestScore)
        assertEquals(5, repository.scoreHistory.first().bestFor(examId))
    }

    @Test
    fun `returns false and keeps best when score does not improve`() = runTest {
        val repository = FakeQuizRepository(
            initialHistory = listOf(ScoreEntry(examId, examTitle, 7, 10, 0L)),
        )
        val useCase = SaveScoreUseCase(repository)

        val saved = useCase(
            examId = examId,
            examTitle = examTitle,
            correctAnswers = 4,
            totalQuestions = 10,
        )

        assertFalse(saved.isNewBestScore)
        assertEquals(7, repository.scoreHistory.first().bestFor(examId))
    }

    @Test
    fun `appends every score to the history`() = runTest {
        val repository = FakeQuizRepository()
        val useCase = SaveScoreUseCase(repository)

        useCase(examId = examId, examTitle = examTitle, correctAnswers = 5, totalQuestions = 10)
        useCase(examId = examId, examTitle = examTitle, correctAnswers = 8, totalQuestions = 10)

        val history = repository.scoreHistory.first()
        assertEquals(2, history.size)
        assertEquals(8, history.bestFor(examId))
    }

    @Test
    fun `compares scores only within the same exam model`() = runTest {
        val repository = FakeQuizRepository(
            initialHistory = listOf(ScoreEntry("modelo_b", "Modelo B", 10, 10, 0L)),
        )
        val useCase = SaveScoreUseCase(repository)

        val saved = useCase(
            examId = examId,
            examTitle = examTitle,
            correctAnswers = 4,
            totalQuestions = 10,
        )

        assertTrue(saved.isNewBestScore)
        assertEquals(4, repository.scoreHistory.first().bestFor(examId))
    }

    @Test
    fun `guarda el detalle de las respuestas del intento`() = runTest {
        val repository = FakeQuizRepository()
        val useCase = SaveScoreUseCase(repository)
        val answers = listOf(AnsweredQuestion(1, 0), AnsweredQuestion(2, 3))

        val saved = useCase(
            examId = examId,
            examTitle = examTitle,
            correctAnswers = 1,
            totalQuestions = 2,
            answers = answers,
        )

        assertEquals(answers, repository.scoreHistory.first().single().answers)
        // Quien llama necesita el instante del intento para volver a localizarlo después.
        assertEquals(saved.entry.timestampMillis, repository.scoreHistory.first().single().timestampMillis)
    }

    @Test
    fun `un repaso con pleno de aciertos no falsea el record del modelo`() = runTest {
        val repository = FakeQuizRepository(
            initialHistory = listOf(ScoreEntry(examId, examTitle, 30, 45, 0L)),
        )
        val useCase = SaveScoreUseCase(repository)

        // Repaso de los 6 fallos, acertados todos: 6/6 no puede leerse como récord de un
        // modelo de 45 preguntas cuya mejor marca real es 30.
        val saved = useCase(
            examId = examId,
            examTitle = examTitle,
            correctAnswers = 6,
            totalQuestions = 6,
            isReview = true,
        )

        assertFalse(saved.isNewBestScore)
        assertTrue(saved.entry.isReview)
    }

    @Test
    fun `un repaso anterior no cuenta al calcular la mejor marca`() = runTest {
        val repository = FakeQuizRepository(
            initialHistory = listOf(
                ScoreEntry(examId, examTitle, 3, 45, 0L),
                ScoreEntry(examId, examTitle, 6, 6, 1L, isReview = true),
            ),
        )
        val useCase = SaveScoreUseCase(repository)

        // 4 aciertos mejoran el 3 del único intento completo, aunque no lleguen al 6 del repaso.
        val saved = useCase(
            examId = examId,
            examTitle = examTitle,
            correctAnswers = 4,
            totalQuestions = 45,
        )

        assertTrue(saved.isNewBestScore)
    }

    @Test
    fun `un simulacro se guarda marcado y no genera récord`() = runTest {
        val repository = FakeQuizRepository()
        val useCase = SaveScoreUseCase(repository)

        val saved = useCase(
            examId = "simulacro_completo",
            examTitle = "Simulacro completo",
            correctAnswers = 90,
            totalQuestions = 137,
            isExam = true,
        )

        assertTrue(saved.entry.isExam)
        assertFalse(saved.isNewBestScore, "un simulacro no es la marca de ningún modelo suelto")
    }

    private fun List<ScoreEntry>.bestFor(examId: String): Int =
        filter { it.examId == examId }.maxOfOrNull { it.correctAnswers } ?: 0
}