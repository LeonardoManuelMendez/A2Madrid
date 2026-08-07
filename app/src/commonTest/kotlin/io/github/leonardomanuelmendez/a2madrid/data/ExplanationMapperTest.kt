package io.github.leonardomanuelmendez.a2madrid.data

import io.github.leonardomanuelmendez.a2madrid.data.dto.QuestionDto
import io.github.leonardomanuelmendez.a2madrid.data.mapper.toDomain
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Guards the JSON → domain path of the explanation, since every question in `exams.json`
 * depends on this shape.
 */
class ExplanationMapperTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `parses a full explanation from json`() {
        val dto = json.decodeFromString(
            QuestionDto.serializer(),
            """
            {
              "id": 1,
              "text": "¿Fuentes del ordenamiento jurídico español?",
              "options": ["Dos", "Tres"],
              "correctAnswerIndex": 1,
              "explanation": {
                "summary": "Son tres: la ley, la costumbre y los principios generales.",
                "points": [
                  { "term": "Jurisprudencia", "text": "complementa el ordenamiento, no es fuente." },
                  { "text": "La costumbre solo rige en defecto de ley aplicable." }
                ],
                "source": {
                  "label": "Código Civil, art. 1.1",
                  "quote": "Las fuentes del ordenamiento jurídico español son la ley, la costumbre y los principios generales del derecho."
                }
              }
            }
            """.trimIndent(),
        )

        val explanation = dto.toDomain().explanation

        assertEquals("Son tres: la ley, la costumbre y los principios generales.", explanation?.summary)
        assertEquals(2, explanation?.points?.size)
        assertEquals("Jurisprudencia", explanation?.points?.first()?.term)
        assertNull(explanation?.points?.last()?.term)
        assertEquals("Código Civil, art. 1.1", explanation?.source?.label)
        assertTrue(explanation!!.hasDetail)
    }

    @Test
    fun `a question without explanation still maps`() {
        val dto = json.decodeFromString(
            QuestionDto.serializer(),
            """{"id": 2, "text": "T", "options": ["a", "b"], "correctAnswerIndex": 0}""",
        )

        assertNull(dto.toDomain().explanation)
    }

    @Test
    fun `a summary alone has no detail to unfold`() {
        val dto = json.decodeFromString(
            QuestionDto.serializer(),
            """
            {
              "id": 3,
              "text": "T",
              "options": ["a", "b"],
              "correctAnswerIndex": 0,
              "explanation": { "summary": "Porque sí." }
            }
            """.trimIndent(),
        )

        val explanation = dto.toDomain().explanation

        assertEquals(emptyList(), explanation?.points)
        assertNull(explanation?.source)
        assertFalse(explanation!!.hasDetail)
    }
}
