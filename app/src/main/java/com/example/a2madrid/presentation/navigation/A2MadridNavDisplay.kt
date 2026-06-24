/*
 * ══ CAPA DE PRESENTACIÓN · Host de navegación ══
 * Único NavDisplay de la app: mantiene el back stack y asocia cada ruta con su pantalla.
 * Aquí se "cablean" las Views: a cada Screen se le inyectan callbacks de navegación
 * (onExamSelected, onQuizFinished, onViewScores, onBack...) que empujan o sacan rutas.
 * El decorador de ViewModelStore + hiltViewModel() da a CADA destino su propio ViewModel.
 *
 *   ExamSelection ──(examId)──► Quiz ──(resultado)──► Result
 *         └───────────────► ScoreHistory ◄──────────────┘
 */
package com.example.a2madrid.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.a2madrid.presentation.examselection.ExamSelectionScreen
import com.example.a2madrid.presentation.quiz.QuizScreen
import com.example.a2madrid.presentation.quiz.QuizViewModel
import com.example.a2madrid.presentation.result.ResultScreen
import com.example.a2madrid.presentation.scorehistory.ScoreHistoryScreen

/**
 * Single Navigation 3 host for the app. Holds the back stack and maps each [androidx.navigation3.runtime.NavKey]
 * to a screen. The ViewModel-store decorator scopes a [androidx.lifecycle.ViewModelStore] per entry so that
 * Hilt ViewModels obtained via [hiltViewModel] live and die with their destination.
 */
@Composable
fun A2MadridNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(ExamSelectionRoute)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<ExamSelectionRoute> {
                ExamSelectionScreen(
                    onExamSelected = { examId -> backStack.add(QuizRoute(examId)) },
                    onViewScores = { backStack.add(ScoreHistoryRoute) },
                )
            }
            entry<QuizRoute> { route ->
                val viewModel: QuizViewModel = hiltViewModel()
                QuizScreen(
                    examId = route.examId,
                    viewModel = viewModel,
                    onQuizFinished = { result, examId, examTitle ->
                        backStack.clear()
                        backStack.add(
                            ResultRoute(
                                examId = examId,
                                examTitle = examTitle,
                                correctAnswers = result.correctAnswers,
                                totalQuestions = result.totalQuestions,
                                isNewBestScore = result.isNewBestScore,
                            ),
                        )
                    },
                    onViewScores = { backStack.add(ScoreHistoryRoute) },
                )
            }
            entry<ResultRoute> { route ->
                ResultScreen(
                    examId = route.examId,
                    examTitle = route.examTitle,
                    correctAnswers = route.correctAnswers,
                    totalQuestions = route.totalQuestions,
                    isNewBestScore = route.isNewBestScore,
                    onRestart = {
                        backStack.clear()
                        backStack.add(QuizRoute(route.examId))
                    },
                    onViewScores = { backStack.add(ScoreHistoryRoute) },
                )
            }
            entry<ScoreHistoryRoute> {
                ScoreHistoryScreen(
                    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                )
            }
        },
    )
}
