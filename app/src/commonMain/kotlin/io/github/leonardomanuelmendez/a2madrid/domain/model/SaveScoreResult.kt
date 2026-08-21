/*
 * ══ CAPA DE DOMINIO · Modelo ══
 * Lo que devuelve SaveScoreUseCase: la entrada REALMENTE persistida (con su timestamp, que es
 * el identificador con el que después se vuelve a localizar el intento para pintar el desglose
 * o repasar sus fallos) más si esa marca es récord del modelo.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.model

/** Outcome of persisting a finished attempt. */
data class SaveScoreResult(
    val entry: ScoreEntry,
    val isNewBestScore: Boolean,
)
