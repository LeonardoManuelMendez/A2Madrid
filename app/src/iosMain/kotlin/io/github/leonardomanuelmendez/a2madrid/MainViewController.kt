/*
 * ══ PUNTO DE ENTRADA · iOS (Kotlin/Native) ══
 * Expone un UIViewController que envuelve el composable común App() mediante
 * ComposeUIViewController. Lo invoca el proyecto Xcode (iosApp) desde SwiftUI.
 * Arranca Koin una sola vez (raíz del grafo de inyección).
 */
package io.github.leonardomanuelmendez.a2madrid

import androidx.compose.ui.window.ComposeUIViewController
import io.github.leonardomanuelmendez.a2madrid.di.initKoin
import platform.UIKit.UIViewController

private var koinStarted = false

fun MainViewController(): UIViewController {
    if (!koinStarted) {
        initKoin()
        koinStarted = true
    }
    return ComposeUIViewController { App() }
}
