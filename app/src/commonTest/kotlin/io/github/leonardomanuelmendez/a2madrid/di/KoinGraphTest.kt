package io.github.leonardomanuelmendez.a2madrid.di

import io.github.leonardomanuelmendez.a2madrid.data.source.ScoreStorage
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetAttemptReviewUseCase
import io.github.leonardomanuelmendez.a2madrid.presentation.quiz.QuizViewModel
import io.github.leonardomanuelmendez.a2madrid.presentation.result.ResultViewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Los tests de ViewModel construyen sus dependencias A MANO, así que no verían un binding de
 * Koin que falta: la app compilaría, pasaría los tests y reventaría al abrir la pantalla.
 * Este test resuelve el grafo de verdad para cerrar ese hueco.
 */
class KoinGraphTest {

    /** Sustituto del platformModule (SharedPreferences / NSUserDefaults / localStorage). */
    private val fakeStorageModule = module {
        single<ScoreStorage> {
            object : ScoreStorage {
                override suspend fun load(): String? = null
                override suspend fun save(json: String) = Unit
            }
        }
    }

    private val koin = koinApplication { modules(appModule, fakeStorageModule) }.koin

    @Test
    fun `el grafo resuelve los ViewModels de test y de resultado`() {
        assertNotNull(koin.get<QuizViewModel>())
        assertNotNull(koin.get<ResultViewModel>())
    }

    @Test
    fun `el grafo resuelve el caso de uso que reconstruye el desglose`() {
        assertNotNull(koin.get<GetAttemptReviewUseCase>())
    }
}
