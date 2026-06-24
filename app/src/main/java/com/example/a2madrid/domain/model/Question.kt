/*
 * ╔══ CAPA DE DOMINIO · Modelo (Entidad de negocio) ══╗
 * Kotlin puro: sin Android, sin JSON, sin DataStore. Es el "Model" de MVVM y
 * el núcleo de Clean Architecture (no depende de ninguna otra capa).
 * Lo CREAN los mappers de la capa de DATOS y lo CONSUMEN los UseCase y el ViewModel.
 */
package com.example.a2madrid.domain.model

/**
 * A single multiple-choice question.
 *
 * Pure domain model: it has no knowledge of how it is stored, serialized or displayed.
 */
data class Question(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String? = null,
    /** Optional shared stimulus (table, grid…) shown verbatim in a monospace block. */
    val context: String? = null,
) {
    init {
        require(options.size >= 2) { "A question needs at least two options" }
        require(correctAnswerIndex in options.indices) {
            "correctAnswerIndex=$correctAnswerIndex is out of bounds for ${options.size} options"
        }
    }

    val correctAnswer: String get() = options[correctAnswerIndex]

    fun isCorrect(optionIndex: Int): Boolean = optionIndex == correctAnswerIndex
}