/*
 * ══ CAPA DE PRESENTACIÓN · View (Composable) ══
 * Muestra el resumen final: los datos del intento llegan por la ruta (ResultRoute), la mejor
 * marca del modelo y el DESGLOSE pregunta a pregunta los aporta el ResultViewModel. Emite
 * onRestart / onViewScores hacia el host de navegación.
 * ResultContent es stateless → previsualizable con @Preview.
 *
 * Es un LazyColumn y no un Column con scroll porque el desglose de un modelo completo son 45
 * filas, cada una con su explicación desplegable.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.leonardomanuelmendez.a2madrid.domain.model.AnswerResult
import io.github.leonardomanuelmendez.a2madrid.domain.model.ExamScore
import io.github.leonardomanuelmendez.a2madrid.domain.model.QuizResult
import io.github.leonardomanuelmendez.a2madrid.presentation.ContentMaxWidth
import io.github.leonardomanuelmendez.a2madrid.presentation.result.components.BreakdownRow
import io.github.leonardomanuelmendez.a2madrid.presentation.theme.A2MadridTheme
import kotlin.math.abs
import kotlin.math.roundToInt
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ResultScreen(
    examId: String,
    examTitle: String,
    correctAnswers: Int,
    totalQuestions: Int,
    isNewBestScore: Boolean,
    attemptMillis: Long,
    isReview: Boolean,
    isExam: Boolean,
    onRestart: () -> Unit,
    onReviewWrong: () -> Unit,
    onViewScores: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultViewModel = koinViewModel(),
) {
    val history by viewModel.scoreHistory.collectAsState()
    val breakdown by viewModel.breakdown.collectAsState()
    val bestForExam = history.filter { it.examId == examId && !it.isReview }
        .maxOfOrNull { it.correctAnswers } ?: correctAnswers

    LaunchedEffect(examId, attemptMillis) {
        viewModel.loadBreakdown(examId, attemptMillis)
    }

    ResultContent(
        examTitle = examTitle,
        result = QuizResult(correctAnswers, totalQuestions, isNewBestScore, attemptMillis),
        bestScore = bestForExam,
        isReview = isReview,
        examScore = if (isExam && breakdown.isNotEmpty()) {
            ExamScore.from(breakdown, totalQuestions)
        } else {
            null
        },
        breakdown = breakdown,
        onRestart = onRestart,
        onReviewWrong = onReviewWrong,
        onViewScores = onViewScores,
        modifier = modifier,
    )
}

@Composable
private fun ResultContent(
    examTitle: String,
    result: QuizResult,
    bestScore: Int,
    isReview: Boolean,
    /** Presente solo en un simulacro: obliga a puntuar con las reglas del examen. */
    examScore: ExamScore?,
    breakdown: List<AnswerResult>,
    onRestart: () -> Unit,
    onReviewWrong: () -> Unit,
    onViewScores: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wrongCount = breakdown.count { !it.isCorrect }
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Summary(
                    examTitle = examTitle,
                    result = result,
                    bestScore = bestScore,
                    isReview = isReview,
                    examScore = examScore,
                    modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth(),
                )
            }

            item {
                Column(
                    modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // La acción con más rendimiento de estudio va primero y en botón sólido.
                    if (wrongCount > 0) {
                        Button(onClick = onReviewWrong, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                if (wrongCount == 1) "Repasar el fallo"
                                else "Repasar los $wrongCount fallos",
                            )
                        }
                        OutlinedButton(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                            Text("Volver a empezar")
                        }
                    } else {
                        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                            Text("Volver a empezar")
                        }
                    }
                    OutlinedButton(onClick = onViewScores, modifier = Modifier.fillMaxWidth()) {
                        Text("Ver puntuaciones")
                    }
                }
            }

            if (breakdown.isNotEmpty()) {
                item {
                    BreakdownHeader(
                        wrongCount = wrongCount,
                        modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth(),
                    )
                }
                itemsIndexed(breakdown, key = { _, answer -> answer.question.id }) { index, answer ->
                    var expanded by rememberSaveable(answer.question.id) { mutableStateOf(false) }
                    BreakdownRow(
                        position = index + 1,
                        answer = answer,
                        expanded = expanded,
                        onToggleExpanded = { expanded = !expanded },
                        modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun Summary(
    examTitle: String,
    result: QuizResult,
    bestScore: Int,
    isReview: Boolean,
    examScore: ExamScore?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (examTitle.isNotBlank()) {
            Text(
                text = when {
                    isReview -> "Repaso de fallos · $examTitle"
                    examScore != null -> "Simulacro · $examTitle"
                    else -> examTitle
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = when {
                isReview -> "Repaso terminado"
                examScore != null -> "Simulacro terminado"
                result.hasPassed -> "¡Prueba superada!"
                else -> "Prueba finalizada"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        if (examScore != null) ExamScoreBadge(examScore) else ScoreBadge(result = result)

        Text(
            text = when {
                isReview && result.correctAnswers == result.totalQuestions ->
                    "Has acertado todas las que antes fallaste."
                isReview -> "Aún quedan preguntas por afianzar. Puedes repasarlas otra vez."
                // No se anuncia aprobado ni suspenso: la nota de corte no está en las
                // instrucciones del examen, así que inventarla sería engañar.
                examScore != null -> "Cada error resta un tercio de acierto; los blancos no puntúan."
                result.hasPassed -> "¡Buen trabajo! Has demostrado un buen conocimiento sobre Madrid."
                else -> "Sigue practicando para mejorar tu resultado."
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (result.isNewBestScore) {
            NewRecordBadge()
        }

        if (!isReview && examScore == null) {
            Text(
                text = "Mejor marca en este examen: $bestScore de ${result.totalQuestions}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Cabecera de la sección de desglose: separa el marcador del repaso pregunta a pregunta. */
@Composable
private fun BreakdownHeader(wrongCount: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = "Repaso pregunta a pregunta",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = when (wrongCount) {
                0 -> "No has fallado ninguna. Toca cualquier pregunta para releer el porqué."
                1 -> "Has fallado 1 pregunta. Tócala para ver por qué."
                else -> "Has fallado $wrongCount preguntas. Tócalas para ver por qué."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Marcador del simulacro: la nota neta manda, y debajo de dónde sale. */
@Composable
private fun ExamScoreBadge(score: ExamScore) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = formatNet(score.net),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "puntos netos sobre ${score.totalQuestions}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = listOf(
                    plural(score.correct, "acierto", "aciertos"),
                    plural(score.wrong, "error", "errores"),
                    "${score.blank} en blanco",
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

/** «1 error» y no «1 errores»: el marcador se lee muchas veces y el detalle canta. */
private fun plural(count: Int, singular: String, plural: String): String =
    "$count ${if (count == 1) singular else plural}"

/** Dos decimales con coma, sin depender de formateo de plataforma. */
private fun formatNet(value: Double): String {
    val rounded = (value * 100).roundToInt()
    val signo = if (rounded < 0) "-" else ""
    val absoluto = abs(rounded)
    return "$signo${absoluto / 100},${(absoluto % 100).toString().padStart(2, '0')}"
}

@Composable
private fun ScoreBadge(result: QuizResult) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${result.correctAnswers}/${result.totalQuestions}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${result.percentage}% de aciertos",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun NewRecordBadge() {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "¡Nuevo récord!",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ---- Previews ----

@Preview(name = "Aprobado + récord", showBackground = true)
@Composable
private fun ResultPassedPreview() {
    A2MadridTheme {
        ResultContent(
            examTitle = "Modelo A · Madrid esencial",
            result = QuizResult(
                correctAnswers = 8,
                totalQuestions = 10,
                isNewBestScore = true,
                attemptMillis = 0L,
            ),
            bestScore = 8,
            isReview = false,
            examScore = null,
            breakdown = emptyList(),
            onRestart = {},
            onReviewWrong = {},
            onViewScores = {},
        )
    }
}

@Preview(name = "Suspendido", showBackground = true)
@Composable
private fun ResultFailedPreview() {
    A2MadridTheme {
        ResultContent(
            examTitle = "Modelo B · Historia y símbolos",
            result = QuizResult(
                correctAnswers = 3,
                totalQuestions = 10,
                isNewBestScore = false,
                attemptMillis = 0L,
            ),
            bestScore = 5,
            isReview = false,
            examScore = null,
            breakdown = emptyList(),
            onRestart = {},
            onReviewWrong = {},
            onViewScores = {},
        )
    }
}
