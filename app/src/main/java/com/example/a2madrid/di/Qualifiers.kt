/*
 * ══ INYECCIÓN DE DEPENDENCIAS (Hilt) ══
 * Un @Qualifier distingue dos objetos del mismo tipo. Aquí marca CUÁL CoroutineDispatcher
 * inyectar (el de IO), para no confundirlo con otros dispatchers. Se define en AppModule
 * y se pide con @IoDispatcher en los constructores que lo necesitan.
 */
package com.example.a2madrid.di

import javax.inject.Qualifier

/** Marks the [kotlinx.coroutines.CoroutineDispatcher] used for IO-bound work. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher