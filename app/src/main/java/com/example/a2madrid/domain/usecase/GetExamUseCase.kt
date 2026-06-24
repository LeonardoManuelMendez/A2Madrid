/*
 * ══ CAPA DE DOMINIO · UseCase ══
 * Devuelve UN examen por su id (o null si no existe).
 * → lo usa QuizViewModel al abrir un test concreto.
 */
package com.example.a2madrid.domain.usecase

import com.example.a2madrid.domain.model.Exam
import com.example.a2madrid.domain.repository.QuizRepository
import javax.inject.Inject

/** Retrieves a single exam model by id, or `null` if it does not exist. */
class GetExamUseCase @Inject constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(examId: String): Exam? =
        repository.getExams().firstOrNull { it.id == examId }
}