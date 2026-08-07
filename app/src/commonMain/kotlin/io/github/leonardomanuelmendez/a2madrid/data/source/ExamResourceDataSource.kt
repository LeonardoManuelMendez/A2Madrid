/*
 * ══ CAPA DE DATOS · DataSource (multiplataforma) ══
 * Lee los exámenes desde Compose Resources (commonMain/composeResources/files/exams.json),
 * que funciona en Android, iOS y Web sin APIs específicas de plataforma. Devuelve DTOs.
 */
package io.github.leonardomanuelmendez.a2madrid.data.source

import io.github.leonardomanuelmendez.a2madrid.data.dto.ExamsFileDto
import io.github.leonardomanuelmendez.a2madrid.resources.Res
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

class ExamResourceDataSource(private val json: Json) {

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadExams(): ExamsFileDto {
        val bytes = Res.readBytes("files/exams.json")
        return json.decodeFromString(ExamsFileDto.serializer(), bytes.decodeToString())
    }
}
