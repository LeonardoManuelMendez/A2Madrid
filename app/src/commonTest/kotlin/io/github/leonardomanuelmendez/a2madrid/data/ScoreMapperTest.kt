package io.github.leonardomanuelmendez.a2madrid.data

import io.github.leonardomanuelmendez.a2madrid.data.dto.ScoreEntryDto
import io.github.leonardomanuelmendez.a2madrid.data.mapper.toDomain
import io.github.leonardomanuelmendez.a2madrid.data.mapper.toDto
import io.github.leonardomanuelmendez.a2madrid.domain.model.AnsweredQuestion
import io.github.leonardomanuelmendez.a2madrid.domain.model.ScoreEntry
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Protege el formato de almacenamiento: las respuestas del intento se persisten, y —lo más
 * importante— un historial escrito por una versión ANTERIOR de la app se sigue leyendo.
 */
class ScoreMapperTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `las respuestas del intento sobreviven a la ida y vuelta DTO`() {
        val entry = ScoreEntry(
            examId = "modelo_a",
            examTitle = "Modelo A",
            correctAnswers = 1,
            totalQuestions = 2,
            timestampMillis = 1_000L,
            answers = listOf(AnsweredQuestion(1, 0), AnsweredQuestion(2, 3)),
        )

        assertEquals(entry, entry.toDto().toDomain())
    }

    @Test
    fun `un repaso se persiste marcado como repaso`() {
        val entry = ScoreEntry("modelo_a", "Modelo A", 3, 3, 1L, isReview = true)

        assertTrue(entry.toDto().toDomain().isReview)
    }

    @Test
    fun `un historial guardado por la version anterior se lee sin perder datos`() {
        // Forma EXACTA del JSON antes de este cambio: sin `answers` ni `isReview`.
        val legacy = """
            {"examId":"modelo_a","examTitle":"Modelo A","correctAnswers":8,
             "totalQuestions":10,"timestampMillis":1700000000000}
        """.trimIndent()

        val restored = json.decodeFromString(ScoreEntryDto.serializer(), legacy).toDomain()

        assertEquals("modelo_a", restored.examId)
        assertEquals(8, restored.correctAnswers)
        assertEquals(10, restored.totalQuestions)
        assertEquals(1_700_000_000_000L, restored.timestampMillis)
        // Un intento antiguo simplemente no ofrece desglose; no debe romper la lectura.
        assertTrue(restored.answers.isEmpty())
        assertFalse(restored.isReview)
    }

    @Test
    fun `en un simulacro los blancos no cuentan como fallos`() {
        // 44 preguntas, 20 contestadas (12 acertadas) y 24 en blanco.
        val entry = ScoreEntry(
            examId = "modelo_a",
            examTitle = "Modelo A",
            correctAnswers = 12,
            totalQuestions = 44,
            timestampMillis = 1L,
            answers = (1..20).map { AnsweredQuestion(it, 0) },
            isExam = true,
        )

        assertEquals(8, entry.wrongAnswers, "fallos = contestadas − acertadas")
        assertEquals(24, entry.blankAnswers)
    }

    @Test
    fun `un intento sin desglose sigue contando los fallos como antes`() {
        // Historial anterior a esta funcionalidad: sin `answers`, todo lo no acertado es fallo.
        val legacy = ScoreEntry("modelo_a", "Modelo A", 30, 44, 1L)

        assertEquals(14, legacy.wrongAnswers)
        assertEquals(0, legacy.blankAnswers)
    }
}
