/*
 * ══ CAPA DE DATOS · ScoreStorage (actual iOS) ══
 * Implementa el almacenamiento del historial con NSUserDefaults. Lo provee el platformModule de
 * iOS. NSUserDefaults es síncrono, así que las funciones suspend solo envuelven la llamada.
 */
package com.example.a2madrid.data.source

import platform.Foundation.NSUserDefaults

class IosScoreStorage : ScoreStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun load(): String? = defaults.stringForKey(KEY)

    override suspend fun save(json: String) = defaults.setObject(json, forKey = KEY)

    private companion object {
        const val KEY = "score_history"
    }
}
