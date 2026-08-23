/*
 * ══ CAPA DE PRESENTACIÓN · View (Composable) ══
 * Las instrucciones que se leen antes de abrir el cuadernillo. Un simulacro compromete casi una
 * hora, penaliza el error y no corrige hasta el final: no debe empezar de un toque por curiosidad.
 * UI pura, sin ViewModel: todo lo que necesita llega por la ruta.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.leonardomanuelmendez.a2madrid.presentation.ContentMaxWidth
import io.github.leonardomanuelmendez.a2madrid.presentation.theme.A2MadridTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationBriefingScreen(
    examTitle: String,
    questionCount: Int,
    onStart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Antes de empezar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = examTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Vas a hacer este modelo con las reglas del examen real.",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Rule("⏱", "$questionCount minutos", "un minuto por pregunta, la proporción del examen real")
                        Rule("−⅓", "Cada error penaliza", "resta un tercio de lo que vale un acierto")
                        Rule("○", "Puedes dejar en blanco", "una pregunta sin contestar no puntúa ni penaliza")
                        Rule("✗", "No se corrige hasta el final", "no verás si aciertas mientras lo haces")
                        Rule("←", "No se puede volver atrás", "una vez avanzas, no se regresa a la pregunta anterior")
                    }
                }

                Text(
                    text = "Al terminar verás la nota con la penalización aplicada, y el desglose " +
                        "pregunta a pregunta para repasar los fallos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                    Text("Empezar simulacro")
                }
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Ahora no")
                }
            }
        }
    }
}

/** Una regla: su símbolo, el titular y la letra pequeña que lo explica. */
@Composable
private fun Rule(symbol: String, title: String, detail: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(36.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Instrucciones del simulacro", showBackground = true)
@Composable
private fun SimulationBriefingPreview() {
    A2MadridTheme {
        SimulationBriefingScreen(
            examTitle = "Orden 2411/2017 · C2 (2018) — Legislación",
            questionCount = 45,
            onStart = {},
            onBack = {},
        )
    }
}
