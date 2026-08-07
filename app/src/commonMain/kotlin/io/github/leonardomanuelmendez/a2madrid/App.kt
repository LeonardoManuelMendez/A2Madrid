/*
 * ══ CAPA DE PRESENTACIÓN · Raíz de la UI (multiplataforma) ══
 * Punto de entrada Compose común a todas las plataformas: aplica el tema y monta el host de
 * navegación. Cada plataforma (Android Activity, iOS UIViewController, Web canvas) solo tiene
 * que arrancar Koin y renderizar este App().
 */
package io.github.leonardomanuelmendez.a2madrid

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.leonardomanuelmendez.a2madrid.presentation.navigation.A2MadridNavHost
import io.github.leonardomanuelmendez.a2madrid.presentation.theme.A2MadridTheme

@Composable
fun App() {
    A2MadridTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            A2MadridNavHost()
        }
    }
}
