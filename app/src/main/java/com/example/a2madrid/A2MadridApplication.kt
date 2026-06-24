/*
 * ══ PUNTO DE ENTRADA · Application ══
 * @HiltAndroidApp arranca el contenedor de dependencias de Hilt, que vive durante todo el
 * proceso: es la RAÍZ del grafo de inyección (sin ella nada de @Inject/@HiltViewModel funciona).
 * Se declara en el AndroidManifest como android:name.
 */
package com.example.a2madrid

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Annotated with [HiltAndroidApp] so Hilt can generate the
 * dependency container that lives for the whole process lifecycle.
 */
@HiltAndroidApp
class A2MadridApplication : Application()