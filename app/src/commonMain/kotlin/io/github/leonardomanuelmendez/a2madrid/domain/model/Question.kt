/*
 * ╔══ CAPA DE DOMINIO · Modelo (Entidad de negocio) ══╗
 * Kotlin puro: sin Android, sin JSON, sin DataStore. Es el "Model" de MVVM y
 * el núcleo de Clean Architecture (no depende de ninguna otra capa).
 * Lo CREAN los mappers de la capa de DATOS y lo CONSUMEN los UseCase y el ViewModel.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.model

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
    /** Why [correctAnswer] is the right one; shown once the user confirms an answer. */
    val explanation: Explanation? = null,
    /** Optional shared stimulus (table, grid…) shown verbatim in a monospace block. */
    val context: String? = null,
    /**
     * True when the options refer to each other («la respuesta b) es correcta», «todas las
     * anteriores») and therefore must keep the order they were written in. Shuffling them would
     * leave the reference dangling. It cannot be inferred from the text with any reliability, so
     * it travels in the data.
     */
    val lockOptionOrder: Boolean = false,
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