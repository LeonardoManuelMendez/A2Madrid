/*
 * ══ CAPA DE DATOS · DTO (Data Transfer Object) ══
 * Refleja EXACTAMENTE la forma del JSON en disco (assets/exams.json). Se separa
 * del modelo de dominio para que un cambio de formato externo no contamine el dominio.
 * Un mapper (data/mapper) lo traduce a Question. @Serializable = kotlinx.serialization.
 */
package com.example.a2madrid.data.dto

import kotlinx.serialization.Serializable

/** Serialized shape of a question as stored on disk. Kept separate from the domain model. */
@Serializable
data class QuestionDto(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String? = null,
    /** Key into [ExamDto.contexts] for a shared stimulus, if any. */
    val contextId: String? = null,
)