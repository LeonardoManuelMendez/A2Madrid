# A2Madrid — Descripción del proyecto

App Android de test tipo cuestionario sobre Madrid preparada para varios modelos de
examen. El usuario elige un modelo, responde preguntas de opción múltiple, confirma cada
respuesta con feedback inmediato (correcto/incorrecto + explicación) y al terminar ve un
resumen con su puntuación. Las puntuaciones se guardan en un historial consultable y
editable. No hay onboarding ni login.

## Arquitectura

Clean Architecture + MVVM en un único módulo, organizado por capas. La regla de
dependencias apunta hacia dentro: `presentation → domain ← data`. El dominio es Kotlin
puro (sin Android, sin JSON, sin DataStore).

```
com.example.a2madrid
├─ A2MadridApplication        @HiltAndroidApp
├─ MainActivity               @AndroidEntryPoint · edge-to-edge · host de Navigation 3
├─ di/                        Hilt: AppModule (Json, @IoDispatcher), RepositoryModule (@Binds)
├─ domain/
│  ├─ model/                  Exam, Question, AnswerResult, QuizResult, ScoreEntry
│  ├─ repository/             QuizRepository (interfaz)
│  └─ usecase/                GetExams, GetExam, EvaluateAnswer, SaveScore,
│                             ObserveScoreHistory, DeleteScore, ClearScores
├─ data/
│  ├─ dto/                    ExamsFileDto, ExamDto, QuestionDto, ScoreEntryDto (@Serializable)
│  ├─ mapper/                 DTO → dominio
│  ├─ source/                 ExamAssetDataSource (assets/exams.json),
│  │                          ScorePreferencesDataSource (DataStore)
│  └─ repository/             QuizRepositoryImpl
└─ presentation/
   ├─ navigation/             Routes (NavKey) + A2MadridNavDisplay (NavDisplay)
   ├─ examselection/          ExamSelectionViewModel, ExamSelectionUiState, ExamSelectionScreen
   ├─ quiz/                   QuizViewModel, QuizUiState, QuizScreen, components/
   ├─ result/                 ResultViewModel, ResultScreen
   ├─ scorehistory/           ScoreHistoryViewModel, ScoreHistoryScreen
   └─ theme/                  Tema M3 (carmesí Madrid) + colores de feedback
```

## Cómo interactúan las capas (flujo)

La regla de oro: las dependencias apuntan **hacia el dominio**. La UI depende del dominio
(vía casos de uso) y la capa de datos también (implementando su interfaz). El dominio no
depende de nadie. Hilt es el "pegamento" que entrega las implementaciones donde se piden.

```
[PRESENTACIÓN]                 [DOMINIO]                    [DATOS]

 Composable (View)
   │  eventos (clics)
   ▼
 ViewModel ───llama───►  UseCase ───usa───►  QuizRepository (interfaz)
   ▲  StateFlow<UiState>                            ▲ implementa
   │                                       QuizRepositoryImpl
   │  la UI se recompone                     ├─ ExamAssetDataSource ──► assets/exams.json
   └──────────────────────────────          └─ ScorePreferencesDataSource ──► DataStore

        Hilt (di/) inyecta la implementación allí donde se pide la interfaz.
        Los mappers (data/mapper) traducen DTO (datos) ⇄ modelo de dominio.
```

**Ruta de lectura recomendada** (una respuesta de principio a fin):

`ExamSelectionScreen` → `ExamSelectionViewModel` → `GetExamsUseCase` →
`QuizRepository` (interfaz) → `QuizRepositoryImpl` → `ExamAssetDataSource` → `exams.json`,
y la vuelta: los datos suben como `Exam` (mapeados desde DTO) hasta el `StateFlow` del
ViewModel, que hace que la pantalla se recomponga.

**Convenciones que se repiten en el código** (cada archivo lleva una leyenda de cabecera
que indica su capa, su rol y con quién habla):

- **View (Composable)**: "state down, events up" — recibe estado y emite eventos; no tiene
  lógica de negocio ni navega por sí misma (de eso se encarga `A2MadridNavDisplay`).
- **ViewModel**: no conoce Compose ni Android UI; solo produce `StateFlow<UiState>` y llama
  a casos de uso. Por eso es testeable con un repositorio fake (ver `QuizViewModelTest`).
- **UseCase**: una acción de negocio única (`operator fun invoke`); aísla la lógica de la UI.
- **Repository**: interfaz en el dominio (el "qué"), implementación en datos (el "cómo").
- **DTO ⇄ Modelo**: los DTO reflejan el JSON/almacenamiento; los mappers los convierten para
  que los detalles externos no se filtren al dominio.

> El mismo diagrama está al inicio de `domain/repository/QuizRepository.kt`, y el mapa de
> navegación entre pantallas, al inicio de `presentation/navigation/A2MadridNavDisplay.kt`.

## Funcionalidades

1. **Selección de modelo**: pantalla inicial con los modelos disponibles desde
   `assets/exams.json`. El examen oficial Orden 2411/2017 · Auxiliares C2 2018 (pregunta 20
   excluida por figurar anulada en la plantilla) está dividido en dos modelos siguiendo su
   estructura oficial: "Psicotécnico y ortografía" (preguntas 1-45) y "Legislación"
   (preguntas 46-90). Claves verificadas contra la plantilla oficial.
2. **Cuestionario interactivo**: pregunta + 4 opciones (Material 3). Selección, confirmación
   y feedback inmediato por color (correcto = verde, incorrecto = error) con explicación.
3. **Navegación secuencial**: avanza pregunta a pregunta hasta el final; barra de progreso.
4. **Reinicio controlado**: el menú del quiz permite ver puntuaciones y reiniciar; si hay
   progreso, se pide confirmación antes de perderlo.
5. **Resumen final por modelo**: muestra título del examen, aciertos, porcentaje,
   aprobado/suspenso, aviso de récord y mejor marca dentro de ese mismo modelo.
6. **Historial de puntuaciones**: cada prueba terminada se persiste (DataStore) con
   `examId` y `examTitle`. Es accesible desde la selección, el quiz y el resumen. La lista
   aparece ordenada por fecha, muestra el modelo de examen, destaca la mejor marca de cada
   modelo y permite borrar una entrada o limpiar todo el historial con confirmación.

## Stack técnico

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose + Material 3, edge-to-edge, tema claro/oscuro + previews
- **Navegación**: Jetpack Navigation 3 (back stack serializable, ViewModels con scope por destino)
- **DI**: Hilt
- **Asíncrono**: Coroutines + Flow / StateFlow
- **Persistencia**: Preferences DataStore (historial serializado con kotlinx.serialization)
- **Datos de exámenes**: JSON en `assets/exams.json`
- **Tests**: JUnit4 + coroutines-test (casos de uso y QuizViewModel con repositorio fake)

## Notas de build

- `compileSdk = 37` (requerido por las dependencias de AndroidX del Compose BOM 2026.06);
  `targetSdk = 36`, `minSdk = 24`.
- Si se compila desde la terminal, usar un JDK completo (con `jlink`):
  `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :app:assembleDebug`.
- Verificación reciente: `./gradlew :app:compileDebugKotlin` y `./gradlew testDebugUnitTest`
  pasan correctamente.
