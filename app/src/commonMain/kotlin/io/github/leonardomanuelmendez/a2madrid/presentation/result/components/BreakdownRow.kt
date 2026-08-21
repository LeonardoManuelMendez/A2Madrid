/*
 * ══ CAPA DE PRESENTACIÓN · Componente de UI reutilizable ══
 * Una fila del desglose del intento: número de pregunta, veredicto y —al desplegar— qué
 * contestaste, cuál era la correcta y la explicación completa (reutiliza ExplanationCard).
 * Es UI pura: recibe un AnswerResult y el estado del desplegable, sin lógica propia.
 * La usa ResultScreen.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.result.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.leonardomanuelmendez.a2madrid.domain.model.AnswerResult
import io.github.leonardomanuelmendez.a2madrid.domain.model.Explanation
import io.github.leonardomanuelmendez.a2madrid.domain.model.ExplanationPoint
import io.github.leonardomanuelmendez.a2madrid.domain.model.ExplanationSource
import io.github.leonardomanuelmendez.a2madrid.domain.model.Question
import io.github.leonardomanuelmendez.a2madrid.presentation.quiz.components.ExplanationCard
import io.github.leonardomanuelmendez.a2madrid.presentation.theme.A2MadridTheme
import io.github.leonardomanuelmendez.a2madrid.presentation.theme.LocalQuizColors
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * One question of the attempt breakdown, collapsed to a verdict line and unfolding into the
 * full comparison plus its explanation.
 *
 * @param position 1-based number shown on the left, so it matches how the question was asked.
 * @param expanded whether the detail is unfolded; owned by the caller.
 */
@Composable
fun BreakdownRow(
    position: Int,
    answer: AnswerResult,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val accent = if (answer.isCorrect) LocalQuizColors.current.correct else scheme.error
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "breakdownArrow",
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onToggleExpanded)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$position",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.width(28.dp),
                )
                Icon(
                    imageVector = if (answer.isCorrect) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = if (answer.isCorrect) "Acertada" else "Fallada",
                    tint = accent,
                    modifier = Modifier.padding(end = 8.dp).size(18.dp),
                )
                Text(
                    text = answer.question.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp).size(20.dp).rotate(arrowRotation),
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // En un acierto, "tu respuesta" y "la correcta" son la misma: repetirlas
                    // sería ruido, así que solo se contrastan cuando difieren.
                    if (!answer.isCorrect) {
                        LabelledOption(
                            label = "Tu respuesta",
                            option = answer.question.options[answer.selectedOptionIndex],
                            color = scheme.error,
                        )
                    }
                    LabelledOption(
                        label = if (answer.isCorrect) "Tu respuesta (correcta)" else "Correcta",
                        option = answer.question.correctAnswer,
                        color = LocalQuizColors.current.correct,
                    )
                    ExplanationCard(
                        isCorrect = answer.isCorrect,
                        explanation = answer.question.explanation,
                        expanded = true,
                        onToggleExpanded = {},
                    )
                }
            }
        }
    }
}

/** «Etiqueta — texto de la opción», con la etiqueta en el color del veredicto. */
@Composable
private fun LabelledOption(
    label: String,
    option: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = color)) {
                append("$label: ")
            }
            append(option)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ---- Previews ----

private val previewQuestion = Question(
    id = 79,
    text = "Son principios de actuación de la Comunidad de Madrid en materia de Hacienda Pública:",
    options = listOf(
        "Legalidad, eficacia y jerarquía.",
        "Legalidad, eficacia, control, universalidad, solidaridad intrarregional y coordinación.",
        "Únicamente legalidad y control.",
    ),
    correctAnswerIndex = 1,
    explanation = Explanation(
        summary = "Los enumera la Ley reguladora de la Hacienda de la Comunidad de Madrid.",
        points = listOf(ExplanationPoint(term = "Universalidad", text = "todos los ingresos y gastos.")),
        source = ExplanationSource(label = "Ley 9/1990, de Hacienda de la Comunidad de Madrid"),
    ),
)

@Preview(name = "Fallada · desplegada", showBackground = true)
@Composable
private fun BreakdownRowWrongPreview() {
    A2MadridTheme {
        BreakdownRow(
            position = 12,
            answer = AnswerResult(previewQuestion, selectedOptionIndex = 0, isCorrect = false),
            expanded = true,
            onToggleExpanded = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Acertada · plegada", showBackground = true)
@Composable
private fun BreakdownRowCorrectPreview() {
    A2MadridTheme {
        BreakdownRow(
            position = 13,
            answer = AnswerResult(previewQuestion, selectedOptionIndex = 1, isCorrect = true),
            expanded = false,
            onToggleExpanded = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
