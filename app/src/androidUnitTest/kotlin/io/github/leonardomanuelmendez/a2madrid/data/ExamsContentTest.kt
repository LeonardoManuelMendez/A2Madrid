package io.github.leonardomanuelmendez.a2madrid.data

import io.github.leonardomanuelmendez.a2madrid.data.dto.ExamsFileDto
import io.github.leonardomanuelmendez.a2madrid.data.mapper.toDomain
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Valida el `exams.json` REAL, el que se empaqueta en la app.
 *
 * Es el único test que toca el contenido en vez del código, y vive en `androidUnitTest` porque
 * necesita leer un fichero del disco, cosa que `commonTest` no puede hacer de forma
 * multiplataforma. Sin él, un `correctAnswerIndex` fuera de rango o una errata en el nombre de un
 * campo llegan a producción: lo primero revienta en el `init` de Question y el usuario ve
 * «No se pudo cargar el examen»; lo segundo se traga en silencio y la marca no se aplica.
 */
class ExamsContentTest {

    /** Estricto A PROPÓSITO: con `ignoreUnknownKeys` una errata como `lockOptionsOrder` pasaría
     *  desapercibida y la pregunta se barajaría igualmente. */
    private val strictJson = Json { ignoreUnknownKeys = false; isLenient = false }

    private val examsFile: File = listOf(
        "src/commonMain/composeResources/files/exams.json",
        "app/src/commonMain/composeResources/files/exams.json",
    ).map(::File).firstOrNull { it.exists() }
        ?: fail("No se encuentra exams.json desde ${File(".").absolutePath}")

    private val exams = strictJson
        .decodeFromString(ExamsFileDto.serializer(), examsFile.readText())
        .exams

    /**
     * Opciones cuyo sentido depende del orden: o citan a otra opción por su letra, o son un
     * comodín («todas», «ninguna») que solo se entiende leído después del resto. Barajarlas las
     * deja sin sentido, así que su pregunta tiene que llevar `lockOptionOrder`.
     *
     * El patrón es DELIBERADAMENTE estrecho. Una versión más amplia daba falsos positivos con
     * texto legítimo —«Ninguna» como recuento en un psicotécnico, «la Cinta de Opciones»— y un
     * guardián que grita de más se acaba desactivando.
     */
    private val orderDependentOption = Regex(
        """(\b[a-d]\s+y\s+[a-d]\s+son\s+correctas""" +
            """|\b(todas|ninguna)\s+(las\s+)?(respuestas?\s+)?(anteriores\s+)?(son\s+correctas|es\s+correcta)""" +
            """|\brespuestas?\s+[a-d]\)""" +
            """|\bopci[óo]n\s+[a-d]\))""",
        RegexOption.IGNORE_CASE,
    )

    /**
     * Explicaciones que señalan una opción por su letra o por su posición («la opción a)»,
     * «la opción 2»). Con el barajado, esa referencia apunta a otra cosa en pantalla. Solo se
     * permite en preguntas con el orden bloqueado.
     */
    private val optionPositionReference = Regex(
        """\b(la|las)\s+(opci[óo]n(es)?|respuestas?)\s+""" +
            """([a-d]\)|\d\b|primera|segunda|tercera|cuarta|última)""",
        RegexOption.IGNORE_CASE,
    )

    @Test
    fun `todos los modelos se parsean y producen preguntas de dominio válidas`() {
        assertTrue(exams.isNotEmpty(), "el fichero no tiene ningún examen")
        exams.forEach { exam ->
            val contexts = exam.contexts
            assertTrue(exam.questions.isNotEmpty(), "${exam.id} no tiene preguntas")
            // toDomain() ejecuta los require() de Question: opciones suficientes e índice en rango.
            exam.questions.forEach { it.toDomain(contexts) }
        }
    }

    @Test
    fun `los identificadores de pregunta son únicos en todo el fichero`() {
        val ids = exams.flatMap { exam -> exam.questions.map { it.id } }
        val repetidos = ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys

        assertEquals(emptySet(), repetidos, "ids repetidos entre modelos")
    }

    @Test
    fun `cada pregunta ofrece cuatro opciones y una correcta dentro de rango`() {
        exams.forEach { exam ->
            exam.questions.forEach { question ->
                assertEquals(
                    4,
                    question.options.size,
                    "${exam.id} · pregunta ${question.id}: se esperan 4 opciones",
                )
                assertTrue(
                    question.correctAnswerIndex in question.options.indices,
                    "${exam.id} · pregunta ${question.id}: correctAnswerIndex fuera de rango",
                )
                assertTrue(
                    question.options.none { it.isBlank() },
                    "${exam.id} · pregunta ${question.id}: alguna opción está vacía",
                )
            }
        }
    }

    @Test
    fun `toda pregunta explica su respuesta`() {
        exams.forEach { exam ->
            exam.questions.forEach { question ->
                val explanation = question.explanation
                    ?: fail("${exam.id} · pregunta ${question.id}: sin explicación")
                assertTrue(
                    explanation.summary.isNotBlank(),
                    "${exam.id} · pregunta ${question.id}: resumen vacío",
                )
            }
        }
    }

    @Test
    fun `las preguntas con opciones que dependen del orden lo tienen bloqueado`() {
        val sinMarcar = exams.flatMap { exam ->
            exam.questions
                .filter { question ->
                    !question.lockOptionOrder &&
                        question.options.any(orderDependentOption::containsMatchIn)
                }
                .map { "${exam.id}·${it.id}" }
        }

        assertEquals(
            emptyList(),
            sinMarcar,
            "estas opciones citan a otras o son un comodín, y se barajarían igualmente",
        )
    }

    @Test
    fun `ninguna explicación señala una opción por su letra o su posición`() {
        val infractoras = exams.flatMap { exam ->
            exam.questions
                .filterNot { it.lockOptionOrder }
                .filter { question ->
                    val explanation = question.explanation ?: return@filter false
                    val textos = listOf(explanation.summary) +
                        explanation.points.flatMap { listOfNotNull(it.term, it.text) }
                    textos.any(optionPositionReference::containsMatchIn)
                }
                .map { "${exam.id}·${it.id}" }
        }

        assertEquals(
            emptyList(),
            infractoras,
            "con el barajado, «la opción b)» en una explicación apunta a otra opción distinta",
        )
    }

    @Test
    fun `todo contexto declarado se usa y todo contexto usado existe`() {
        exams.forEach { exam ->
            val usados = exam.questions.mapNotNull { it.contextId }.toSet()
            val declarados = exam.contexts.keys

            assertEquals(emptySet(), usados - declarados, "${exam.id}: contextId sin declarar")
            assertEquals(emptySet(), declarados - usados, "${exam.id}: contexto declarado y no usado")
        }
    }
}
