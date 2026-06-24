/*
 * ══ INYECCIÓN DE DEPENDENCIAS · platformModule (actual Android) ══
 * Aporta las dependencias específicas de Android: aquí, el ScoreStorage basado en
 * SharedPreferences (con el Context que Koin conoce vía androidContext()).
 */
package com.example.a2madrid.di

import com.example.a2madrid.data.source.AndroidScoreStorage
import com.example.a2madrid.data.source.ScoreStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ScoreStorage> { AndroidScoreStorage(androidContext()) }
}
