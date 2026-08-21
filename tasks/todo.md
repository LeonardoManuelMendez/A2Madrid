# TODO: Memoria de respuestas y repaso de fallos

Plan: [`tasks/plan.md`](plan.md) · Spec: [`docs/spec_repaso_fallos.md`](../docs/spec_repaso_fallos.md)

Regla de ejecución: TDD. Test rojo → código mínimo → verde → siguiente.
Comando de verificación en todas las tareas salvo indicación contraria:
`./gradlew :app:testDebugUnitTest`

---

## Slice 1 · El dato existe y se persiste

- [x] **Tarea 1: Modelo de dominio y persistencia de respuestas**
  - **Aceptación:**
    * Nuevo `AnsweredQuestion(questionId: Int, selectedOptionIndex: Int)` en `domain/model`.
    * `ScoreEntry` gana `answers: List<AnsweredQuestion> = emptyList()` e `isReview: Boolean = false`.
    * `ScoreEntryDto` refleja ambos campos con el mismo valor por defecto; `ScoreMapper` los
      traduce en los dos sentidos.
    * Un JSON con la forma **anterior** (sin `answers` ni `isReview`) se deserializa sin error.
  - **Verificación:** `ScoreMapperTest` nuevo, con un caso de ida y vuelta y un caso de JSON
    heredado. Suite completa en verde.
  - **Archivos:** `domain/model/AnsweredQuestion.kt` (nuevo), `domain/model/ScoreEntry.kt`,
    `data/dto/ScoreEntryDto.kt`, `data/mapper/ScoreMapper.kt`,
    `commonTest/data/ScoreMapperTest.kt` (nuevo)

- [x] **Tarea 2: `SaveScoreUseCase` guarda las respuestas y distingue los repasos**
  - **Aceptación:**
    * Acepta `answers: List<AnsweredQuestion>` e `isReview: Boolean = false`.
    * Devuelve `SaveScoreResult(entry, isNewBestScore)` en vez de `Boolean`, para que quien
      llama conozca el `timestampMillis` del intento recién guardado.
    * El cálculo de mejor marca ignora las entradas con `isReview = true`.
  - **Verificación:** `SaveScoreUseCaseTest` ampliado: las respuestas llegan al repositorio, y un
    repaso con pleno de aciertos no cuenta como récord.
  - **Archivos:** `domain/usecase/SaveScoreUseCase.kt`, `commonTest/domain/SaveScoreUseCaseTest.kt`

## Slice 2 · El test produce el dato

- [x] **Tarea 3: `QuizUiState` y `QuizViewModel` acumulan las respuestas**
  - **Aceptación:**
    * `QuizUiState` gana `answers: List<AnswerResult>`, y `correctAnswers` pasa a derivarse de
      ella (una sola fuente de verdad, en vez de un contador paralelo).
    * `QuizUiState` expone `wrongAnswers` para quien lo necesite.
    * `confirmAnswer()` añade el `AnswerResult` al estado en vez de descartarlo.
    * `finishQuiz()` entrega las respuestas a `SaveScoreUseCase` y guarda el `attemptMillis`
      resultante en `QuizResult`.
  - **Verificación:** `QuizViewModelTest` ampliado: un test completo deja tantas respuestas como
    preguntas, con el índice elegido correcto en cada una; los tests existentes siguen pasando.
  - **Archivos:** `presentation/quiz/QuizUiState.kt`, `presentation/quiz/QuizViewModel.kt`,
    `domain/model/QuizResult.kt`, `commonTest/presentation/QuizViewModelTest.kt`

## Slice 3 · El usuario lo ve

- [x] **Tarea 4: `GetAttemptReviewUseCase` reconstruye el detalle del intento**
  - **Aceptación:**
    * Dado `examId` y `attemptMillis`, cruza las `answers` guardadas con las preguntas del examen
      y devuelve `List<AnswerResult>` en el orden original del examen.
    * Un intento inexistente, o uno antiguo sin respuestas, devuelve lista vacía (no lanza).
    * Registrado en `di/Koin.kt`.
  - **Verificación:** `GetAttemptReviewUseCaseTest` nuevo.
  - **Archivos:** `domain/usecase/GetAttemptReviewUseCase.kt` (nuevo), `di/Koin.kt`,
    `commonTest/domain/GetAttemptReviewUseCaseTest.kt` (nuevo)

- [x] **Tarea 5: Desglose pregunta a pregunta en la pantalla de resultado**
  - **Aceptación:**
    * `ResultRoute` gana `attemptMillis: Long`; el host de navegación lo propaga desde el test.
    * `ResultViewModel` expone el desglose del intento.
    * `ResultScreen` lo pinta en un `LazyColumn`: número, ✓/✗, opción elegida, opción correcta y
      la explicación existente. Un intento sin respuestas guardadas no muestra la sección.
  - **Verificación:** compilación + ejecución real de la app; terminar un test y comprobar el
    desglose contra las respuestas dadas.
  - **Archivos:** `presentation/navigation/Routes.kt`,
    `presentation/navigation/A2MadridNavDisplay.kt`, `presentation/quiz/QuizScreen.kt`,
    `presentation/result/ResultViewModel.kt`, `presentation/result/ResultScreen.kt`

## Slice 4 · El usuario lo usa

- [x] **Tarea 6: Modo repaso en el test**
  - **Aceptación:**
    * `QuizViewModel.loadReview(examId, attemptMillis)` carga el examen filtrado a las preguntas
      falladas en ese intento, conservando el orden original.
    * El estado marca la sesión como repaso; al terminar se guarda con `isReview = true`.
    * Si no quedan fallos, el estado lo refleja en vez de abrir un test vacío.
  - **Verificación:** `QuizViewModelTest` ampliado: el repaso carga solo las falladas y no genera
    récord.
  - **Archivos:** `presentation/quiz/QuizUiState.kt`, `presentation/quiz/QuizViewModel.kt`,
    `commonTest/presentation/QuizViewModelTest.kt`

- [x] **Tarea 7: Entradas al repaso desde resultado e historial**
  - **Aceptación:**
    * `ReviewRoute(examId, examTitle, attemptMillis)` nueva; `QuizScreen` acepta el modo repaso
      como parámetro de composable.
    * `ResultScreen` muestra «Repasar los N fallos» cuando N > 0.
    * `ScoreHistoryScreen` etiqueta los repasos como tales y permite repasar los fallos de un
      intento anterior.
  - **Verificación:** ejecución real: repasar desde el resultado, y repasar un intento anterior
    tras cerrar y reabrir la app.
  - **Archivos:** `presentation/navigation/Routes.kt`,
    `presentation/navigation/A2MadridNavDisplay.kt`, `presentation/quiz/QuizScreen.kt`,
    `presentation/result/ResultScreen.kt`, `presentation/scorehistory/ScoreHistoryScreen.kt`

## Cierre

- [x] **Tarea 8: Verificación multiplataforma y documentación**
  - **Aceptación:** `:app:testDebugUnitTest` y `:app:compileKotlinWasmJs` en verde; `README.md`
    menciona el repaso de fallos si procede.
  - **Verificación:** ambos comandos de Gradle.
  - **Archivos:** `README.md`

---

## Verificado en dispositivo

Checkpoints 3 y 4 del plan, comprobados el 21-08-2026 en un emulador Pixel_8 (Android headless,
build de depuración) sembrando el historial directamente en SharedPreferences para no tener que
contestar 45 preguntas a mano:

- [x] Un historial en el formato ANTERIOR (sin `answers`) se lee sin fallar y no ofrece repaso.
- [x] «Repasar 2 fallos» abre un test con exactamente las dos preguntas falladas, en el orden del
      examen y con la cabecera «Repaso · …».
- [x] El desglose despliega «Tu respuesta» en rojo, «Correcta» en verde y la explicación completa
      con su artículo citado.
- [x] El repaso se guarda etiquetado «Repaso», sin estrella: no roba el récord del modelo.
- [x] Desde el repaso se encadena otro («Repasar 1 fallo»).
