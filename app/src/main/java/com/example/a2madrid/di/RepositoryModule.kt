/*
 * ══ INYECCIÓN DE DEPENDENCIAS (Hilt) ══
 * Módulo que ENLAZA la interfaz del dominio (QuizRepository) con su implementación de
 * datos (QuizRepositoryImpl). Gracias a @Binds, cuando un UseCase pide un QuizRepository,
 * Hilt le entrega el Impl. Es lo que permite que el dominio NO dependa de la capa de datos.
 */
package com.example.a2madrid.di

import com.example.a2madrid.data.repository.QuizRepositoryImpl
import com.example.a2madrid.domain.repository.QuizRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds data-layer implementations to their domain-layer abstractions. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuizRepository(impl: QuizRepositoryImpl): QuizRepository
}