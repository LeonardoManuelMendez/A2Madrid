package io.github.leonardomanuelmendez.a2madrid.domain

import io.github.leonardomanuelmendez.a2madrid.domain.model.AnswerResult
import io.github.leonardomanuelmendez.a2madrid.domain.model.ExamScore
import io.github.leonardomanuelmendez.a2madrid.domain.model.Question
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * La regla que define el simulacro: «la pregunta con contestación errónea se penalizará con la
 * tercera parte del valor asignado a la contestación correcta», y «la pregunta no contestada no
 * tendrá valoración» (instrucciones del 1er ejercicio, Orden 2411/2017).
 */
class ExamScoreTest {

    private fun answer(id: Int, correct: Boolean): AnswerResult {
        val question = Question(id, "Q$id", listOf("a", "b", "c", "d"), correctAnswerIndex = 0)
        return AnswerResult(question, selectedOptionIndex = if (correct) 0 else 1, isCorrect = correct)
    }

    @Test
    fun `cada error resta un tercio de acierto`() {
        val score = ExamScore(correct = 10, wrong = 3, blank = 2)

        assertEquals(9.0, score.net)
        assertEquals(15, score.totalQuestions)
        assertEquals(13, score.answered)
    }

    @Test
    fun `sin errores el neto es el número de aciertos`() {
        assertEquals(20.0, ExamScore(correct = 20, wrong = 0, blank = 5).net)
    }

    @Test
    fun `los blancos no puntúan ni penalizan`() {
        val sinBlancos = ExamScore(correct = 8, wrong = 6, blank = 0)
        val conBlancos = ExamScore(correct = 8, wrong = 6, blank = 40)

        assertEquals(sinBlancos.net, conBlancos.net)
        assertEquals(6.0, conBlancos.net)
    }

    @Test
    fun `un ejercicio entero en blanco vale cero`() {
        val score = ExamScore(correct = 0, wrong = 0, blank = 137)

        assertEquals(0.0, score.net)
        assertEquals(0, score.answered)
    }

    @Test
    fun `tres errores anulan exactamente un acierto`() {
        assertEquals(0.0, ExamScore(correct = 1, wrong = 3, blank = 0).net)
    }

    @Test
    fun `el neto puede ser negativo si se falla más de lo que se acierta`() {
        // Contestar a todo sin saber no sale gratis: es la lección del simulacro.
        assertEquals(-3.0, ExamScore(correct = 3, wrong = 18, blank = 0).net)
    }

    @Test
    fun `se deriva de las respuestas dadas y del total de preguntas`() {
        val answers = listOf(answer(1, true), answer(2, false), answer(3, true), answer(4, false))

        val score = ExamScore.from(answers, totalQuestions = 10)

        assertEquals(2, score.correct)
        assertEquals(2, score.wrong)
        assertEquals(6, score.blank, "las no respondidas son blancos")
        assertEquals(2 - 2 / 3.0, score.net)
    }
}
