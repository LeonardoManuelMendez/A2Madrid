# TODO: Modo simulacro de examen

Plan: [`tasks/plan.md`](plan.md) · Spec: [`docs/spec_simulacro.md`](../docs/spec_simulacro.md)

Regla: TDD. Verificación por defecto `./gradlew :app:testDebugUnitTest`.

---

## Slice 1 · Puntuación

- [x] **Tarea 1: `ExamScore` con la penalización de un tercio**
  - **Aceptación:** modelo nuevo con aciertos, errores, blancos y `net = aciertos − errores/3`;
    los blancos no puntúan; se deriva de una lista de `AnswerResult` más el total de preguntas.
  - **Verificación:** `ExamScoreTest` nuevo, con los extremos (todo en blanco, sin errores).
  - **Archivos:** `domain/model/ExamScore.kt` (nuevo), `commonTest/domain/ExamScoreTest.kt` (nuevo)

- [x] **Tarea 2: Persistir el simulacro y resolver el desglose globalmente**
  - **Aceptación:** `ScoreEntry`/`ScoreEntryDto` ganan `isExam = false`; `SaveScoreUseCase` lo
    acepta y lo excluye del récord; `GetAttemptReviewUseCase` resuelve las preguntas contra todos
    los modelos en vez de contra uno.
  - **Verificación:** tests existentes en verde sin tocarlos, más un caso nuevo de desglose que
    mezcla preguntas de dos modelos.
  - **Archivos:** `domain/model/ScoreEntry.kt`, `data/dto/ScoreEntryDto.kt`,
    `data/mapper/ScoreMapper.kt`, `domain/usecase/SaveScoreUseCase.kt`,
    `domain/usecase/GetAttemptReviewUseCase.kt`, sus tests

## Slice 2 · El modo

- [x] **Tarea 3: `QuizMode` y carga del simulacro**
  - **Aceptación:** `QuizMode { PRACTICE, REVIEW, EXAM }` sustituye a `isReview` en el estado;
    `loadExamSimulation(examId)` carga UN modelo con su barajado; `skipQuestion()` avanza sin
    registrar respuesta.
  - **Verificación:** `QuizViewModelTest` ampliado: orden de bloques, blanco no registrado,
    y que el simulacro no genera récord de ningún modelo.
  - **Archivos:** `presentation/quiz/QuizUiState.kt`, `presentation/quiz/QuizViewModel.kt`,
    `domain/usecase/GetExamsUseCase.kt` (si procede), `commonTest/presentation/QuizViewModelTest.kt`

## Slice 3 · El reloj

- [x] **Tarea 4: Cuenta atrás y cierre automático**
  - **Aceptación:** un minuto por pregunta; el restante se calcula contra un instante de fin, no
    acumulando ticks; a los 5 minutos se marca el aviso; al llegar a cero el ejercicio se cierra
    con lo contestado.
  - **Verificación:** tests con reloj virtual, sin esperas reales.
  - **Archivos:** `presentation/quiz/QuizViewModel.kt`, `presentation/quiz/QuizUiState.kt`,
    `commonTest/presentation/QuizViewModelTest.kt`

## Slice 4 · La UI

- [x] **Tarea 5: Pantalla de test en modo simulacro**
  - **Aceptación:** barra con el tiempo restante (y aviso en los últimos 5 minutos); botón de
    dejar en blanco; sin tarjeta de explicación durante la prueba; el botón avanza sin confirmar.
  - **Verificación:** ejecución real.
  - **Archivos:** `presentation/quiz/QuizScreen.kt`

- [x] **Tarea 6: Resultado del simulacro**
  - **Aceptación:** aciertos, errores, blancos y neto; sin veredicto de aprobado; el desglose y el
    repaso de fallos siguen funcionando.
  - **Verificación:** ejecución real.
  - **Archivos:** `presentation/result/ResultScreen.kt`, `presentation/navigation/Routes.kt`,
    `presentation/navigation/A2MadridNavDisplay.kt`

## Slice 5 · Cierre

- [x] **Tarea 7: Entrada al simulacro, dispositivo y documentación**
  - **Aceptación:** se lanza desde la pantalla de selección, avisando de cuántas preguntas, cuánto
    dura, que penaliza y que no corrige hasta el final; `README.md` actualizado.
  - **Verificación:** emulador + ambos comandos de Gradle.
  - **Archivos:** `presentation/examselection/ExamSelectionScreen.kt`, `README.md`

---

## Cambio de diseño a mitad de camino: el reloj va por modelo

La primera versión encadenaba los tres modelos en un solo simulacro de 137 minutos. Lo señaló el
usuario y tenía razón: eso no es un simulacro, es una maratón que nadie hace, y el examen real
dura 90 minutos. Ahora **cada modelo se lanza por separado con su propio reloj** (44, 45 y 48
minutos, a un minuto por pregunta).

No se pierde fidelidad por ello: el primer ejercicio son 90 preguntas en 90 minutos, así que hacer
psicotécnico y legislación seguidos son 89 en 89 minutos. Casi exacto, pero decidiéndolo el
usuario y no la app.

## Verificado en dispositivo

Emulador Pixel_8 headless, 22-08-2026:

- [x] Cada modelo ofrece su simulacro con su duración: 44, 45 y 48 minutos.
- [x] El cronómetro corre y se ve en la barra superior.
- [x] Durante el simulacro no hay «Confirmar» ni explicación: solo «Dejar en blanco | Siguiente».
- [x] Recorrido completo de 44 preguntas con 1 contestada y 43 en blanco → neto **−0,33**,
      «0 aciertos · 1 error · 43 en blanco», sin veredicto de aprobado.
- [x] El historial etiqueta el simulacro, no le da la estrella de mejor marca y ofrece repasar
      solo el fallo real.

## Tres defectos encontrados en esa verificación

Los tres estaban en el historial y ninguno los habría cazado la suite:

1. **«Repasar 44 fallos»** cuando solo se falló una: `ScoreEntry.wrongAnswers` restaba aciertos al
   total, contando los blancos como fallos. Con el modo práctica daba igual porque no había
   blancos; en un simulacro prometía repasar preguntas que nunca se contestaron. Corregido y
   cubierto con test, incluido el caso de un historial antiguo sin desglose.
2. **El simulacro reclamaba la estrella de mejor marca**, porque el filtro del historial excluía
   repasos pero no simulacros. El dominio ya lo prohibía; la UI no se había enterado.
3. **Plurales**: «1 errores», «1 fallos». Dos sitios distintos, el segundo escrito justo después
   de arreglar el primero.
