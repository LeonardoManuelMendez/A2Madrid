/*
 * ╔══════ CAPA DE DOMINIO · Repository (interfaz = "puerto") ══════╗
 * Declara QUÉ datos necesita la app, nunca CÓMO se obtienen. Es la frontera
 * entre dominio y datos: el dominio depende de esta interfaz y la capa de DATOS
 * la implementa (QuizRepositoryImpl). Por eso las dependencias apuntan siempre
 * "hacia dentro" (regla de Clean Architecture).
 *
 * FLUJO GENERAL (de fuera hacia dentro y vuelta):
 *
 *   [PRESENTACIÓN]              [DOMINIO]                 [DATOS]
 *   Composable (View)
 *        │ eventos
 *        ▼
 *   ViewModel ──llama──► UseCase ──usa──► QuizRepository (esta interfaz)
 *        ▲ StateFlow                          ▲ implementa
 *        │                          QuizRepositoryImpl
 *        │                            ├─ ExamAssetDataSource → assets/exams.json
 *        └──── UI se recompone        └─ ScorePreferencesDataSource → DataStore
 *
 *   Hilt (paquete di/) "inyecta" la implementación allí donde se pide la interfaz.
 *   Los mappers traducen DTO (datos) ⇄ modelo de dominio.
 */
package com.example.a2madrid.domain.repository

import com.example.a2madrid.domain.model.Exam
import com.example.a2madrid.domain.model.ScoreEntry
import kotlinx.coroutines.flow.Flow

/**
 * Domain abstraction over quiz data. The presentation layer depends on this interface,
 * never on a concrete data source, keeping the dependency rule pointing inwards.
 */
interface QuizRepository {

    /** Loads every available exam model with its questions. */
    suspend fun getExams(): List<Exam>

    /** Stream of every score the player has obtained, oldest first. */
    val scoreHistory: Flow<List<ScoreEntry>>

    /** Appends a finished-quiz [entry] to the persisted history. */
    suspend fun addScore(entry: ScoreEntry)

    /** Removes a single [entry] from the history. */
    suspend fun deleteScore(entry: ScoreEntry)

    /** Removes every stored score. */
    suspend fun clearScores()
}