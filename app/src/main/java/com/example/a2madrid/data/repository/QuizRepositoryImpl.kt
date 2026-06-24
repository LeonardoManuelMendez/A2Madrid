/*
 * ══ CAPA DE DATOS · Repository (implementación = "adaptador") ══
 * Cumple el contrato QuizRepository del dominio. Coordina las fuentes (exams.json y
 * DataStore) y usa los mappers para devolver SIEMPRE modelos de dominio, ocultando DTOs
 * y detalles de almacenamiento. Hilt enlaza esta clase a la interfaz en RepositoryModule,
 * así los UseCase reciben esta implementación sin conocerla.
 */
package com.example.a2madrid.data.repository

import com.example.a2madrid.data.mapper.toDomain
import com.example.a2madrid.data.mapper.toDto
import com.example.a2madrid.data.source.ExamAssetDataSource
import com.example.a2madrid.data.source.ScorePreferencesDataSource
import com.example.a2madrid.domain.model.Exam
import com.example.a2madrid.domain.model.ScoreEntry
import com.example.a2madrid.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [QuizRepository] backed by a bundled JSON asset for exams and
 * DataStore for the persisted score history.
 */
@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val examDataSource: ExamAssetDataSource,
    private val scoreDataSource: ScorePreferencesDataSource,
) : QuizRepository {

    override suspend fun getExams(): List<Exam> =
        examDataSource.loadExams().exams.map { it.toDomain() }

    override val scoreHistory: Flow<List<ScoreEntry>> =
        scoreDataSource.scoreHistory.map { dtos -> dtos.map { it.toDomain() } }

    override suspend fun addScore(entry: ScoreEntry) =
        scoreDataSource.addScore(entry.toDto())

    override suspend fun deleteScore(entry: ScoreEntry) =
        scoreDataSource.deleteScore(entry.toDto())

    override suspend fun clearScores() =
        scoreDataSource.clearScores()
}