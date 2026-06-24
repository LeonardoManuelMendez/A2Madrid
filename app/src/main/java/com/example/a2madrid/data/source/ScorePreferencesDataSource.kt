/*
 * ══ CAPA DE DATOS · DataSource (persistencia local) ══
 * Sabe leer/escribir el historial en Preferences DataStore. Guarda la lista como una
 * cadena JSON y la expone como Flow<List<ScoreEntryDto>> (reactivo). Trabaja en DTOs;
 * el repositorio mapea a dominio. @Singleton porque el DataStore debe ser único por proceso.
 */
package com.example.a2madrid.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.a2madrid.data.dto.ScoreEntryDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quiz_preferences")

/** Persists the player's score history as a JSON-encoded list in Preferences DataStore. */
@Singleton
class ScorePreferencesDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json,
) {
    private val historyKey = stringPreferencesKey("score_history")
    private val listSerializer = ListSerializer(ScoreEntryDto.serializer())

    val scoreHistory: Flow<List<ScoreEntryDto>> = context.dataStore.data.map { prefs ->
        prefs.decodeHistory()
    }

    suspend fun addScore(entry: ScoreEntryDto) {
        context.dataStore.edit { prefs ->
            val updated = prefs.decodeHistory() + entry
            prefs[historyKey] = json.encodeToString(listSerializer, updated)
        }
    }

    suspend fun deleteScore(entry: ScoreEntryDto) {
        context.dataStore.edit { prefs ->
            val updated = prefs.decodeHistory().toMutableList().apply { remove(entry) }
            prefs[historyKey] = json.encodeToString(listSerializer, updated)
        }
    }

    suspend fun clearScores() {
        context.dataStore.edit { prefs -> prefs.remove(historyKey) }
    }

    private fun Preferences.decodeHistory(): List<ScoreEntryDto> =
        this[historyKey]?.let { raw ->
            runCatching { json.decodeFromString(listSerializer, raw) }.getOrDefault(emptyList())
        } ?: emptyList()
}