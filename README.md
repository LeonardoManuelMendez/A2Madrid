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
- **Plataformas**: Android ✅ · Web/wasmJs ✅ · iOS ✅ (código + framework verificado en CI; el
  `.xcodeproj` ejecutable se genera en un Mac).
- **CI**: GitHub Actions (`.github/workflows/ci.yml`) — Android (build+test), Web (compile) e
  iOS (framework en runner macOS).



Hecho con 🤍 por [Leonardo Manuel Méndez](https://leonardomanuelmendez.github.io/).