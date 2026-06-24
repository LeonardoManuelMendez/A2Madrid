/*
 * ══ CAPA DE DATOS · ScoreStorage (actual Android) ══
 * Implementa el almacenamiento del historial con SharedPreferences. Lo provee el platformModule
 * de Android (con el Context inyectado por Koin).
 */
package com.example.a2madrid.data.source

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidScoreStorage(context: Context) : ScoreStorage {

    private val prefs = context.getSharedPreferences("quiz_scores", Context.MODE_PRIVATE)

    override suspend fun load(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY, null)
    }

    override suspend fun save(json: String) {
        withContext(Dispatchers.IO) {
            prefs.edit().putString(KEY, json).apply()
        }
    }

    private companion object {
        const val KEY = "score_history"
    }
}
