/*
 * ══ CAPA DE DATOS · Almacenamiento clave-valor (multiplataforma) ══
 * Abstracción mínima para persistir el historial (un string JSON). Cada plataforma aporta su
 * implementación vía Koin (platformModule): Android = SharedPreferences, iOS = NSUserDefaults,
 * Web = localStorage. Así el dominio/repositorio no conocen la API concreta de cada sistema.
 */
package com.example.a2madrid.data.source

interface ScoreStorage {
    /** Devuelve el JSON guardado (o null si no hay nada). */
    suspend fun load(): String?

    /** Persiste el JSON del historial. */
    suspend fun save(json: String)
}
