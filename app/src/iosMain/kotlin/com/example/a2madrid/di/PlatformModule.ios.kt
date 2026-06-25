/*
 * ══ INYECCIÓN DE DEPENDENCIAS · platformModule (actual iOS) ══
 * Aporta las dependencias específicas de iOS: el ScoreStorage basado en NSUserDefaults.
 */
package com.example.a2madrid.di

import com.example.a2madrid.data.source.IosScoreStorage
import com.example.a2madrid.data.source.ScoreStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ScoreStorage> { IosScoreStorage() }
}
