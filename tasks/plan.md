# Plan de Implementación: Barajado de preguntas y opciones

Spec de referencia: [`docs/spec_barajado.md`](../docs/spec_barajado.md).

---

## 1. Arquitectura y Componentes

La idea que gobierna el diseño: **el barajado es una vista, no un cambio en el dato.**

1. **Dominio**
   * `Question` gana `lockOptionOrder`, el único dato que el código no puede inferir solo.
   * `ShuffledQuestions` (preguntas reordenadas + permutación de opciones por pregunta).
   * `ShuffleQuestionsUseCase`: puro, sin repositorio, con `Random` inyectado.
2. **Datos**
   * `QuestionDto` refleja el campo nuevo con valor por defecto; `exams.json` marca las
     preguntas 47 y 88.
3. **Presentación**
   * `QuizUiState` guarda la permutación de la sesión y expone las opciones ya en orden de
     pantalla, más la traducción a índice canónico.
   * `QuizViewModel` baraja al cargar y traduce en `selectOption`.
   * `QuizScreen` pinta las opciones mostradas; nada más cambia.

La capa de datos y el historial **no se tocan**: siguen hablando en índices canónicos.

---

## 2. Orden de implementación (rebanadas verticales)

```
Slice 1 (dominio)  ──►  Slice 2 (estado)  ──►  Slice 3 (UI)
 barajar y proteger      traducir índices      pintar el orden
 las no barajables       canónicos             barajado
```

* **Slice 1 · El barajado existe y respeta las reglas.** Sin esto no hay nada que mostrar.
* **Slice 2 · El estado traduce.** Es la rebanada crítica: aquí se protege el repaso de fallos.
* **Slice 3 · El usuario lo ve.** La UI pinta lo que el estado ya le da resuelto.

---

## 3. Mitigación de Riesgos

* **Riesgo 1 (el grave): romper el repaso de fallos ya publicado.**
  * *Mitigación:* el índice que se persiste sigue siendo el canónico. Se añade un test que baraja
    con semilla fija, responde, y comprueba que lo guardado apunta a la opción correcta del JSON
    y no a la posición de pantalla. Ese test es la red de seguridad de todo el cambio.
* **Riesgo 2: romper las preguntas con opciones autorreferentes.**
  * *Mitigación:* `lockOptionOrder` en el dato + test dedicado. No se usan heurísticas de texto.
* **Riesgo 3: dispersar las preguntas que comparten estímulo.**
  * *Mitigación:* barajado por bloques + test que comprueba que siguen contiguas.
* **Riesgo 4: barajado no reproducible en tests.**
  * *Mitigación:* `Random` inyectado; en producción se crea uno por sesión.
* **Riesgo 5: que el barajado se aplique también al reintentar y confunda al comparar marcas.**
  * *Mitigación:* ninguna necesaria. La puntuación no depende del orden; el récord se sigue
    calculando sobre aciertos totales del modelo.

---

## 4. Puntos de Verificación (Checkpoints)

* **Checkpoint 1 (fin de Slice 1):** suite en verde, incluidos los tests de bloques y de
  `lockOptionOrder`.
* **Checkpoint 2 (fin de Slice 2):** el test de índice canónico pasa, y se comprueba que FALLA si
  se guarda a propósito la posición mostrada (prueba de mutación).
* **Checkpoint 3 (fin de Slice 3):** ejecución real en emulador — dos sesiones seguidas salen en
  orden distinto, la 47 y la 88 conservan sus opciones, y el desglose tras un test barajado señala
  la opción realmente pulsada.
* **Checkpoint final:** `:app:compileKotlinWasmJs` en verde.
