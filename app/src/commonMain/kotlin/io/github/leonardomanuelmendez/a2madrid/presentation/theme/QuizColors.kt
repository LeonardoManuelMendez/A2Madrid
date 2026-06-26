/*
 * ══ CAPA DE PRESENTACIÓN · Tema ══
 * Colores semánticos que Material 3 no incluye (los del estado "correcto"). Se exponen por
 * CompositionLocal (LocalQuizColors), lo provee A2MadridTheme y los consume AnswerOptionCard.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extra semantic colors not covered by the Material 3 [androidx.compose.material3.ColorScheme],
 * used to signal a *correct* answer. (Incorrect answers reuse the standard `error` role.)
 */
@Immutable
data class QuizColors(
    val correct: Color,
    val onCorrect: Color,
    val correctContainer: Color,
    val onCorrectContainer: Color,
)

val LightQuizColors = QuizColors(
    correct = Color(0xFF2E7D32),
    onCorrect = Color(0xFFFFFFFF),
    correctContainer = Color(0xFFB7F0B9),
    onCorrectContainer = Color(0xFF00210B),
)

val DarkQuizColors = QuizColors(
    correct = Color(0xFF7FDA82),
    onCorrect = Color(0xFF00390F),
    correctContainer = Color(0xFF1B5E20),
    onCorrectContainer = Color(0xFFB7F0B9),
)

/** Access via `LocalQuizColors.current` from any composable inside [A2MadridTheme]. */
val LocalQuizColors = staticCompositionLocalOf { LightQuizColors }