/*
 * ══ PUNTO DE ENTRADA · Activity (Android) ══
 * Única Activity. Activa edge-to-edge y monta el composable común App() (tema + navegación).
 * La inyección (Koin) ya está arrancada en A2MadridApplication. A partir de aquí TODO es
 * Compose Multiplatform compartido; la Activity es solo el contenedor del árbol de UI.
 */
package io.github.leonardomanuelmendez.a2madrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}
