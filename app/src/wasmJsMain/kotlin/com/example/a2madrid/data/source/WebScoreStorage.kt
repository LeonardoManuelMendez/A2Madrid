/*
 * ══ CAPA DE DATOS · ScoreStorage (actual Web/wasmJs) ══
 * Implementa el almacenamiento del historial con localStorage del navegador. Lo provee el
 * platformModule de wasmJs. localStorage es síncrono, así que las funciones suspend solo
 * envuelven la llamada directa.
 */
package com.example.a2madrid.data.source

import kotlinx.browser.localStorage

class WebScoreStorage : ScoreStorage {

    override suspend fun load(): String? = localStorage.getItem(KEY)

    override suspend fun save(json: String) = localStorage.setItem(KEY, json)

    private companion object {
        const val KEY = "score_history"
    }
}
