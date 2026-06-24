/*
 * ══ CAPA DE DOMINIO · Modelo (Agregado) ══
 * Un examen = título + su lista de Question. Kotlin puro.
 * Lo devuelve el repositorio (vía GetExams/GetExamUseCase) y lo consumen
 * ExamSelectionViewModel (para listar) y QuizViewModel (para jugar).
 */
package com.example.a2madrid.domain.model

/** An exam model: a named set of questions the user can choose to take. */
data class Exam(
    val id: String,
    val title: String,
    val questions: List<Question>,
) {
    val questionCount: Int get() = questions.size
}