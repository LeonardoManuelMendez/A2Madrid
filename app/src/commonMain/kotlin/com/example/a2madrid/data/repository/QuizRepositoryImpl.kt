/*
 * ══ CAPA DE DATOS · Repository (implementación · multiplataforma) ══
 * Cumple el contrato QuizRepository del dominio. Carga los exámenes desde Compose Resources
 * y mantiene el historial en memoria (StateFlow) sincronizándolo con ScoreStorage (la KV de
 * cada plataforma). Devuelve siempre modelos de dominio. Koin lo enlaza a la interfaz.
 */
package com.example.a2madrid.data.repository

import com.example.a2madrid.data.dto.ScoreEntryDto
import com.example.a2madrid.data.mapper.toDomain
import com.example.a2madrid.data.mapper.toDto
import com.example.a2madrid.data.source.ExamResourceDataSource
import com.example.a2madrid.data.source.ScoreStorage
import com.example.a2madrid.domain.model.Exam
import com.example.a2madrid.domain.model.ScoreEntry
import com.example.a2madrid.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class QuizRepositoryImpl(
    private val examDataSource: ExamResourceDataSource,
    private val scoreStorage: ScoreStorage,
    private val json: Json,
) : QuizRepository {

    private val listSerializer = ListSerializer(ScoreEntryDto.serializer())
    private val history = MutableStateFlow<List<ScoreEntry>>(emptyList())
    private val mutex = Mutex()
    private var loaded = false

    override suspend fun getExams(): List<Exam> =
        examDataSource.loadExams().exams.map { it.toDomain() }

    override val scoreHistory: Flow<List<ScoreEntry>> = flow {
        ensureLoaded()
        emitAll(history)
    }

    override suspend fun addScore(entry: ScoreEntry) = mutate { it + entry }

    override suspend fun deleteScore(entry: ScoreEntry) =
        mutate { current -> current.toMutableList().apply { remove(entry) } }

    override suspend fun clearScores() = mutate { emptyList() }

    private suspend fun ensureLoaded() {
        if (loaded) return
        mutex.withLock {
            if (loaded) return
            val raw = scoreStorage.load()
            history.value = raw?.let { stored ->
                runCatching {
                    json.decodeFromString(listSerializer, stored).map { it.toDomain() }
                }.getOrDefault(emptyList())
            } ?: emptyList()
            loaded = true
        }
    }

    private suspend fun mutate(transform: (List<ScoreEntry>) -> List<ScoreEntry>) {
        ensureLoaded()
        mutex.withLock {
            val updated = transform(history.value)
            history.value = updated
            scoreStorage.save(json.encodeToString(listSerializer, updated.map { it.toDto() }))
        }
    }
}
