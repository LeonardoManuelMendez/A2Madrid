# TODO: Barajado de preguntas y opciones

Plan: [`tasks/plan.md`](plan.md) · Spec: [`docs/spec_barajado.md`](../docs/spec_barajado.md)

Regla de ejecución: TDD. Test rojo → código mínimo → verde → siguiente.
Verificación por defecto: `./gradlew :app:testDebugUnitTest`

---

## Slice 1 · El barajado existe y respeta las reglas

- [x] **Tarea 1: Marcar en el dato las preguntas que no admiten barajado de opciones**
  - **Aceptación:**
    * `QuestionDto` y `Question` ganan `lockOptionOrder: Boolean = false`.
    * `QuestionMapper` lo traslada.
    * En `exams.json`, las preguntas 47 y 88 quedan marcadas con `"lockOptionOrder": true`.
  - **Verificación:** `QuestionMapperTest` nuevo (viaja el campo, por defecto `false`), más una
    comprobación de que en el JSON hay exactamente 2 preguntas marcadas.
  - **Archivos:** `data/dto/QuestionDto.kt`, `data/mapper/QuestionMapper.kt`,
    `domain/model/Question.kt`, `composeResources/files/exams.json`,
    `commonTest/data/QuestionMapperTest.kt` (nuevo)

- [x] **Tarea 2: `ShuffleQuestionsUseCase`**
  - **Aceptación:**
    * Devuelve `ShuffledQuestions(questions, optionOrder)`.
    * Preguntas: bloques por contexto; cada pregunta sin contexto es su propio bloque; los grupos
      contiguos que comparten contexto se mueven juntos y se barajan por dentro.
    * Opciones: permutación por pregunta, salvo las marcadas con `lockOptionOrder`, que reciben la
      permutación identidad.
    * `Random` inyectado para que el resultado sea reproducible.
  - **Verificación:** `ShuffleQuestionsUseCaseTest` nuevo: mismo conjunto de preguntas antes y
    después, bloques contiguos, permutación biyectiva, identidad en las bloqueadas.
  - **Archivos:** `domain/model/ShuffledQuestions.kt` (nuevo),
    `domain/usecase/ShuffleQuestionsUseCase.kt` (nuevo), `di/Koin.kt`,
    `commonTest/domain/ShuffleQuestionsUseCaseTest.kt` (nuevo)

## Slice 2 · El estado traduce índices

- [x] **Tarea 3: `QuizUiState` y `QuizViewModel` barajan y guardan el índice canónico**
  - **Aceptación:**
    * `QuizUiState` guarda `optionOrder` y expone `displayedOptions(question)` y
      `canonicalOptionIndex(question, displayedIndex)`.
    * `selectOption` recibe la posición MOSTRADA y guarda la CANÓNICA en el estado.
    * El barajado se aplica tanto al test completo como al repaso de fallos.
  - **Verificación:** `QuizViewModelTest` ampliado con el test crítico: con semilla fija y opciones
    barajadas, lo que llega a `SaveScoreUseCase` es el índice canónico. Comprobar por mutación que
    el test falla si se guarda la posición mostrada.
  - **Archivos:** `presentation/quiz/QuizUiState.kt`, `presentation/quiz/QuizViewModel.kt`,
    `commonTest/presentation/QuizViewModelTest.kt`

## Slice 3 · El usuario lo ve

- [x] **Tarea 4: `QuizScreen` pinta el orden barajado**
  - **Aceptación:**
    * Las opciones se recorren en orden de pantalla y la letra (A/B/C/D) corresponde a la posición
      mostrada.
    * El estado de cada tarjeta (elegida / correcta / incorrecta) se resuelve contra el índice
      canónico.
  - **Verificación:** ejecución real en emulador.
  - **Archivos:** `presentation/quiz/QuizScreen.kt`

- [x] **Tarea 5: Verificación en dispositivo y cierre**
  - **Aceptación:** dos sesiones seguidas en orden distinto; 47 y 88 intactas; desglose correcto
    tras un test barajado; `:app:compileKotlinWasmJs` en verde; README actualizado.
  - **Verificación:** emulador + ambos comandos de Gradle.
  - **Archivos:** `README.md`

---

## Verificado en dispositivo

Emulador Pixel_8 headless, 21-08-2026. Sembrando el historial en SharedPreferences para llegar a
las preguntas concretas sin contestar 45 a mano:

- [x] **Las bloqueadas siguen intactas.** La 47 conserva A/B/C/D, así que «la respuesta b)» sigue
      apuntando a la Sección segunda; la 88 mantiene «Todas las respuestas anteriores» en último
      lugar.
- [x] **Las demás sí se barajan.** Contrastado contra `exams.json`: la q66 se mostró
      Quince·Veinte·Diez·Siete frente al canónico Siete·Diez·Quince·Veinte, y la q50
      Aeropuertos·Helipuertos·Espectáculos·Pesca frente a Pesca·Espectáculos·Helipuertos·Aeropuertos.
- [x] **Tres sesiones seguidas del mismo modelo abren con preguntas distintas.**
- [x] **La traducción a índice canónico funciona de punta a punta.** En la q66, con las opciones
      barajadas, se pulsó la posición C («Veinte días»); el desglose dice «Tu respuesta: Veinte
      días». Si guardara la posición de pantalla diría «Quince días», que es la canónica en C.
