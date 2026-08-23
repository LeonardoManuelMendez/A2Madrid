/*
 * ══ CAPA DE DOMINIO · UseCase ══
 * Reconstruye el DESGLOSE de un intento ya terminado. El historial guarda solo ids de pregunta
 * e índices elegidos, así que aquí se cruzan con las preguntas del examen para devolver
 * AnswerResult completos (enunciado, opciones, explicación).
 * → lo usa ResultViewModel para pintar el desglose y para armar el repaso de fallos.
 *
 * Se degrada en silencio: si el intento no existe, es anterior a esta funcionalidad, o el
 * contenido del examen ha cambiado y una respuesta ya no casa, esa respuesta se descarta en
 * vez de reventar la pantalla.
 *
 * Las preguntas se buscan en TODOS los modelos, no solo en el del intento: un simulacro se guarda
 * con un examId sintético y mezcla preguntas de los tres. Es seguro porque ExamsContentTest
 * garantiza que los ids de pregunta son únicos en todo el fichero.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.usecase

import io.github.leonardomanuelmendez.a2madrid.domain.model.AnswerResult
import io.github.leonardomanuelmendez.a2madrid.domain.repository.QuizRepository
import kotlinx.coroutines.flow.first

/** Rebuilds the per-question detail of a stored attempt. */
class GetAttemptReviewUseCase(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(examId: String, attemptMillis: Long): List<AnswerResult> {
        val entry = repository.scoreHistory.first()
            .firstOrNull { it.examId == examId && it.timestampMillis == attemptMillis }
            ?: return emptyList()
        if (entry.answers.isEmpty()) return emptyList()

        val answeredBy = entry.answers.associateBy { it.questionId }

        // Recorrer el contenido (y no las respuestas) fija el orden y descarta de paso las
        // respuestas huérfanas de preguntas que ya no existen.
        return repository.getExams().flatMap { it.questions }.mapNotNull { question ->
            val answer = answeredBy[question.id] ?: return@mapNotNull null
            val selected = answer.selectedOptionIndex
            if (selected !in question.options.indices) return@mapNotNull null
            AnswerResult(
                question = question,
                selectedOptionIndex = selected,
                isCorrect = question.isCorrect(selected),
            )
        }
    }
}
