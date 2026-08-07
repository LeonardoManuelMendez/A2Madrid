package io.github.leonardomanuelmendez.a2madrid.data

import io.github.leonardomanuelmendez.a2madrid.data.repository.QuizRepositoryImpl
import io.github.leonardomanuelmendez.a2madrid.data.source.ExamResourceDataSource
import io.github.leonardomanuelmendez.a2madrid.data.source.ScoreStorage
import io.github.leonardomanuelmendez.a2madrid.domain.model.ScoreEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Comprueba que el historial PERSISTE entre sesiones sin base de datos: una nueva instancia del
 * repositorio (simula reabrir la app) lee del mismo ScoreStorage lo que guardó la anterior.
 */
class QuizRepositoryPersistenceTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val examDataSource = ExamResourceDataSource(json) // no se usa aquí (solo getExams)

    /** Almacenamiento de prueba que persiste en memoria, igual que localStorage/SharedPreferences. */
    private class PersistentFakeStorage(var data: String? = null) : ScoreStorage {
        override suspend fun load(): String? = data
        override suspend fun save(json: String) { data = json }
    }

    @Test
    fun `el historial sobrevive a una nueva instancia del repositorio`() = runTest {
        val storage = PersistentFakeStorage()
        val entry = ScoreEntry("modelo_a", "Modelo A", 8, 10, 1_000L)

        // Sesión 1: guardar una puntuación.
        QuizRepositoryImpl(examDataSource, storage, json).addScore(entry)

        // Sesión 2: instancia nueva, mismo almacenamiento (como reabrir la app).
        val reopened = QuizRepositoryImpl(examDataSource, storage, json)
        val loaded = reopened.scoreHistory.first()

        assertEquals(listOf(entry), loaded)
    }

    @Test
    fun `acumula puntuaciones de varias sesiones`() = runTest {
        val storage = PersistentFakeStorage()
        val a = ScoreEntry("m", "M", 5, 10, 1L)
        val b = ScoreEntry("m", "M", 9, 10, 2L)

        QuizRepositoryImpl(examDataSource, storage, json).addScore(a)
        QuizRepositoryImpl(examDataSource, storage, json).addScore(b)
        val loaded = QuizRepositoryImpl(examDataSource, storage, json).scoreHistory.first()

        assertEquals(listOf(a, b), loaded)
    }
}
