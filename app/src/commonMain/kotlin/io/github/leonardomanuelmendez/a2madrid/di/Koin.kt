/*
 * ══ INYECCIÓN DE DEPENDENCIAS (Koin · multiplataforma) ══
 * Sustituye a Hilt. `appModule` (común) declara Json, data source, repositorio, casos de uso
 * y ViewModels. `platformModule` (expect/actual) aporta lo específico de cada plataforma
 * (ScoreStorage: SharedPreferences en Android, NSUserDefaults en iOS, localStorage en web).
 * Cada entrada de plataforma llama a initKoin() al arrancar.
 */
package io.github.leonardomanuelmendez.a2madrid.di

import io.github.leonardomanuelmendez.a2madrid.data.repository.QuizRepositoryImpl
import io.github.leonardomanuelmendez.a2madrid.data.source.ExamResourceDataSource
import io.github.leonardomanuelmendez.a2madrid.domain.repository.QuizRepository
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.ClearScoresUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.DeleteScoreUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.EvaluateAnswerUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetAttemptReviewUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetExamUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetExamsUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.GetOppositionsUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.ObserveScoreHistoryUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.SaveScoreUseCase
import io.github.leonardomanuelmendez.a2madrid.domain.usecase.ShuffleQuestionsUseCase
import io.github.leonardomanuelmendez.a2madrid.presentation.examselection.ExamSelectionViewModel
import io.github.leonardomanuelmendez.a2madrid.presentation.oppositionselection.OppositionSelectionViewModel
import io.github.leonardomanuelmendez.a2madrid.presentation.quiz.QuizViewModel
import io.github.leonardomanuelmendez.a2madrid.presentation.result.ResultViewModel
import io.github.leonardomanuelmendez.a2madrid.presentation.scorehistory.ScoreHistoryViewModel
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { Json { ignoreUnknownKeys = true; isLenient = true } }

    singleOf(::ExamResourceDataSource)
    singleOf(::QuizRepositoryImpl) bind QuizRepository::class

    factoryOf(::GetOppositionsUseCase)
    factoryOf(::GetExamsUseCase)
    factoryOf(::GetExamUseCase)
    factoryOf(::GetAttemptReviewUseCase)
    factoryOf(::EvaluateAnswerUseCase)
    factory { ShuffleQuestionsUseCase() }
    factoryOf(::SaveScoreUseCase)
    factoryOf(::ObserveScoreHistoryUseCase)
    factoryOf(::DeleteScoreUseCase)
    factoryOf(::ClearScoresUseCase)

    viewModelOf(::OppositionSelectionViewModel)
    viewModelOf(::ExamSelectionViewModel)
    // A mano y no con viewModelOf: el reloj del cronómetro lleva valor por defecto y el
    // DSL de constructor intentaría resolverlo por tipo.
    viewModel { QuizViewModel(get(), get(), get(), get(), get()) }
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
