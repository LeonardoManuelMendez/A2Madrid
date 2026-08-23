/*
 * ══ CAPA DE PRESENTACIÓN · Modo de sesión ══
 * Las tres formas de recorrer las mismas preguntas. La diferencia no es de contenido sino de
 * REGLAS: qué se corrige, cuándo y con qué cuentas.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.quiz

/** How a quiz session behaves. */
enum class QuizMode {
    /** Test corriente: corrige y explica pregunta a pregunta, sin reloj. */
    PRACTICE,

    /** Solo las falladas en un intento anterior. Se comporta como PRACTICE. */
    REVIEW,

    /**
     * Simulacro: todos los modelos, con cronómetro, sin corrección hasta el final y con la
     * penalización de un tercio por error. Se puede dejar en blanco.
     */
    EXAM,
    ;

    /** True when the session corrects and explains as you go. */
    val showsFeedback: Boolean get() = this != EXAM
}
