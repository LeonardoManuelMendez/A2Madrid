# Spec: Memoria de respuestas y repaso de fallos

## Objetivo

Hoy la app corrige, pero no recuerda. En `QuizViewModel.confirmAnswer()` se construye un
`AnswerResult`, se lee su `isCorrect` para sumar uno al contador y se descarta. Al terminar el
test solo sobrevive el número de aciertos, así que es **estructuralmente imposible** repasar lo
que fallaste: el dato no existe en ninguna capa.

Esta funcionalidad cierra el bucle de estudio:

1. El test recuerda qué contestaste en cada pregunta.
2. La pantalla de resultado muestra el desglose pregunta a pregunta, con la explicación.
3. Puedes lanzar un test compuesto **solo por las preguntas que fallaste**.
4. Ese recuerdo sobrevive al cierre de la app: mañana puedes repasar los fallos de hoy.

**Usuario:** opositor al Auxiliar C2 de la Comunidad de Madrid que repite modelos de examen a
lo largo de semanas.

**Éxito:** tras fallar N preguntas, el usuario puede, en un toque y también al día siguiente,
hacer un test de exactamente esas N preguntas, y ver por qué falló cada una.

## Tech Stack

Kotlin Multiplatform · Compose Multiplatform · Koin · kotlinx.serialization ·
Navigation Compose type-safe. Todo el cambio vive en `commonMain` — sin APIs de plataforma,
para no romper Android, iOS ni web.

## Commands

```
Test:   ./gradlew :app:testDebugUnitTest
Build:  ./gradlew :app:assembleDebug
Web:    ./gradlew :app:compileKotlinWasmJs
iOS:    ./gradlew :app:linkDebugFrameworkIosSimulatorArm64   (solo en macOS)
```

## Restricción de diseño que condiciona todo

`A2MadridNavDisplay.kt` navega al resultado con `popUpTo<QuizRoute> { inclusive = true }`: el
destino del test se destruye y con él su ViewModel. Los datos del intento viajan hoy como cinco
enteros dentro de `ResultRoute`, pero **una lista de respuestas no cabe en una ruta** (las rutas
type-safe se serializan a argumentos de navegación).

→ Las respuestas tienen que persistirse en la capa de datos, no sostenerse en el ViewModel.
La ruta solo transporta un identificador del intento (`attemptMillis`) con el que la pantalla de
resultado lo recupera del historial.

## Modelo de datos

```kotlin
// Dominio: qué contestó el usuario en una pregunta concreta.
data class AnsweredQuestion(val questionId: Int, val selectedOptionIndex: Int)

// ScoreEntry gana dos campos, ambos con valor por defecto.
data class ScoreEntry(
    …,
    val answers: List<AnsweredQuestion> = emptyList(),
    val isReview: Boolean = false,
)
```

**Compatibilidad hacia atrás:** `ScoreEntryDto` es `@Serializable` y `kotlinx.serialization`
rellena con el valor por defecto los campos ausentes, así que los historiales ya guardados en el
dispositivo de un usuario se siguen leyendo sin migración ni pérdida. Los intentos antiguos
simplemente no ofrecen desglose (`answers` vacío).

Se guarda el **id** de la pregunta y el **índice** elegido, nunca el enunciado: el contenido se
recupera recargando el examen y cruzando por id. Almacenamiento mínimo y una sola fuente de
verdad para el texto de las preguntas.

## Los repasos son intentos marcados, no intentos ocultos

Un repaso se guarda como un `ScoreEntry` más, con `isReview = true`. Esto evita inventar un
segundo camino de resultado: el repaso reutiliza la misma pantalla, el mismo desglose y el mismo
botón (puedes repasar los fallos de un repaso hasta dejarlos a cero).

A cambio, `isReview` obliga a dos exclusiones:

* `SaveScoreUseCase` ignora los repasos al calcular la mejor marca — si no, un repaso de 6
  preguntas con 6 aciertos falsearía el récord del modelo completo de 45.
* La pantalla de historial los etiqueta como «Repaso» para que no se lean como intentos
  completos.

## Code Style

Se sigue el estilo del repositorio: cabecera de bloque en castellano explicando la capa, KDoc en
inglés, modelos de dominio puros, un caso de uso por operación, DTO separado del dominio.

```kotlin
/*
 * ══ CAPA DE DOMINIO · UseCase ══
 * Reconstruye el detalle de un intento cruzando las respuestas guardadas con las
 * preguntas del examen. → lo usa ResultViewModel para pintar el desglose.
 */
class GetAttemptReviewUseCase(
    private val getExam: GetExamUseCase,
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(examId: String, attemptMillis: Long): List<AnswerResult>
}
```

## Testing Strategy

`kotlin.test` en `app/src/commonTest`, con `FakeQuizRepository` como doble. TDD estricto: test
rojo antes que código. Cobertura mínima nueva:

* `SaveScoreUseCaseTest` — persiste las respuestas; un repaso no altera la mejor marca.
* `ScoreMapperTest` — ida y vuelta DTO⇄dominio; un JSON sin los campos nuevos se lee sin fallar.
* `QuizViewModelTest` — acumula un `AnswerResult` por pregunta; `correctAnswers` derivado;
  el modo repaso carga solo las falladas y no escribe récord.
* `GetAttemptReviewUseCaseTest` — cruza ids correctamente; intento inexistente → lista vacía.

## Boundaries

* **Siempre:** todo en `commonMain`; campos nuevos con valor por defecto; test antes que código;
  `./gradlew :app:testDebugUnitTest` en verde antes de cerrar cada tarea.
* **Preguntar antes:** cambiar el formato de almacenamiento de forma no compatible; añadir
  dependencias; tocar la penalización o el umbral de aprobado (eso es el paso «modo examen»).
* **Nunca:** romper historiales ya guardados; barajar preguntas aquí (es el paso siguiente del
  roadmap); guardar el enunciado de las preguntas en el historial.

## Success Criteria

1. Terminar un test y ver el desglose completo: por cada pregunta, ✓/✗, la opción elegida, la
   correcta y la explicación.
2. Pulsar «Repasar los N fallos» y hacer un test con exactamente esas N preguntas.
3. Cerrar la app, reabrirla, entrar en Puntuaciones y repasar los fallos de un intento anterior.
4. Un repaso con pleno de aciertos **no** aparece como récord del modelo.
5. Un historial guardado con la versión anterior se sigue leyendo (sin desglose, sin crash).
6. `./gradlew :app:testDebugUnitTest` y `:app:compileKotlinWasmJs` en verde.

## Open Questions

* Ninguna bloqueante. Pendiente de decidir más adelante (fuera de esta spec): si el repaso debe
  barajar las preguntas — depende del paso «barajar», que va después.
