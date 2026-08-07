/*
 * ══ CAPA DE PRESENTACIÓN · Componente de UI reutilizable ══
 * Tarjeta de corrección: veredicto (correcto/incorrecto), resumen del porqué y un DESPLEGABLE
 * con el desarrollo (palabras con error, pasos del cálculo, artículo de la ley citado).
 * Es UI pura: recibe el estado del desplegable y un callback, sin estado propio ni lógica.
 * La usa QuizScreen cuando el usuario confirma la respuesta.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.quiz.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.leonardomanuelmendez.a2madrid.domain.model.Explanation
import io.github.leonardomanuelmendez.a2madrid.domain.model.ExplanationPoint
import io.github.leonardomanuelmendez.a2madrid.domain.model.ExplanationSource
import io.github.leonardomanuelmendez.a2madrid.presentation.theme.A2MadridTheme
import io.github.leonardomanuelmendez.a2madrid.presentation.theme.LocalQuizColors
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Feedback shown after confirming an answer: the verdict, the short reason and — behind a
 * disclosure — the detail needed to actually learn from it.
 *
 * @param expanded whether the detail section is unfolded; owned by the caller so it can be
 *   reset when moving to the next question.
 */
@Composable
fun ExplanationCard(
    isCorrect: Boolean,
    explanation: Explanation?,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val quiz = LocalQuizColors.current
    val accent = if (isCorrect) quiz.correct else scheme.error

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCorrect) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(end = 6.dp).size(20.dp),
                )
                Text(
                    text = if (isCorrect) "¡Correcto!" else "Incorrecto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
            }

            if (explanation != null) {
                Text(
                    text = explanation.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }

            if (explanation != null && explanation.hasDetail) {
                ExplanationDisclosure(
                    explanation = explanation,
                    expanded = expanded,
                    onToggleExpanded = onToggleExpanded,
                )
            }
        }
    }
}

/** The "Ver explicación" toggle plus the detail it unfolds. */
@Composable
private fun ExplanationDisclosure(
    explanation: Explanation,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "explanationArrow",
    )

    TextButton(
        onClick = onToggleExpanded,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.padding(top = 4.dp),
    ) {
        Text(
            text = if (expanded) "Ocultar explicación" else "Ver explicación",
            style = MaterialTheme.typography.labelLarge,
        )
        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = null,
            modifier = Modifier.padding(start = 4.dp).size(20.dp).rotate(arrowRotation),
        )
    }

    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            explanation.points.forEach { point -> ExplanationPointRow(point) }
            explanation.source?.let { source ->
                SourceBlock(source, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

/** A bulleted item of the breakdown, with its term in bold when there is one. */
@Composable
private fun ExplanationPointRow(point: ExplanationPoint) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = buildAnnotatedString {
                point.term?.let { term ->
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(term) }
                    append(" — ")
                }
                append(point.text)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The cited rule, set apart on its own surface so it reads as a quotation. */
@Composable
private fun SourceBlock(source: ExplanationSource, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = source.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            source.quote?.let { quote ->
                Text(
                    text = quote,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ---- Previews ----

private val previewExplanation = Explanation(
    summary = "Hay 3 palabras mal escritas: esaltamiento, urogayo y bacuo.",
    points = listOf(
        ExplanationPoint(term = "esaltamiento", text = "se escribe exaltamiento, con x."),
        ExplanationPoint(term = "urogayo", text = "se escribe urogallo, con ll."),
        ExplanationPoint(term = "bacuo", text = "se escribe vacuo, con v."),
    ),
    source = ExplanationSource(
        label = "Ortografía · RAE",
        quote = "Se escriben con x las palabras que empiezan por el prefijo ex- ante vocal.",
    ),
)

@Preview(name = "Explicación plegada", showBackground = true)
@Composable
private fun ExplanationCardCollapsedPreview() {
    A2MadridTheme {
        ExplanationCard(
            isCorrect = true,
            explanation = previewExplanation,
            expanded = false,
            onToggleExpanded = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Explicación desplegada", showBackground = true)
@Composable
private fun ExplanationCardExpandedPreview() {
    A2MadridTheme {
        ExplanationCard(
            isCorrect = false,
            explanation = previewExplanation,
            expanded = true,
            onToggleExpanded = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
