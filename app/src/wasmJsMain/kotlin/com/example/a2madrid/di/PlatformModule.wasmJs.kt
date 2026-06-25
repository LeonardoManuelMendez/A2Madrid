/*
 * ══ INYECCIÓN DE DEPENDENCIAS · platformModule (actual Web/wasmJs) ══
 * Aporta las dependencias específicas del navegador: el ScoreStorage basado en localStorage.
 */
package com.example.a2madrid.di

import com.example.a2madrid.data.source.ScoreStorage
import com.example.a2madrid.data.source.WebScoreStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ScoreStorage> { WebScoreStorage() }
}
