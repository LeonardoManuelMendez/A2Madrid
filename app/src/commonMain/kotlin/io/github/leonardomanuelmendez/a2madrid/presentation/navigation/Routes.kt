/*
 * ══ CAPA DE PRESENTACIÓN · Navegación (Compose Navigation · multiplataforma) ══
 * Destinos tipados y @Serializable para la navegación type-safe de Compose (Android/iOS/Web).
 * ResultRoute viaja con sus datos. Quién navega a dónde lo decide A2MadridNavHost.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object ExamSelectionRoute

@Serializable
data class QuizRoute(val examId: String)

@Serializable
data class ResultRoute(
    val examId: String,
    val examTitle: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val isNewBestScore: Boolean,
)

@Serializable
data object ScoreHistoryRoute
