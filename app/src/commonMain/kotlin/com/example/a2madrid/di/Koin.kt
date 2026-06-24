/*
 * ══ INYECCIÓN DE DEPENDENCIAS (Koin · multiplataforma) ══
 * Sustituye a Hilt. `appModule` (común) declara Json, data source, repositorio, casos de uso
 * y ViewModels. `platformModule` (expect/actual) aporta lo específico de cada plataforma
 * (ScoreStorage: SharedPreferences en Android, NSUserDefaults en iOS, localStorage en web).
 * Cada entrada de plataforma llama a initKoin() al arrancar.
 */
package com.example.a2madrid.di

import com.example.a2madrid.data.repository.QuizRepositoryImpl
import com.example.a2madrid.data.source.ExamResourceDataSource
import com.example.a2madrid.domain.repository.QuizRepository
import com.example.a2madrid.domain.usecase.ClearScoresUseCase
import com.example.a2madrid.domain.usecase.DeleteScoreUseCase
import com.example.a2madrid.domain.usecase.EvaluateAnswerUseCase
import com.example.a2madrid.domain.usecase.GetExamUseCase
import com.example.a2madrid.domain.usecase.GetExamsUseCase
import com.example.a2madrid.domain.usecase.ObserveScoreHistoryUseCase
import com.example.a2madrid.domain.usecase.SaveScoreUseCase
import com.example.a2madrid.presentation.examselection.ExamSelectionViewModel
import com.example.a2madrid.presentation.quiz.QuizViewModel
import com.example.a2madrid.presentation.result.ResultViewModel
import com.example.a2madrid.presentation.scorehistory.ScoreHistoryViewModel
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { Json { ignoreUnknownKeys = true; isLenient = true } }

    singleOf(::ExamResourceDataSource)
    singleOf(::QuizRepositoryImpl) bind QuizRepository::class

    factoryOf(::GetExamsUseCase)
    factoryOf(::GetExamUseCase)
    factoryOf(::EvaluateAnswerUseCase)
    factoryOf(::SaveScoreUseCase)
    factoryOf(::ObserveScoreHistoryUseCase)
    factoryOf(::DeleteScoreUseCase)
    factoryOf(::ClearScoresUseCase)

    viewModelOf(::ExamSelectionViewModel)
    viewModelOf(::QuizViewModel)
    viewModelOf(::ResultViewModel)
    viewModelOf(::ScoreHistoryViewModel)
}

/** Bindings específicos de cada plataforma (p. ej. ScoreStorage). */
expect val platformModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(appModule, platformModule)
    }
