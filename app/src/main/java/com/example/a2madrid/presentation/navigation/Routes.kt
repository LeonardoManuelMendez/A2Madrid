/*
 * ══ CAPA DE PRESENTACIÓN · Navegación (Navigation 3) ══
 * Define los destinos como claves tipadas (NavKey) y @Serializable, para que el back stack
 * sobreviva a cambios de configuración / muerte del proceso. ResultRoute viaja con sus datos.
 * Quién navega a dónde lo decide A2MadridNavDisplay.
 */
package com.example.a2madrid.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Type-safe Navigation 3 destinations. Each key is serializable so the back stack survives
 *  process death. */

@Serializable
data object ExamSelectionRoute : NavKey

@Serializable
data class QuizRoute(
    val examId: String,
) : NavKey

@Serializable
data class ResultRoute(
    val examId: String,
    val examTitle: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val isNewBestScore: Boolean,
) : NavKey

@Serializable
data object ScoreHistoryRoute : NavKey
