/*
 * ══ CAPA DE DOMINIO · UseCase ══
 * Regla de estudio PURA (no usa repositorio): cada sesión presenta las mismas preguntas en otro
 * orden y con las opciones en otra posición, para que acertar exija reconocer el contenido y no
 * recordar «era la tercera».
 * → lo usa QuizViewModel al cargar un test o un repaso.
 *
 * Dos restricciones que no son negociables:
 *   · Las preguntas que comparten estímulo (una rejilla, una tabla) se mueven EN BLOQUE. Están
 *     contiguas en el examen y dispersarlas obligaría a releer el mismo estímulo cinco veces.
 *   · Las preguntas marcadas con `lockOptionOrder` conservan sus opciones tal cual: las suyas se
 *     refieren unas a otras («la respuesta b)», «todas las anteriores») y barajarlas las rompe.
 */
package io.github.leonardomanuelmendez.a2madrid.domain.usecase

import io.github.leonardomanuelmendez.a2madrid.domain.model.Question
import io.github.leonardomanuelmendez.a2madrid.domain.model.ShuffledQuestions
import kotlin.random.Random

/** Produces one session's presentation order for a set of questions. */
class ShuffleQuestionsUseCase(
    private val random: Random = Random.Default,
) {
    operator fun invoke(questions: List<Question>): ShuffledQuestions = ShuffledQuestions(
        questions = questions.toBlocks()
            .shuffled(random)
            .flatMap { block -> if (block.size > 1) block.shuffled(random) else block },
        optionOrder = questions.associate { question ->
            question.id to if (question.lockOptionOrder) {
                question.options.indices.toList()
            } else {
                question.options.indices.shuffled(random)
            }
        },
    )

    /**
     * Splits the questions into movable units: a run of consecutive questions sharing the same
     * stimulus is one block, and every other question is a block of its own.
     */
    private fun List<Question>.toBlocks(): List<List<Question>> {
        val blocks = mutableListOf<MutableList<Question>>()
        forEach { question ->
            val current = blocks.lastOrNull()
            val continuesBlock = question.context != null && current?.last()?.context == question.context
            if (continuesBlock) current.add(question) else blocks.add(mutableListOf(question))
        }
        return blocks
    }
}
