/*
 * ══ CAPA DE PRESENTACIÓN · Navegación (Compose Navigation · multiplataforma) ══
 * Destinos tipados y @Serializable para la navegación type-safe de Compose (Android/iOS/Web).
 * ResultRoute viaja con sus datos. Quién navega a dónde lo decide A2MadridNavHost.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object OppositionSelectionRoute

@Serializable
data class ExamSelectionRoute(val oppositionId: String)

@Serializable
data class QuizRoute(val examId: String)

@Serializable
data class ResultRoute(
    val examId: String,
    val examTitle: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val isNewBestScore: Boolean,
    /** Identifica el intento en el historial para recuperar su desglose. */
    val attemptMillis: Long,
    /** Un repaso cubre un subconjunto: su marcador no es comparable con el del modelo. */
    val isReview: Boolean = false,
)

/** Repaso de los fallos de un intento concreto: mismo test, solo las preguntas falladas. */
@Serializable
data class ReviewRoute(
    val examId: String,
    val examTitle: String,
    val attemptMillis: Long,
)

@Serializable
data object ScoreHistoryRoute
