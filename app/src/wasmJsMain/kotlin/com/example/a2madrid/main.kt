/*
 * ══ PUNTO DE ENTRADA · Web (wasmJs) ══
 * Arranca Koin (raíz del grafo de inyección) y monta el composable común App() sobre el
 * <body> del documento mediante ComposeViewport. A partir de aquí todo es Compose Multiplatform
 * compartido; este main solo es el contenedor del árbol de UI en el navegador.
 */
package com.example.a2madrid

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.a2madrid.di.initKoin
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    ComposeViewport(document.body!!) {
        App()
    }
}
