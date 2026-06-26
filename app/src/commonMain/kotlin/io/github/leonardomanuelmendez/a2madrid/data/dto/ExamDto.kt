/*
 * ══ CAPA DE DATOS · DTO ══
 * Forma serializada de un examen y del fichero raíz (ExamsFileDto = lo que parsea
 * ExamAssetDataSource). `contexts` guarda los estímulos compartidos (tabla, rejilla)
 * una sola vez; cada QuestionDto los referencia por `contextId`. ExamMapper lo pasa a Exam.
 */
package io.github.leonardomanuelmendez.a2madrid.data.dto

import kotlinx.serialization.Serializable

/** Root object of `assets/exams.json`. */
@Serializable
data class ExamsFileDto(
    val exams: List<ExamDto>,
)

/** Serialized shape of an exam model. */
@Serializable
data class ExamDto(
    val id: String,
    val title: String,
    /** Shared stimuli (tables, grids…) referenced by questions via [QuestionDto.contextId]. */
    val contexts: Map<String, String> = emptyMap(),
    val questions: List<QuestionDto>,
)
