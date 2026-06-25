# A2Madrid — Descripción del proyecto

App de test (cuestionario) sobre el examen oficial de oposición (Auxiliares C2, Comunidad de
Madrid). El usuario elige un modelo, responde preguntas de opción múltiple, confirma cada
respuesta con feedback inmediato (correcto/incorrecto + explicación) y al terminar ve un
resumen con su puntuación. Las puntuaciones se guardan en un historial consultable y editable.
No hay onboarding ni login.

**Estado:** en **migración a Kotlin Multiplatform** (Android · iOS · Web). **Android** corre en
emulador y **Web (wasmJs)** empaqueta y se sirve en navegador (Hitos 1 y 2 hechos). iOS pendiente.

Repo: https://github.com/LeonardoManuelMendez/A2Madrid · respaldo local en `PruebaA2MadridCopia`.

## Arquitectura

Clean Architecture + MVVM + **UDF** (Unidirectional Data Flow), ahora en un **módulo único
multiplataforma** (`composeApp`) con source sets. La regla de dependencias apunta hacia el
dominio: `presentation → domain ← data`. El dominio es Kotlin puro (sin Android, sin JSON,
sin almacenamiento concreto).

```
app/  (módulo composeApp · Kotlin Multiplatform + Compose Multiplatform)
└─ src/
   ├─ commonMain/kotlin/com/example/a2madrid/      ← código COMPARTIDO (Android/iOS/Web)
   │  ├─ App.kt                  raíz Compose (tema + host de navegación)
   │  ├─ di/Koin.kt              appModule + `expect val platformModule` + initKoin()
   │  ├─ domain/                 model/ · repository/QuizRepository · usecase/ (Get/Evaluate/Save/Observe/Delete/Clear)
   │  ├─ data/                   dto/ · mapper/ · source/ (ExamResourceDataSource, ScoreStorage) · repository/QuizRepositoryImpl
   │  └─ presentation/           navigation/ · examselection/ · quiz/ · result/ · scorehistory/ · theme/
   ├─ commonMain/composeResources/files/exams.json   (Compose Resources)
   ├─ androidMain/kotlin/...     MainActivity · A2MadridApplication (initKoin) · AndroidScoreStorage · platformModule (actual)
   ├─ androidMain/{AndroidManifest.xml, res/}
   ├─ iosMain/      (PENDIENTE)  IosScoreStorage (NSUserDefaults) · platformModule (actual) · MainViewController
   └─ wasmJsMain/   (HECHO)      WebScoreStorage (localStorage) · platformModule (actual) · main.kt (ComposeViewport) · resources/index.html
```

## Cómo interactúan las capas (flujo)

Las dependencias apuntan **hacia el dominio**. La UI depende del dominio (vía casos de uso) y
la capa de datos también (implementando su interfaz). El dominio no depende de nadie. **Koin**
es el "pegamento" que entrega las implementaciones donde se piden.

```
[PRESENTACIÓN]                 [DOMINIO]                    [DATOS]

 Composable (View)
   │  eventos (clics)
   ▼
 ViewModel ───llama───►  UseCase ───usa───►  QuizRepository (interfaz)
   ▲  StateFlow<UiState>                            ▲ implementa
   │                                       QuizRepositoryImpl
   │  la UI se recompone                     ├─ ExamResourceDataSource ──► composeResources/exams.json
   └──────────────────────────────          └─ ScoreStorage ──► (Android SharedPreferences ·
                                                                  iOS NSUserDefaults · Web localStorage)

   Koin (di/Koin.kt) inyecta la implementación allí donde se pide la interfaz.
   Los mappers (data/mapper) traducen DTO (datos) ⇄ modelo de dominio.
```

**Ruta de lectura recomendada** (una respuesta de principio a fin):
`ExamSelectionScreen → ExamSelectionViewModel → GetExamsUseCase → QuizRepository (interfaz) →
QuizRepositoryImpl → ExamResourceDataSource → exams.json`, y la vuelta: los datos suben como
`Exam` (mapeados desde DTO) hasta el `StateFlow` del ViewModel, que recompone la pantalla.

**Convenciones** (cada archivo lleva una leyenda de cabecera con su capa, rol y con quién habla):
- **View (Composable)**: "state down, events up"; sin lógica de negocio ni navegación propia.
- **ViewModel**: no conoce Compose; produce `StateFlow<UiState>` y llama a casos de uso (testeable con fake). Lo provee Koin (`koinViewModel()`).
- **UseCase**: una acción de negocio (`operator fun invoke`).
- **Repository**: interfaz en dominio (el "qué"), implementación en datos (el "cómo").
- **DTO ⇄ Modelo**: los DTO reflejan JSON/almacenamiento; los mappers aíslan el dominio.

## Funcionalidades

1. **Selección de modelo**: pantalla inicial con los modelos de `exams.json`. El examen oficial
   Orden 2411/2017 · Auxiliares C2 2018 (pregunta 20 excluida por anulada) está dividido en dos
   modelos: "Psicotécnico y ortografía" (1-45) y "Legislación" (46-90). Claves verificadas
   contra la plantilla oficial.
2. **Cuestionario interactivo**: pregunta + 4 opciones (A–D) con feedback inmediato por color y
   explicación; bloque de contexto (tabla/rejilla) en monoespaciado para los ítems que lo usan.
3. **Navegación secuencial** con barra de progreso.
4. **Reinicio controlado**: menú con "Puntuaciones"/"Reiniciar"; confirmación si hay progreso.
5. **Resumen final por modelo**: aciertos, %, aprobado/suspenso, récord y mejor marca del modelo.
6. **Historial de puntuaciones**: persistido por plataforma; lista por fecha, destaca la mejor
   marca de cada modelo, permite borrar una entrada o todo (con confirmación).

## Stack técnico

- **Lenguaje**: Kotlin Multiplatform (Kotlin 2.3.20 — requerido por el target web; ver Hito 2)
- **UI**: Compose Multiplatform 1.10.3 + Material 3 (paquetes `androidx.compose.*`), previews
- **Navegación**: Compose Navigation multiplataforma `org.jetbrains.androidx.navigation` 2.9.2 (rutas type-safe `@Serializable`)
- **DI**: Koin 4.2.2 (`koin-core`/`koin-compose`/`koin-compose-viewmodel`; `koin-android` en androidMain)
- **ViewModel/Lifecycle**: `org.jetbrains.androidx.lifecycle` 2.9.6
- **Asíncrono**: Coroutines + Flow / StateFlow
- **Persistencia**: `ScoreStorage` (interfaz común) → SharedPreferences / NSUserDefaults / localStorage
- **Datos de exámenes**: JSON vía **Compose Resources** (`composeResources/files/exams.json`)
- **Fecha/hora**: kotlinx-datetime 0.6.2 (sin `java.*`)
- **Tests**: `kotlin.test` en `commonTest` (11 tests verdes con `:app:testDebugUnitTest`)

## Migración a KMP — estado

**Decisión**: módulo único `composeApp`, Koin, Compose Navigation multiplataforma, targets
Android + iOS + Web (wasmJs).

**Hito 1 — Android sobre KMP (HECHO, commit `f5057f6`, pusheado):**
- Build a `kotlin.multiplatform` + `org.jetbrains.compose` 1.9.3.
- Fuentes a `commonMain`/`androidMain`; `exams.json` a Compose Resources.
- Hilt → Koin; Navigation 3 → Compose Navigation MP; assets → Compose Resources;
  DataStore → `ScoreStorage` (actual Android con SharedPreferences); `System`/`java.*` → kotlinx-datetime.
- Tema sin color dinámico (era exclusivo de Android).
- `./gradlew :app:assembleDebug` ✅ (APK ~20 MB).
- **Tests portados a `kotlin.test`** en `commonTest`: se eliminó el `MainDispatcherRule` de
  JUnit y se usa `Dispatchers.setMain`/`resetMain` en `@BeforeTest`/`@AfterTest`; aserciones
  `org.junit.Assert.*` → `kotlin.test.*`. `:app:testDebugUnitTest` ✅ (11 tests, 0 fallos).

**Hito 2 — Web (wasmJs) (HECHO):**
- Target `wasmJs { outputModuleName="a2madrid"; browser{}; binaries.executable() }`; dep
  `kotlinx-browser` 0.3.1 (localStorage/document); `WebScoreStorage` (localStorage),
  `platformModule` (actual wasmJs), `main.kt` (`ComposeViewport(document.body!!) { App() }`) y
  `wasmJsMain/resources/index.html` (carga `a2madrid.js`).
- **Bump obligado de versiones**: el ecosistema wasm (koin/navigation/lifecycle/compose) ya está
  en la era Kotlin 2.3, y sus klibs wasm arrastran `kotlin-stdlib` a **2.3.20** por constraint.
  Compilar esos klibs con un compilador 2.2.x rompe el plugin de kotlinx-serialization (ICE:
  NPE en `createDeprecatedHiddenAnnotation`, `null cast a FirRegularClassSymbol`). Forzar el
  stdlib a 2.2.x rompe Koin (sus klibs exigen ≥2.3.20). Solución: **Kotlin 2.3.20 + Compose MP
  1.10.3** (compilador ≥ stdlib). Android sigue verde tras el bump (APK + 11 tests).
- `settings.gradle.kts`: quitado `repositoriesMode = FAIL_ON_PROJECT_REPOS` (default
  PREFER_PROJECT) porque el plugin de Node/Wasm añade el repo `nodejs.org/dist` a nivel proyecto.
- Verificado: `:app:compileKotlinWasmJs` ✅ y `:app:wasmJsBrowserDistribution` ✅ (dist en
  `app/build/dist/wasmJs/productionExecutable/`: index.html + a2madrid.js + .wasm + exams.json).
  Servir/probar: `./gradlew :app:wasmJsBrowserDevelopmentRun` (o `wasmJsBrowserRun`).

**Pendiente:**
- [ ] **Hito 3 — iOS**: targets `iosX64/iosArm64/iosSimulatorArm64`, `IosScoreStorage`
      (NSUserDefaults), `platformModule` (actual iOS) y `MainViewController` (`ComposeUIViewController`)
      + proyecto Xcode `iosApp`. **Solo compila en macOS/Xcode** (no verificable en este Linux).

## Notas de build

- **AGP 9 + KMP**: `com.android.application` + `kotlin.multiplatform` no son compatibles en AGP 9
  sin el workaround oficial: en `gradle.properties` → `android.builtInKotlin=false` y
  `android.newDsl=false` (por eso el bloque `android {}` usa el DSL clásico). `compileSdk = 37`.
- **JDK completo (jlink) + config-cache off** ya fijados, sin flags por comando: `jlink` lo
  necesita el `JdkImageTransform` de AGP (compileSdk 37) y el JRE de la extensión Java de VSCode
  no lo trae → `org.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64` en
  `~/.gradle/gradle.properties` (no commiteado). La configuration-cache es incompatible con ese
  transform (`JdkImageInput` no serializable) → `org.gradle.configuration-cache=false` en el
  `gradle.properties` del proyecto. Así `./gradlew :app:assembleDebug` funciona sin `JAVA_HOME`
  ni `--no-configuration-cache`. En Android Studio, fijar *Gradle JDK* a la JBR si se queja de jlink.
