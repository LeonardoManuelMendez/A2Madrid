/*
 * ══ CAPA DE PRESENTACIÓN · View (Composable) ══
 * Pantalla de selección de oposición. Observa OppositionSelectionViewModel y dibuja las opciones.
 * Recibe estado y emite eventos (onOppositionSelected, onViewScores).
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.oppositionselection

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.leonardomanuelmendez.a2madrid.domain.model.Opposition
import io.github.leonardomanuelmendez.a2madrid.presentation.ContentMaxWidth
import io.github.leonardomanuelmendez.a2madrid.presentation.theme.A2MadridTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OppositionSelectionScreen(
    onOppositionSelected: (String) -> Unit,
    onViewScores: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OppositionSelectionViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    OppositionSelectionContent(
        uiState = uiState,
        onOppositionSelected = onOppositionSelected,
        onViewScores = onViewScores,
        onRetry = viewModel::load,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OppositionSelectionContent(
    uiState: OppositionSelectionUiState,
    onOppositionSelected: (String) -> Unit,
    onViewScores: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("A2Madrid") },
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
                        text = "¿Qué oposición deseas preparar?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    uiState.oppositions.forEach { opposition ->
                        OppositionCard(
                            opposition = opposition,
                            onClick = { onOppositionSelected(opposition.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OppositionCard(opposition: Opposition, onClick: () -> Unit) {
    val alpha = if (opposition.isActive) 1f else 0.5f
    Surface(
        onClick = onClick,
        enabled = opposition.isActive,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = alpha),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = opposition.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (!opposition.isActive) {
                Text(
                    text = "Próximamente disponible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
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

@Preview(name = "Selección de oposición", showBackground = true)
@Composable
private fun OppositionSelectionContentPreview() {
    A2MadridTheme {
        OppositionSelectionContent(
            uiState = OppositionSelectionUiState(
                isLoading = false,
                oppositions = listOf(
                    Opposition("1", "Cuerpo de Administrativos (C1)", false),
                    Opposition("2", "Administración General (C2)", true),
                    Opposition("3", "Técnicos Superiores (A1)", false),
                ),
            ),
            onOppositionSelected = {},
            onViewScores = {},
            onRetry = {},
        )
    }
}
