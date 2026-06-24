/*
 * ══ CAPA DE PRESENTACIÓN · Host de navegación (Compose Navigation · multiplataforma) ══
 * Único NavHost de la app: mantiene el back stack y asocia cada ruta tipada con su pantalla.
 * A cada Screen se le inyectan callbacks que navegan con el NavController. Los ViewModels los
 * provee Koin dentro de cada Screen (koinViewModel()), con scope por destino de navegación.
 *
 *   ExamSelection ──(examId)──► Quiz ──(resultado)──► Result
 *         └───────────────► ScoreHistory ◄──────────────┘
 */
package com.example.a2madrid.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.a2madrid.presentation.examselection.ExamSelectionScreen
import com.example.a2madrid.presentation.quiz.QuizScreen
import com.example.a2madrid.presentation.result.ResultScreen
import com.example.a2madrid.presentation.scorehistory.ScoreHistoryScreen

@Composable
fun A2MadridNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ExamSelectionRoute,
        modifier = modifier,
    ) {
        composable<ExamSelectionRoute> {
            ExamSelectionScreen(
                onExamSelected = { examId -> navController.navigate(QuizRoute(examId)) },
                onViewScores = { navController.navigate(ScoreHistoryRoute) },
            )
        }
        composable<QuizRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<QuizRoute>()
            QuizScreen(
                examId = route.examId,
                onQuizFinished = { result, examId, examTitle ->
                    navController.navigate(
                        ResultRoute(
                            examId = examId,
                            examTitle = examTitle,
                            correctAnswers = result.correctAnswers,
                            totalQuestions = result.totalQuestions,
                            isNewBestScore = result.isNewBestScore,
                        ),
                    ) { popUpTo<QuizRoute> { inclusive = true } }
                },
                onViewScores = { navController.navigate(ScoreHistoryRoute) },
            )
        }
        composable<ResultRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ResultRoute>()
            ResultScreen(
                examId = route.examId,
                examTitle = route.examTitle,
                correctAnswers = route.correctAnswers,
                totalQuestions = route.totalQuestions,
                isNewBestScore = route.isNewBestScore,
                onRestart = {
                    navController.navigate(QuizRoute(route.examId)) {
                        popUpTo<ResultRoute> { inclusive = true }
                    }
                },
                onViewScores = { navController.navigate(ScoreHistoryRoute) },
            )
        }
        composable<ScoreHistoryRoute> {
            ScoreHistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
