/*
 * ══ INYECCIÓN DE DEPENDENCIAS · platformModule (actual Web/wasmJs) ══
 * Aporta las dependencias específicas del navegador: el ScoreStorage basado en localStorage.
 */
package io.github.leonardomanuelmendez.a2madrid.di

import io.github.leonardomanuelmendez.a2madrid.data.source.ScoreStorage
import io.github.leonardomanuelmendez.a2madrid.data.source.WebScoreStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ScoreStorage> { WebScoreStorage() }
}
