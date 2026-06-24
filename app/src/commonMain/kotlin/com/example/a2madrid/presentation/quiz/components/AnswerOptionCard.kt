/*
 * ══ CAPA DE PRESENTACIÓN · Componente de UI reutilizable ══
 * Tarjeta de una opción: letra (A–D) + texto + estado visual (por defecto / seleccionada /
 * correcta / incorrecta / atenuada). Es UI pura: recibe datos y un onClick, sin estado ni
 * lógica propia. La usa QuizScreen para cada opción de la pregunta.
 */
package com.example.a2madrid.presentation.quiz.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a2madrid.presentation.theme.A2MadridTheme
import com.example.a2madrid.presentation.theme.LocalQuizColors

/** Visual state of a single answer option. */
enum class OptionState { Default, Selected, Correct, Incorrect, Dimmed }

/** A selectable answer option that reflects selection and post-confirmation feedback. */
@Composable
fun AnswerOptionCard(
    text: String,
    label: String,
    state: OptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val quiz = LocalQuizColors.current

    val container: Color
    val content: Color
    val border: Color
    val trailingIcon: ImageVector?
    when (state) {
        OptionState.Default -> {
            container = scheme.surface
            content = scheme.onSurface
            border = scheme.outlineVariant
            trailingIcon = null
        }
        OptionState.Selected -> {
            container = scheme.primaryContainer
            content = scheme.onPrimaryContainer
            border = scheme.primary
            trailingIcon = null
        }
        OptionState.Correct -> {
            container = quiz.correctContainer
            content = quiz.onCorrectContainer
            border = quiz.correct
            trailingIcon = Icons.Filled.Check
        }
        OptionState.Incorrect -> {
            container = scheme.errorContainer
            content = scheme.onErrorContainer
            border = scheme.error
            trailingIcon = Icons.Filled.Close
        }
        OptionState.Dimmed -> {
            container = scheme.surface
            content = scheme.onSurfaceVariant
            border = scheme.outlineVariant
            trailingIcon = null
        }
    }

    val animatedContainer by animateColorAsState(container, label = "optionContainer")
    val animatedBorder by animateColorAsState(border, label = "optionBorder")
    val selectedLike = state == OptionState.Selected || state == OptionState.Correct ||
        state == OptionState.Incorrect

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = animatedContainer,
        contentColor = content,
        border = BorderStroke(if (selectedLike) 2.dp else 1.dp, animatedBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            )
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Preview(name = "Estados de opción", showBackground = true)
@Composable
private fun AnswerOptionCardPreview() {
    A2MadridTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AnswerOptionCard("Opción por defecto", "A", OptionState.Default, enabled = true, onClick = {})
            AnswerOptionCard("Opción seleccionada", "B", OptionState.Selected, enabled = true, onClick = {})
            AnswerOptionCard("Respuesta correcta", "C", OptionState.Correct, enabled = false, onClick = {})
            AnswerOptionCard("Respuesta incorrecta", "D", OptionState.Incorrect, enabled = false, onClick = {})
            AnswerOptionCard("Opción atenuada", "E", OptionState.Dimmed, enabled = false, onClick = {})
        }
    }
}