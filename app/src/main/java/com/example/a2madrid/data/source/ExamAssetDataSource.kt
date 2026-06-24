/*
 * ══ CAPA DE DATOS · DataSource (fuente concreta) ══
 * Sabe leer UNA fuente: el fichero assets/exams.json. Lee el texto en el dispatcher
 * de IO y lo deserializa a DTOs con kotlinx.serialization. Devuelve DTOs, no dominio:
 * el repositorio se encarga de mapearlos. Hilt lo crea por @Inject e inyecta sus deps.
 */
package com.example.a2madrid.data.source

import android.content.Context
import com.example.a2madrid.data.dto.ExamsFileDto
import com.example.a2madrid.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** Reads and parses the bundled exam models from the app assets. */
class ExamAssetDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    suspend fun loadExams(): ExamsFileDto = withContext(dispatcher) {
        val raw = context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }
        json.decodeFromString(ExamsFileDto.serializer(), raw)
    }

    private companion object {
        const val FILE_NAME = "exams.json"
    }
}