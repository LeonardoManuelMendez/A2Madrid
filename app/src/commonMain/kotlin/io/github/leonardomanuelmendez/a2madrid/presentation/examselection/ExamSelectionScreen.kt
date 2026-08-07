/*
 * ══ CAPA DE PRESENTACIÓN · View (Composable) ══
 * Pantalla inicial. Observa ExamSelectionViewModel y dibuja la lista de modelos.
 * Patrón "state down, events up": recibe estado y emite eventos (onExamSelected, onViewScores);
 * NO contiene lógica de negocio ni navega por sí misma (de eso se encarga el host).
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.examselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.leonardomanuelmendez.a2madrid.domain.model.Exam
import io.github.leonardomanuelmendez.a2madrid.presentation.ContentMaxWidth
import io.github.leonardomanuelmendez.a2madrid.presentation.theme.A2MadridTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExamSelectionScreen(
    oppositionId: String,
    onExamSelected: (String) -> Unit,
    onViewScores: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExamSelectionViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ExamSelectionContent(
        oppositionId = oppositionId,
        uiState = uiState,
        onExamSelected = onExamSelected,
        onViewScores = onViewScores,
        onBack = onBack,
        onRetry = viewModel::load,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamSelectionContent(
    oppositionId: String,
    uiState: ExamSelectionUiState,
    onExamSelected: (String) -> Unit,
    onViewScores: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (oppositionId) {
        "c1_admin" -> "Administración General C1"
        "c2_auxiliar" -> "Administración General C2"
        "a1_tecnico" -> "Técnicos Superiores A1"
        "a2_gestion" -> "Técnicos Gestión A2"
        else -> "A2Madrid"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = { ScoresMenu(onViewScores = onViewScores) },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(innerPadding)) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            uiState.errorMessage != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = onRetry) { Text("Reintentar") }
            }

            else -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = ContentMaxWidth)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Elige un modelo de examen",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    uiState.exams.forEach { exam ->
                        ExamCard(exam = exam, onClick = { onExamSelected(exam.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamCard(exam: Exam, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = exam.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${exam.questionCount} preguntas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ScoresMenu(onViewScores: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.Menu, contentDescription = "Menú")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text("Puntuaciones") },
            onClick = {
                expanded = false
                onViewScores()
            },
        )
    }
}

@Preview(name = "Selección de examen", showBackground = true)
@Composable
private fun ExamSelectionContentPreview() {
    A2MadridTheme {
        ExamSelectionContent(
            oppositionId = "c2_auxiliar",
            uiState = ExamSelectionUiState(
                isLoading = false,
                exams = listOf(
                    Exam("a", "Modelo A · Madrid esencial", emptyList()),
                    Exam("b", "Modelo B · Historia y símbolos", emptyList()),
                    Exam("c", "Modelo C · Deporte y ocio", emptyList()),
                ),
            ),
            onExamSelected = {},
            onViewScores = {},
            onBack = {},
            onRetry = {},
        )
    }
}