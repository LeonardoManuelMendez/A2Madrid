/*
 * ══ CAPA DE PRESENTACIÓN · Host de navegación (Compose Navigation · multiplataforma) ══
 * Único NavHost de la app: mantiene el back stack y asocia cada ruta tipada con su pantalla.
 * A cada Screen se le inyectan callbacks que navegan con el NavController. Los ViewModels los
 * provee Koin dentro de cada Screen (koinViewModel()), con scope por destino de navegación.
 *
 *   ExamSelection ──(examId)──► Quiz ──(resultado)──► Result ──(fallos)──► Review ──┐
 *         └───────────────► ScoreHistory ◄──────────────┘                            │
 *                                  └──────────(fallos de un intento anterior)─────────┘
 */
package io.github.leonardomanuelmendez.a2madrid.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.leonardomanuelmendez.a2madrid.presentation.examselection.ExamSelectionScreen
import io.github.leonardomanuelmendez.a2madrid.presentation.oppositionselection.OppositionSelectionScreen
import io.github.leonardomanuelmendez.a2madrid.presentation.quiz.QuizScreen
import io.github.leonardomanuelmendez.a2madrid.presentation.result.ResultScreen
import io.github.leonardomanuelmendez.a2madrid.presentation.scorehistory.ScoreHistoryScreen

@Composable
fun A2MadridNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // Vuelve a la pantalla de inicio (selección de oposición) limpiando el back stack.
    val goHome: () -> Unit = {
        navController.navigate(OppositionSelectionRoute) {
            popUpTo<OppositionSelectionRoute> { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = OppositionSelectionRoute,
        modifier = modifier,
    ) {
        composable<OppositionSelectionRoute> {
            OppositionSelectionScreen(
                onOppositionSelected = { oppositionId ->
                    navController.navigate(ExamSelectionRoute(oppositionId))
                },
                onViewScores = { navController.navigate(ScoreHistoryRoute) },
            )
        }
        composable<ExamSelectionRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ExamSelectionRoute>()
            ExamSelectionScreen(
                oppositionId = route.oppositionId,
                onExamSelected = { examId -> navController.navigate(QuizRoute(examId)) },
                onSimulationSelected = { examId -> navController.navigate(SimulationRoute(examId)) },
                onViewScores = { navController.navigate(ScoreHistoryRoute) },
                onBack = { navController.popBackStack() },
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
                            attemptMillis = result.attemptMillis,
                        ),
                    ) { popUpTo<QuizRoute> { inclusive = true } }
                },
                onViewScores = { navController.navigate(ScoreHistoryRoute) },
                onGoHome = goHome,
            )
        }
        composable<SimulationRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<SimulationRoute>()
            QuizScreen(
                examId = route.examId,
                isSimulation = true,
                onQuizFinished = { result, examId, examTitle ->
                    navController.navigate(
                        ResultRoute(
                            examId = examId,
                            examTitle = examTitle,
                            correctAnswers = result.correctAnswers,
                            totalQuestions = result.totalQuestions,
                            isNewBestScore = result.isNewBestScore,
                            attemptMillis = result.attemptMillis,
                            isExam = true,
                        ),
                    ) { popUpTo<SimulationRoute> { inclusive = true } }
                },
                onViewScores = { navController.navigate(ScoreHistoryRoute) },
                onGoHome = goHome,
            )
        }
        composable<ReviewRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ReviewRoute>()
            QuizScreen(
                examId = route.examId,
                reviewAttemptMillis = route.attemptMillis,
                onQuizFinished = { result, examId, examTitle ->
                    navController.navigate(
                        ResultRoute(
                            examId = examId,
                            examTitle = examTitle,
                            correctAnswers = result.correctAnswers,
                            totalQuestions = result.totalQuestions,
                            isNewBestScore = result.isNewBestScore,
                            attemptMillis = result.attemptMillis,
                            isReview = true,
                        ),
                    ) { popUpTo<ReviewRoute> { inclusive = true } }
                },
                onViewScores = { navController.navigate(ScoreHistoryRoute) },
                onGoHome = goHome,
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
                attemptMillis = route.attemptMillis,
                isReview = route.isReview,
                isExam = route.isExam,
                onRestart = {
                    navController.navigate(QuizRoute(route.examId)) {
                        popUpTo<ResultRoute> { inclusive = true }
                    }
                },
                onReviewWrong = {
                    navController.navigate(
                        ReviewRoute(
                            examId = route.examId,
                            examTitle = route.examTitle,
                            attemptMillis = route.attemptMillis,
                        ),
                    ) { popUpTo<ResultRoute> { inclusive = true } }
                },
                onViewScores = { navController.navigate(ScoreHistoryRoute) },
            )
        }
        composable<ScoreHistoryRoute> {
            ScoreHistoryScreen(
                onBack = { navController.popBackStack() },
                onGoHome = goHome,
                onReviewAttempt = { entry ->
                    navController.navigate(
                        ReviewRoute(
                            examId = entry.examId,
                            examTitle = entry.examTitle,
                            attemptMillis = entry.timestampMillis,
                        ),
                    )
                },
            )
        }
    }
}
