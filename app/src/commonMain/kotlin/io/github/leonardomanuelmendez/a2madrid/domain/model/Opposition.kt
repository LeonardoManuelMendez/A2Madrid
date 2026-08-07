/*
 * ══ CAPA DE DOMINIO · Modelo ══
 * Modelo de datos puro que representa una oposición.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.model

/**
 * Represents a study opposition category.
 *
 * @param id Unique identifier.
 * @param name The descriptive name of the opposition.
 * @param isActive True if it has available content and exams ready to be taken.
 */
data class Opposition(
    val id: String,
    val name: String,
    val isActive: Boolean
)
