# Spec: Barajado de preguntas y opciones

## Objetivo

Hoy la segunda vuelta a un modelo es idéntica a la primera: mismas preguntas, mismo orden, y cada
opción en la misma posición. A la tercera pasada el usuario no está recordando el contenido, está
recordando «era la tercera». La app se vuelve **menos útil cuanto más se usa**, que es lo contrario
de lo que debe hacer una herramienta de estudio.

Cada sesión debe presentar las mismas preguntas en distinto orden y con las opciones en distinta
posición, de modo que acertar exija reconocer el contenido.

**Éxito:** dos sesiones consecutivas del mismo modelo presentan orden de preguntas y posición de
opciones distintos, sin que se rompa ninguna pregunta ni ningún dato ya guardado.

## Las dos trampas

### 1 · Hay preguntas cuyas opciones NO se pueden barajar

Dos de las 89 preguntas tienen opciones que se refieren a otras opciones:

| Pregunta | Opción D |
| --- | --- |
| 47 | «La respuesta **b)** es correcta y además el recurso de amparo será aplicable…» |
| 88 | «Todas las respuestas **anteriores** son correctas.» |

Barajarlas convierte una pregunta correcta en una pregunta sin sentido. No es detectable con una
heurística fiable en tiempo de ejecución —«anteriores» y «b)» aparecen en texto legítimo—, así que
se marca en el dato: `lockOptionOrder: true`.

El valor por defecto es `false` (barajable), porque es el caso de 87 de 89. **Al añadir preguntas
nuevas —en particular el bloque de ofimática— hay que revisar si alguna necesita la marca.**

### 2 · El índice de la opción elegida está persistido

`AnsweredQuestion.selectedOptionIndex` ya vive en el historial de usuarios reales, y
`GetAttemptReviewUseCase` lo usa para reconstruir el desglose cruzándolo con `exams.json`.

Si se baraja la posición de las opciones y se guarda la posición **mostrada**, el desglose del día
siguiente señalaría una opción distinta de la que el usuario eligió. Rompería el repaso de fallos
recién publicado, y lo haría de forma silenciosa.

→ **El barajado es una vista, no un cambio en el dato.** El dominio y la persistencia siguen
hablando siempre del orden canónico de `exams.json`; la capa de presentación guarda la permutación
de la sesión y traduce en ambos sentidos. El historial ya guardado sigue siendo válido y el repaso
no se entera de que existe el barajado.

## Modelo

```kotlin
// Dominio: el resultado de barajar una sesión.
data class ShuffledQuestions(
    /** Preguntas reordenadas; su contenido sigue siendo el canónico. */
    val questions: List<Question>,
    /** questionId → posición mostrada → índice canónico de la opción. */
    val optionOrder: Map<Int, List<Int>>,
)

class ShuffleQuestionsUseCase {
    operator fun invoke(questions: List<Question>, random: Random): ShuffledQuestions
}
```

### Cómo se barajan las preguntas

Las preguntas que comparten estímulo (`context`) van contiguas en el examen —5 de `rejilla_letras`
y 9 de `tabla_empleados`— y deben seguir juntas: dispersar la misma rejilla por todo el test es
peor experiencia y peor estudio.

El barajado trabaja con **bloques**: cada pregunta sin contexto es un bloque de una, y cada grupo
contiguo que comparte contexto es un bloque. Se baraja el orden de los bloques, y dentro de un
bloque con contexto se barajan sus preguntas. Para el examen de aptitudes eso da 32 bloques
(8 + 1 + 11 + 1 + 11), suficiente entropía sin romper la coherencia.

## Tech Stack

Kotlin Multiplatform · Compose Multiplatform · Koin · kotlinx.serialization. Todo en `commonMain`.
`kotlin.random.Random` es multiplataforma; se inyecta para poder fijar semilla en los tests.

## Commands

```
Test:   ./gradlew :app:testDebugUnitTest
Build:  ./gradlew :app:assembleDebug
Web:    ./gradlew :app:compileKotlinWasmJs
```

## Testing Strategy

`kotlin.test` en `app/src/commonTest`, con `Random(semilla)` para que el barajado sea reproducible.

* `ShuffleQuestionsUseCaseTest` — los bloques con contexto siguen contiguos; una pregunta con
  `lockOptionOrder` conserva sus opciones intactas; la permutación es una biyección; el conjunto de
  preguntas es el mismo antes y después.
* `QuizViewModelTest` — el índice que se persiste es el CANÓNICO aunque las opciones se muestren
  barajadas. Este es el test que protege el repaso de fallos.
* `QuestionMapperTest` — `lockOptionOrder` viaja del JSON al dominio y su valor por defecto es
  `false`.

## Boundaries

* **Siempre:** el dominio y la persistencia hablan en índices canónicos; `Random` inyectado;
  test antes que código.
* **Preguntar antes:** desactivar el barajado con un ajuste de usuario; barajar en el simulacro
  de examen (eso es el paso siguiente del roadmap y lleva su propia decisión).
* **Nunca:** guardar la posición mostrada; barajar las opciones de una pregunta marcada;
  separar preguntas que comparten estímulo.

## Success Criteria

1. Dos sesiones seguidas del mismo modelo salen con orden de preguntas distinto.
2. Las opciones de una misma pregunta aparecen en posiciones distintas entre sesiones.
3. Las preguntas 47 y 88 conservan siempre el orden original de sus opciones.
4. Las 5 preguntas de rejilla y las 9 de tabla siguen apareciendo agrupadas.
5. Tras un test barajado, el desglose señala como «Tu respuesta» exactamente la opción que se
   pulsó, y el repaso de fallos carga las preguntas correctas.
6. Un historial guardado ANTES del barajado se sigue leyendo y su desglose sigue siendo correcto.
7. `:app:testDebugUnitTest` y `:app:compileKotlinWasmJs` en verde.

## Open Questions

Ninguna bloqueante.
