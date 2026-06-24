/*
 * ══ CAPA DE DOMINIO · Modelo ══
 * Resumen final del test (aciertos, %, aprobado, récord). Lo arma el
 * QuizViewModel al terminar y se transporta a la pantalla de resultado.
 */
package com.example.a2madrid.domain.model

/** Aggregated result shown when the quiz is finished. */
data class QuizResult(
    val correctAnswers: Int,
    val totalQuestions: Int,
    val isNewBestScore: Boolean,
) {
    val percentage: Int
        get() = if (totalQuestions == 0) 0 else (correctAnswers * 100) / totalQuestions

    val hasPassed: Boolean get() = percentage >= PASS_THRESHOLD_PERCENT

    companion object {
        const val PASS_THRESHOLD_PERCENT = 60
    }
}