/*
 * ══ CAPA DE PRESENTACIÓN · Layout responsive (multiplataforma) ══
 * Constante de ancho máximo del contenido. En móvil no se nota (la pantalla es más estrecha),
 * pero en web/escritorio/tablet evita que botones, tarjetas y texto se estiren a todo el ancho
 * (sensación de "sin diseñar"). Cada pantalla centra su cuerpo y lo limita a este ancho, dejando
 * las barras (TopAppBar / bottomBar) a ancho completo.
 */
package io.github.leonardomanuelmendez.a2madrid.presentation

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Ancho máximo legible para el contenido principal de cada pantalla. */
val ContentMaxWidth: Dp = 640.dp
