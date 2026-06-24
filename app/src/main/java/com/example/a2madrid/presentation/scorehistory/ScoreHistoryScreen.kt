/*
 * ══ CAPA DE PRESENTACIÓN · View (Composable) ══
 * Lista las puntuaciones (estado del ScoreHistoryViewModel), destaca la mejor de cada modelo
 * y permite borrar una o todas con diálogo de confirmación. Incluye estado vacío.
 * ScoreHistoryContent es stateless → previsualizable; emite onBack/onDelete/onClearAll.
 */
package com.example.a2madrid.presentation.scorehistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a2madrid.domain.model.ScoreEntry
import com.example.a2madrid.presentation.theme.A2MadridTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScoreHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScoreHistoryViewModel = hiltViewModel(),
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    ScoreHistoryContent(
        entries = history,
        onBack = onBack,
        onDeleteEntry = viewModel::deleteScore,
        onClearAll = viewModel::clearScores,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoreHistoryContent(
    entries: List<ScoreEntry>,
    onBack: () -> Unit,
    onDeleteEntry: (ScoreEntry) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var pendingDeleteEntry by remember { mutableStateOf<ScoreEntry?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Puntuaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (entries.isNotEmpty()) {
                        HistoryOptionsMenu(
                            onClearAll = { showClearDialog = true },
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (entries.isEmpty()) {
            EmptyState(Modifier.fillMaxSize().padding(innerPadding))
        } else {
            val bestCorrectByExam = remember(entries) {
                entries.groupBy(ScoreEntry::examId)
                    .mapValues { (_, examEntries) -> examEntries.maxOf { it.correctAnswers } }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    bottom = innerPadding.calculateBottomPadding() + 20.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = entries,
                    key = { entry -> "${entry.examId}-${entry.timestampMillis}-${entry.correctAnswers}" },
                ) { entry ->
                    ScoreRow(
                        entry = entry,
                        isBest = entry.correctAnswers == bestCorrectByExam[entry.examId],
                        onDelete = { pendingDeleteEntry = entry },
                    )
                }
            }
        }
    }

    pendingDeleteEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDeleteEntry = null },
            title = { Text("Borrar puntuación") },
            text = {
                Text(
                    "Se eliminará el resultado ${entry.correctAnswers}/${entry.totalQuestions} de ${entry.displayTitle}.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteEntry = null
                        onDeleteEntry(entry)
                    },
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteEntry = null }) {
                    Text("Cancelar")
                }
            },
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Borrar historial") },
            text = { Text("Se eliminarán todas las puntuaciones guardadas.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearAll()
                    },
                ) {
                    Text("Borrar todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun ScoreRow(
    entry: ScoreEntry,
    isBest: Boolean,
    onDelete: () -> Unit,
) {
    val container = if (isBest) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = container,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${entry.correctAnswers}/${entry.totalQuestions}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    if (isBest) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Mejor marca del modelo",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(20.dp),
                        )
                    }
                }
                Text(
                    text = entry.displayTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatTimestamp(entry.timestampMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier.padding(start = 12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "${entry.percentage}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = onDelete) {
                    Text("Borrar")
                }
            }
        }
    }
}

@Composable
private fun HistoryOptionsMenu(onClearAll: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text("Borrar historial") },
            onClick = {
                expanded = false
                onClearAll()
            },
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "Aún no has completado ninguna prueba.\n¡Haz un test para ver aquí tus puntuaciones!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun formatTimestamp(millis: Long): String {
    val formatter = remember {
        SimpleDateFormat("d MMM yyyy · HH:mm", Locale("es", "ES"))
    }
    return formatter.format(Date(millis))
}

private val ScoreEntry.displayTitle: String
    get() = examTitle.ifBlank { "Modelo sin título" }

// ---- Previews ----

@Preview(name = "Con puntuaciones", showBackground = true)
@Composable
private fun ScoreHistoryContentPreview() {
    A2MadridTheme {
        ScoreHistoryContent(
            entries = listOf(
                ScoreEntry("modelo_a", "Modelo A · Madrid esencial", 9, 10, 1_718_900_000_000),
                ScoreEntry("modelo_b", "Modelo B · Historia y símbolos", 6, 10, 1_718_800_000_000),
                ScoreEntry("modelo_a", "Modelo A · Madrid esencial", 4, 10, 1_718_700_000_000),
            ),
            onBack = {},
            onDeleteEntry = {},
            onClearAll = {},
        )
    }
}

@Preview(name = "Vacío", showBackground = true)
@Composable
private fun ScoreHistoryEmptyPreview() {
    A2MadridTheme {
        ScoreHistoryContent(
            entries = emptyList(),
            onBack = {},
            onDeleteEntry = {},
            onClearAll = {},
        )
    }
}
