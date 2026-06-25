/*
 * ══ CAPA DE PRESENTACIÓN · View (Composable) ══
 * Muestra el resumen final: los datos del intento llegan por la ruta (ResultRoute) y la mejor
 * marca del modelo del ResultViewModel. Emite onRestart / onViewScores hacia el host de
 * navegación. ResultContent es stateless → previsualizable con @Preview.
 */
package com.example.a2madrid.presentation.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.collectAsState
import com.example.a2madrid.domain.model.QuizResult
import com.example.a2madrid.presentation.ContentMaxWidth
import com.example.a2madrid.presentation.theme.A2MadridTheme

@Composable
fun ResultScreen(
    examId: String,
    examTitle: String,
    correctAnswers: Int,
    totalQuestions: Int,
    isNewBestScore: Boolean,
    onRestart: () -> Unit,
    onViewScores: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultViewModel = koinViewModel(),
) {
    val history by viewModel.scoreHistory.collectAsState()
    val bestForExam = history.filter { it.examId == examId }
        .maxOfOrNull { it.correctAnswers } ?: correctAnswers

    ResultContent(
        examTitle = examTitle,
        result = QuizResult(correctAnswers, totalQuestions, isNewBestScore),
        bestScore = bestForExam,
        onRestart = onRestart,
        onViewScores = onViewScores,
        modifier = modifier,
    )
}

@Composable
private fun ResultContent(
    examTitle: String,
    result: QuizResult,
    bestScore: Int,
    onRestart: () -> Unit,
    onViewScores: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
        Column(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            if (examTitle.isNotBlank()) {
                Text(
                    text = examTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }

            Text(
                text = if (result.hasPassed) "¡Prueba superada!" else "Prueba finalizada",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            ScoreBadge(result = result)

            Text(
                text = if (result.hasPassed) {
                    "¡Buen trabajo! Has demostrado un buen conocimiento sobre Madrid."
                } else {
                    "Sigue practicando para mejorar tu resultado."
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (result.isNewBestScore) {
                NewRecordBadge()
            }

            Text(
                text = "Mejor marca en este examen: $bestScore de ${result.totalQuestions}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Volver a empezar")
            }

            OutlinedButton(
                onClick = onViewScores,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Ver puntuaciones")
            }
        }
        }
    }
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
            result = QuizResult(correctAnswers = 8, totalQuestions = 10, isNewBestScore = true),
            bestScore = 8,
            onRestart = {},
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
            result = QuizResult(correctAnswers = 3, totalQuestions = 10, isNewBestScore = false),
            bestScore = 5,
            onRestart = {},
            onViewScores = {},
        )
    }
}