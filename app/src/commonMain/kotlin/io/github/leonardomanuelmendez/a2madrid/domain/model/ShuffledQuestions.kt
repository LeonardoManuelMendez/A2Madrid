/*
 * ══ CAPA DE DOMINIO · Modelo ══
 * El resultado de barajar una sesión. Contiene las preguntas REORDENADAS (su contenido sigue
 * siendo el canónico de exams.json) y, aparte, la permutación de opciones de cada pregunta.
 *
 * Las opciones no se reordenan dentro de Question a propósito: el índice de la opción elegida se
 * PERSISTE en el historial, y el desglose de un intento se reconstruye cruzándolo con el JSON.
 * Si el dominio hablara en posiciones de pantalla, el repaso de fallos señalaría otra opción.
 * Por eso el barajado de opciones es una permutación aparte, que traduce la capa de presentación.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.model

/** One session's presentation order over a fixed set of questions. */
data class ShuffledQuestions(
    /** The questions in the order they will be asked. */
    val questions: List<Question>,
    /** questionId → displayed option position → canonical option index. */
    val optionOrder: Map<Int, List<Int>>,
)
