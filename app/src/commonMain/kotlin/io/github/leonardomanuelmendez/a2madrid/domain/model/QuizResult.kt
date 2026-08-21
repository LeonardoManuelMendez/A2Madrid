/*
 * ══ CAPA DE DOMINIO · Modelo ══
 * Resumen final del test (aciertos, %, aprobado, récord). Lo arma el
 * QuizViewModel al terminar y se transporta a la pantalla de resultado.
 *
 * `attemptMillis` identifica el intento dentro del historial. Es lo que viaja por la ruta para
 * que la pantalla de resultado pueda recuperar el desglose: la lista de respuestas no cabe en
 * un argumento de navegación, pero un Long sí.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.model

/** Aggregated result shown when the quiz is finished. */
data class QuizResult(
    val correctAnswers: Int,
    val totalQuestions: Int,
    val isNewBestScore: Boolean,
    /** Timestamp of the stored attempt, used to look its detail up again. */
    val attemptMillis: Long,
) {
    val percentage: Int
        get() = if (totalQuestions == 0) 0 else (correctAnswers * 100) / totalQuestions

    val hasPassed: Boolean get() = percentage >= PASS_THRESHOLD_PERCENT

    companion object {
        const val PASS_THRESHOLD_PERCENT = 60
    }
}
