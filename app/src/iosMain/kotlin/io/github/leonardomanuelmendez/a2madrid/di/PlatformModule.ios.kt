/*
 * ══ INYECCIÓN DE DEPENDENCIAS · platformModule (actual iOS) ══
 * Aporta las dependencias específicas de iOS: el ScoreStorage basado en NSUserDefaults.
 */
package io.github.leonardomanuelmendez.a2madrid.di

import io.github.leonardomanuelmendez.a2madrid.data.source.IosScoreStorage
import io.github.leonardomanuelmendez.a2madrid.data.source.ScoreStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ScoreStorage> { IosScoreStorage() }
}
