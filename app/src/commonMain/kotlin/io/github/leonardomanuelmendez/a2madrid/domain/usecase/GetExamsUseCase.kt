/*
 * ══ CAPA DE DOMINIO · UseCase (Caso de uso / Interactor) ══
 * Encapsula UNA acción de negocio y se expone como función invocable (operator
 * invoke). El ViewModel llama a casos de uso en lugar de tocar el repositorio
 * directamente: así la lógica de negocio vive fuera de la UI y es fácil de testear.
 * Este: obtiene todos los modelos de examen → lo usa ExamSelectionViewModel.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.usecase

import io.github.leonardomanuelmendez.a2madrid.domain.model.Exam
import io.github.leonardomanuelmendez.a2madrid.domain.repository.QuizRepository

/** Retrieves every available exam model (for the selection screen). */
class GetExamsUseCase constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(): List<Exam> = repository.getExams()
}