/*
 * ══ CAPA DE DOMINIO · Modelo ══
 * La puntuación de un simulacro, con las reglas del examen real en vez de las del modo práctica.
 *
 * Las instrucciones del 1er ejercicio (Orden 2411/2017) dicen tres cosas: el acierto suma, «la
 * pregunta con contestación errónea se penalizará con la tercera parte del valor asignado a la
 * contestación correcta», y «la pregunta no contestada … no tendrá valoración».
 *
 * De ahí que dejar en blanco sea una decisión táctica y no una rendición: tres respuestas
 * lanzadas al azar que fallen borran un acierto entero.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.model

/** Score of a mock exam under the real marking rules. */
data class ExamScore(
    val correct: Int,
    val wrong: Int,
    val blank: Int,
) {
    val answered: Int get() = correct + wrong
    val totalQuestions: Int get() = correct + wrong + blank

    /** Aciertos menos un tercio por cada error. Los blancos no intervienen. */
    val net: Double get() = correct - wrong / PENALTY_DIVISOR

    companion object {
        /** «La tercera parte del valor asignado a la contestación correcta». */
        const val PENALTY_DIVISOR = 3.0

        /** Builds the score from the answers actually given; the rest of [totalQuestions] are blanks. */
        fun from(answers: List<AnswerResult>, totalQuestions: Int): ExamScore {
            val correct = answers.count { it.isCorrect }
            val wrong = answers.size - correct
            return ExamScore(
                correct = correct,
                wrong = wrong,
                blank = (totalQuestions - answers.size).coerceAtLeast(0),
            )
        }
    }
}
