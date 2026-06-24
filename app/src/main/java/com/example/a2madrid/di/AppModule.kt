/*
 * ══ INYECCIÓN DE DEPENDENCIAS (Hilt) ══
 * Provee objetos transversales que no se construyen con @Inject: el parser Json y el
 * dispatcher de IO (etiquetado con @IoDispatcher). @InstallIn(SingletonComponent) = viven
 * mientras vive la app. Hilt los entrega a quien los pida (p. ej. ExamAssetDataSource).
 */
package com.example.a2madrid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/** Process-wide bindings for cross-cutting infrastructure (serialization, dispatchers). */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}