/*
 * ══ PUNTO DE ENTRADA · Application (Android) ══
 * Arranca Koin al iniciar el proceso (raíz del grafo de inyección). Le pasa el Context de la
 * app (para SharedPreferences) y el logger de Android. Declarada en el AndroidManifest.
 */
package com.example.a2madrid

import android.app.Application
import com.example.a2madrid.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class A2MadridApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@A2MadridApplication)
        }
    }
}
