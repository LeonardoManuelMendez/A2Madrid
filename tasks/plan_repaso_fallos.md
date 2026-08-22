# Plan de Implementación: Memoria de respuestas y repaso de fallos

Spec de referencia: [`docs/spec_repaso_fallos.md`](../docs/spec_repaso_fallos.md).

---

## 1. Arquitectura y Componentes

El cambio recorre las tres capas, siempre respetando la regla de dependencias hacia dentro:

1. **Dominio**
   * Modelo nuevo `AnsweredQuestion` (id de pregunta + índice elegido).
   * `ScoreEntry` gana `answers` e `isReview`, ambos con valor por defecto.
   * `SaveScoreUseCase` pasa a recibir las respuestas y a devolver `SaveScoreResult`
     (la entrada persistida + si es récord), porque la pantalla de resultado necesita el
     `timestampMillis` del intento para localizarlo después.
   * `GetAttemptReviewUseCase` reconstruye el detalle cruzando respuestas guardadas con
     preguntas del examen.
2. **Datos**
   * `ScoreEntryDto` refleja los dos campos nuevos; `ScoreMapper` los traduce en ambos sentidos.
   * Sin cambios en `ScoreStorage` ni en las implementaciones de plataforma: sigue siendo una
     lista de DTO serializada a JSON.
3. **Presentación**
   * `QuizUiState` arrastra `List<AnswerResult>`; `correctAnswers` pasa a ser derivado.
   * `QuizViewModel` acumula cada respuesta y gana el modo repaso (`loadReview`).
   * `ResultViewModel` + `ResultScreen`: desglose pregunta a pregunta y botón de repaso.
   * `Routes` gana `attemptMillis` en `ResultRoute` y una `ReviewRoute` nueva.
   * `ScoreHistoryScreen` etiqueta los repasos y ofrece repasar intentos anteriores.

---

## 2. Orden de implementación (rebanadas verticales)

El orden es por dependencia, no por importancia. Cada rebanada deja el proyecto compilando y en
verde.

```
Slice 1 (dominio+datos)  ──►  Slice 2 (captura)  ──►  Slice 3 (desglose)  ──►  Slice 4 (repaso)
   persistir respuestas       el test las graba      el resultado las pinta    se estudian
```

* **Slice 1 · El dato existe y se persiste.** Sin esto no hay nada que mostrar.
* **Slice 2 · El test lo produce.** `QuizViewModel` acumula y entrega a `SaveScoreUseCase`.
* **Slice 3 · El usuario lo ve.** Desglose en la pantalla de resultado. Primer valor visible.
* **Slice 4 · El usuario lo usa.** Repaso de fallos desde resultado e historial.

---

## 3. Mitigación de Riesgos

* **Riesgo 1: romper historiales ya guardados en dispositivos reales.**
  * *Mitigación:* los dos campos nuevos del DTO llevan valor por defecto, que es exactamente el
    caso que `kotlinx.serialization` cubre al deserializar. Se añade un test que decodifica un
    JSON con la forma **antigua** y comprueba que produce un `ScoreEntry` válido con `answers`
    vacío. Ese test es la red de seguridad del cambio.
* **Riesgo 2: que un repaso falsee el récord del modelo.**
  * *Mitigación:* `isReview` y su exclusión en el cálculo de mejor marca, con test dedicado.
* **Riesgo 3: `Long?` como argumento de navegación type-safe.**
  * *Mitigación:* no se usa. En vez de hacer nullable el argumento de `QuizRoute`, se añade una
    `ReviewRoute` separada con sus dos argumentos obligatorios. El modo llega a `QuizScreen`
    como parámetro de composable, no como argumento de navegación.
* **Riesgo 4: desglose de 45 preguntas en la pantalla de resultado.**
  * *Mitigación:* `LazyColumn`, no `Column` con scroll. Cada fila colapsada por defecto.
* **Riesgo 5: romper multiplataforma.**
  * *Mitigación:* todo en `commonMain`; verificación con `:app:compileKotlinWasmJs` al cierre.

---

## 4. Puntos de Verificación (Checkpoints)

* **Checkpoint 1 (fin de Slice 1):** `./gradlew :app:testDebugUnitTest` en verde, incluido el
  test de compatibilidad con el formato antiguo.
* **Checkpoint 2 (fin de Slice 2):** los tests de `QuizViewModel` demuestran que un test
  completo deja tantas respuestas como preguntas, y `correctAnswers` sigue cuadrando.
* **Checkpoint 3 (fin de Slice 3):** ejecución real: terminar un test y ver el desglose con la
  opción elegida y la correcta en cada pregunta.
* **Checkpoint 4 (fin de Slice 4):** ejecución real: repasar fallos desde el resultado, y
  repasar los fallos de un intento anterior tras reiniciar la app.
* **Checkpoint final:** `:app:compileKotlinWasmJs` en verde (garantía multiplataforma).
