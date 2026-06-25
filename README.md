# A2Madrid 📝

App de tests para preparar la oposición de **Auxiliar C2 de la Comunidad de Madrid**: eliges un
modelo, respondes preguntas de opción múltiple, recibes corrección y explicación al instante, y al
terminar ves tu puntuación con un historial de marcas.

> 🎁 **Esto es un regalo** para todas las personas que están preparando esta oposición. Úsalo
> libremente: sin registro, sin anuncios y sin coste.

Además forma parte de **mi portafolio** como desarrollador
([leonardomanuelmendez.github.io](https://leonardomanuelmendez.github.io/)): está construido con
**Kotlin Multiplatform + Compose Multiplatform** para demostrar una misma base de código corriendo
en **web, iOS y Android**.

## Cómo usarla

- **Web**: ábrela en el navegador (cualquier dispositivo: móvil, tablet u ordenador).
- **Android**: descarga el APK e instálalo directamente, sin pasar por Google Play.

(Los enlaces de descarga y la web se publican en la landing del proyecto.)

## Tecnología

- **Kotlin Multiplatform** (Kotlin 2.3.20) · módulo único `composeApp` con source sets
  `commonMain` / `androidMain` / `wasmJsMain`.
- **Compose Multiplatform 1.10.3** + Material 3 (UI 100 % compartida entre plataformas).
- **Arquitectura**: Clean Architecture + MVVM + UDF (`presentation → domain ← data`).
- **Navegación**: Compose Navigation multiplataforma (rutas type-safe).
- **DI**: Koin · **Asíncrono**: Coroutines/Flow.
- **Persistencia** (`ScoreStorage`): SharedPreferences (Android) · NSUserDefaults (iOS) ·
  localStorage (Web).
- **Plataformas**: Android ✅ · Web/wasmJs ✅ · iOS 🚧 (pendiente).

## Desarrollo

```bash
# Android (APK debug)
./gradlew :app:assembleDebug

# Tests
./gradlew :app:testDebugUnitTest

# Web en local (dev server con http://localhost:8080)
./gradlew :app:wasmJsBrowserDevelopmentRun
```

## Publicar la web (Firebase Hosting)

El sitio se aloja en un **proyecto de Firebase propio** (separado de otros proyectos). Una vez
creado el proyecto y elegido con `firebase use <project-id>`:

```bash
./scripts/deploy-web.sh
```

El script compila la web de producción, ensambla `build/site/` (landing en `/` + app en `/app/`)
y despliega a Firebase Hosting. Para habilitar el botón de descarga de Android, coloca el APK
firmado en `landing/a2madrid.apk`.

---

Hecho con 🤍 por [Leonardo Manuel Méndez](https://leonardomanuelmendez.github.io/).