package io.github.leonardomanuelmendez.a2madrid.domain

import io.github.leonardomanuelmendez.a2madrid.domain.model.Question
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.ShuffleQuestionsUseCase
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShuffleQuestionsUseCaseTest {

    private fun question(id: Int, context: String? = null, locked: Boolean = false) = Question(
        id = id,
        text = "Q$id",
        options = listOf("a", "b", "c", "d"),
        correctAnswerIndex = 0,
        context = context,
        lockOptionOrder = locked,
    )

    /** Imita la forma real del examen de aptitudes: sueltas, un bloque con estímulo, más sueltas. */
    private val questions = listOf(
        question(1), question(2), question(3),
        question(4, context = "rejilla"),
        question(5, context = "rejilla"),
        question(6, context = "rejilla"),
        question(7), question(8),
    )

    @Test
    fun `conserva exactamente las mismas preguntas`() {
        val shuffled = ShuffleQuestionsUseCase(Random(1))(questions)

        assertEquals(questions.map { it.id }.toSet(), shuffled.questions.map { it.id }.toSet())
        assertEquals(questions.size, shuffled.questions.size)
    }

    @Test
    fun `las preguntas que comparten estimulo siguen contiguas`() {
        // Con muchas semillas, porque un fallo de agrupación puede no aparecer con una sola.
        repeat(50) { seed ->
            val shuffled = ShuffleQuestionsUseCase(Random(seed))(questions)
            val posiciones = shuffled.questions
                .mapIndexedNotNull { index, q -> index.takeIf { q.context == "rejilla" } }

            assertEquals(3, posiciones.size)
            assertEquals(
                (posiciones.first()..posiciones.first() + 2).toList(),
                posiciones,
                "las de rejilla quedaron dispersas con la semilla $seed: $posiciones",
            )
        }
    }

    @Test
    fun `una pregunta con el orden bloqueado conserva sus opciones`() {
        val conBloqueada = questions + question(9, locked = true)

        repeat(20) { seed ->
            val shuffled = ShuffleQuestionsUseCase(Random(seed))(conBloqueada)
            assertEquals(listOf(0, 1, 2, 3), shuffled.optionOrder.getValue(9))
        }
    }

    @Test
    fun `la permutacion de opciones es una biyeccion`() {
        val shuffled = ShuffleQuestionsUseCase(Random(3))(questions)

        questions.forEach { q ->
            val order = shuffled.optionOrder.getValue(q.id)
            assertEquals(q.options.indices.toList(), order.sorted(), "permutación inválida en ${q.id}")
        }
    }

    @Test
    fun `la misma semilla produce el mismo barajado`() {
        val a = ShuffleQuestionsUseCase(Random(42))(questions)
        val b = ShuffleQuestionsUseCase(Random(42))(questions)

        assertEquals(a.questions.map { it.id }, b.questions.map { it.id })
        assertEquals(a.optionOrder, b.optionOrder)
    }

    @Test
    fun `baraja de verdad, tanto las preguntas como las opciones`() {
        // Si la implementación no barajara nada, todo esto pasaría igual salvo estas dos
        // comprobaciones. Son las que dan sentido al caso de uso.
        val ordenOriginal = questions.map { it.id }
        val identidad = listOf(0, 1, 2, 3)

        val ordenesDistintos = (1..20).count {
            ShuffleQuestionsUseCase(Random(it))(questions).questions.map { q -> q.id } != ordenOriginal
        }
        val opcionesDistintas = (1..20).count {
            ShuffleQuestionsUseCase(Random(it))(questions).optionOrder.getValue(1) != identidad
        }

        assertTrue(ordenesDistintos >= 18, "el orden de preguntas apenas cambia ($ordenesDistintos/20)")
        assertTrue(opcionesDistintas >= 15, "el orden de opciones apenas cambia ($opcionesDistintas/20)")
    }
}
