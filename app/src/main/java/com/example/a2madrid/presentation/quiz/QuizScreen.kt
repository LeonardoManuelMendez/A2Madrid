/*
 * ══ CAPA DE PRESENTACIÓN · View (Composable) ══
 * Pinta el QuizUiState y reenvía los gestos del usuario al QuizViewModel
 * (patrón "state down, events up"). collectAsStateWithLifecycle() la suscribe al estado y
 * se recompone sola cuando cambia. Las funciones privadas (QuizContent, ContextBlock…) son
 * UI pura y previsualizable con @Preview. No accede a datos ni casos de uso directamente.
 */
package com.example.a2madrid.presentation.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a2madrid.domain.model.Question
import com.example.a2madrid.domain.model.QuizResult
import com.example.a2madrid.presentation.quiz.components.AnswerOptionCard
import com.example.a2madrid.presentation.quiz.components.OptionState
import com.example.a2madrid.ui.theme.A2MadridTheme

@Composable
fun QuizScreen(
    examId: String,
    viewModel: QuizViewModel,
    onQuizFinished: (QuizResult, String, String) -> Unit,
    onViewScores: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(examId) {
        viewModel.loadExam(examId)
    }

    // Fire navigation exactly once when the quiz completes.
    androidx.compose.runtime.LaunchedEffect(uiState.result) {
        uiState.result?.let { result ->
            val finishedExamId = uiState.examId ?: return@let
            onQuizFinished(result, finishedExamId, uiState.examTitle)
        }
    }

    QuizContent(
        uiState = uiState,
        onSelectOption = viewModel::selectOption,
        onConfirm = viewModel::confirmAnswer,
        onNext = viewModel::nextQuestion,
        onRestart = viewModel::restart,
        onViewScores = onViewScores,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizContent(
    uiState: QuizUiState,
    onSelectOption: (Int) -> Unit,
    onConfirm: () -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
    onViewScores: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRestartDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(uiState.examTitle.ifBlank { "A2Madrid" }) },
                    actions = {
                        QuizOptionsMenu(
                            onViewScores = onViewScores,
                            onRestart = {
                                if (uiState.hasProgress) showRestartDialog = true else onRestart()
                            },
                        )
                    },
                )
                if (!uiState.isLoading && uiState.errorMessage == null && uiState.questions.isNotEmpty()) {
                    LinearProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        bottomBar = {
            val question = uiState.currentQuestion
            if (question != null) {
                QuizBottomBar(
                    isAnswerConfirmed = uiState.isAnswerConfirmed,
                    isLastQuestion = uiState.isLastQuestion,
                    canConfirm = uiState.canConfirm,
                    onConfirm = onConfirm,
                    onNext = onNext,
                )
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingState(Modifier.fillMaxSize().padding(innerPadding))
            uiState.errorMessage != null -> ErrorState(
                message = uiState.errorMessage,
                onRetry = onRestart,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
            uiState.currentQuestion != null -> QuestionState(
                uiState = uiState,
                question = uiState.currentQuestion!!,
                onSelectOption = onSelectOption,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Reiniciar prueba") },
            text = { Text("Perderás el progreso de esta prueba. ¿Quieres empezar de nuevo?") },
            confirmButton = {
                TextButton(onClick = {
                    showRestartDialog = false
                    onRestart()
                }) { Text("Reiniciar") }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun QuestionState(
    uiState: QuizUiState,
    question: Question,
    onSelectOption: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Pregunta ${uiState.questionNumber} de ${uiState.totalQuestions}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = question.text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        question.context?.let { context ->
            ContextBlock(context)
        }

        question.options.forEachIndexed { index, option ->
            AnswerOptionCard(
                text = option,
                label = ('A' + index).toString(),
                state = optionStateFor(uiState, index),
                enabled = !uiState.isAnswerConfirmed,
                onClick = { onSelectOption(index) },
            )
        }

        if (uiState.isAnswerConfirmed) {
            FeedbackCard(
                isCorrect = uiState.selectedOptionIndex == question.correctAnswerIndex,
                explanation = question.explanation,
            )
        }
    }
}

@Composable
private fun ContextBlock(context: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = context,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            softWrap = false,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
        )
    }
}

private fun optionStateFor(uiState: QuizUiState, index: Int): OptionState {
    val confirmed = uiState.isAnswerConfirmed
    val correctIndex = uiState.currentQuestion?.correctAnswerIndex
    return when {
        !confirmed && uiState.selectedOptionIndex == index -> OptionState.Selected
        !confirmed -> OptionState.Default
        index == correctIndex -> OptionState.Correct
        index == uiState.selectedOptionIndex -> OptionState.Incorrect
        else -> OptionState.Dimmed
    }
}

@Composable
private fun FeedbackCard(isCorrect: Boolean, explanation: String?) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (isCorrect) "¡Correcto!" else "Incorrecto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
            if (!explanation.isNullOrBlank()) {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun QuizBottomBar(
    isAnswerConfirmed: Boolean,
    isLastQuestion: Boolean,
    canConfirm: Boolean,
    onConfirm: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            if (isAnswerConfirmed) {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (isLastQuestion) "Ver resultado" else "Siguiente")
                }
            } else {
                Button(
                    onClick = onConfirm,
                    enabled = canConfirm,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Confirmar")
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        OutlinedButton(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

// ---- Previews ----

@Composable
private fun QuizOptionsMenu(
    onViewScores: () -> Unit,
    onRestart: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text("Puntuaciones") },
            onClick = {
                expanded = false
                onViewScores()
            },
        )
        DropdownMenuItem(
            text = { Text("Reiniciar prueba") },
            onClick = {
                expanded = false
                onRestart()
            },
        )
    }
}

// ---- Previews ----

private val previewQuestions = listOf(
    Question(
        id = 1,
        text = "¿Cuál es la capital de España?",
        options = listOf("Barcelona", "Madrid", "Valencia", "Sevilla"),
        correctAnswerIndex = 1,
        explanation = "Madrid es la capital de España desde 1561.",
    ),
)

@Preview(name = "Pregunta sin responder", showBackground = true)
@Composable
private fun QuizContentQuestionPreview() {
    A2MadridTheme {
        QuizContent(
            uiState = QuizUiState(
                isLoading = false,
                questions = previewQuestions,
                selectedOptionIndex = 0,
            ),
            onSelectOption = {},
            onConfirm = {},
            onNext = {},
            onRestart = {},
            onViewScores = {},
        )
    }
}

@Preview(name = "Respuesta confirmada", showBackground = true)
@Composable
private fun QuizContentAnsweredPreview() {
    A2MadridTheme {
        QuizContent(
            uiState = QuizUiState(
                isLoading = false,
                questions = previewQuestions,
                selectedOptionIndex = 0,
                isAnswerConfirmed = true,
            ),
            onSelectOption = {},
            onConfirm = {},
            onNext = {},
            onRestart = {},
            onViewScores = {},
        )
    }
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun QuizContentErrorPreview() {
    A2MadridTheme {
        QuizContent(
            uiState = QuizUiState(isLoading = false, errorMessage = "No se pudieron cargar las preguntas"),
            onSelectOption = {},
            onConfirm = {},
            onNext = {},
            onRestart = {},
            onViewScores = {},
        )
    }
}
