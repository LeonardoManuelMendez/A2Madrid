package io.github.leonardomanuelmendez.a2madrid.data

import io.github.leonardomanuelmendez.a2madrid.data.dto.QuestionDto
import io.github.leonardomanuelmendez.a2madrid.data.mapper.toDomain
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `lockOptionOrder` es el único dato que el código no puede deducir por su cuenta: marca las
 * preguntas cuyas opciones se refieren a otras opciones («la respuesta b) es correcta»,
 * «todas las anteriores»), que quedan sin sentido si se barajan. Este test protege ese camino.
 */
class QuestionMapperTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun parse(raw: String) = json.decodeFromString(QuestionDto.serializer(), raw).toDomain()

    @Test
    fun `una pregunta corriente admite barajar sus opciones`() {
        val question = parse(
            """
            {
              "id": 1,
              "text": "¿Cuántas fuentes del ordenamiento jurídico hay?",
              "options": ["Dos", "Tres", "Cuatro", "Cinco"],
              "correctAnswerIndex": 1
            }
            """.trimIndent(),
        )

        assertFalse(question.lockOptionOrder, "por defecto una pregunta debe ser barajable")
    }

    @Test
    fun `una pregunta con opciones autorreferentes llega marcada`() {
        val question = parse(
            """
            {
              "id": 47,
              "text": "…la tutela de:",
              "options": ["Sección primera", "Sección segunda", "Capítulo III",
                          "La respuesta b) es correcta y además la objeción de conciencia"],
              "correctAnswerIndex": 0,
              "lockOptionOrder": true
            }
            """.trimIndent(),
        )

        assertTrue(question.lockOptionOrder)
    }
}
