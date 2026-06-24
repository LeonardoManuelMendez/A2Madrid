/*
 * ══ PUNTO DE ENTRADA · Activity (UI) ══
 * Única Activity, @AndroidEntryPoint para que Hilt pueda inyectar aquí. Activa edge-to-edge,
 * aplica el tema Compose (A2MadridTheme) y monta A2MadridNavDisplay (el host de navegación).
 * A partir de aquí TODO es Compose: la Activity solo es el contenedor del árbol de UI.
 */
package com.example.a2madrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.a2madrid.presentation.navigation.A2MadridNavDisplay
import com.example.a2madrid.presentation.theme.A2MadridTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A2MadridTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    A2MadridNavDisplay()
                }
            }
        }
    }
}