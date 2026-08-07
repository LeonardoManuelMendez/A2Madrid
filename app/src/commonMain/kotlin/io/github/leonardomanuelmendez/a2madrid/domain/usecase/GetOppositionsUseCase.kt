/*
 * ══ CAPA DE DOMINIO · UseCase (Caso de uso / Interactor) ══
 * Encapsula la acción de negocio para obtener la lista de oposiciones disponibles.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.usecase

import io.github.leonardomanuelmendez.a2madrid.domain.model.Opposition
import io.github.leonardomanuelmendez.a2madrid.domain.repository.QuizRepository

/** Retrieves the list of opposition categories. */
class GetOppositionsUseCase constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(): List<Opposition> = repository.getOppositions()
}
