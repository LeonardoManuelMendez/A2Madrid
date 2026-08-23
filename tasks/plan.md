# Plan de Implementación: Modo simulacro de examen

Spec de referencia: [`docs/spec_simulacro.md`](../docs/spec_simulacro.md).

---

## 1. Arquitectura

El simulacro es un **tercer modo** del test que ya existe, no una pantalla nueva. La sesión se
compone distinto y se corrige distinto; confirmar, avanzar y guardar son los mismos.

1. **Dominio:** `ExamScore` (aciertos, errores, blancos, neto con la penalización de un tercio);
   `ScoreEntry.isExam`; `GetAttemptReviewUseCase` pasa a resolver preguntas contra todos los
   modelos, no contra uno.
2. **Presentación:** `QuizMode { PRACTICE, REVIEW, EXAM }` sustituye al booleano `isReview` en el
   estado; el ViewModel carga el modelo elegido, lleva su cronómetro y permite dejar en blanco.
   El simulacro es **por modelo**: encadenar los tres serían más de dos horas seguidas, que no se
   parece a ningún examen.
3. **UI:** barra de tiempo, botón de dejar en blanco, sin tarjeta de explicación durante la
   prueba, y resultado con las cuatro cifras.

---

## 2. Orden de implementación

```
Slice 1 (puntuación)  ──►  Slice 2 (modo)  ──►  Slice 3 (reloj)  ──►  Slice 4 (UI)  ──►  Slice 5 (cierre)
 la nota neta y el         cargar y dejar       cuenta atrás y      pintarlo         entrada, dispositivo
 desglose global           en blanco            cierre automático                    y documentación
```

Primero la puntuación porque es la regla que define el modo: sin penalización, un simulacro es un
test largo. Después la composición, luego el reloj —lo más delicado de probar— y al final la UI.

---

## 3. Mitigación de Riesgos

* **Riesgo 1: el cronómetro regala tiempo al salir de la app.**
  * *Mitigación:* se guarda un instante de finalización y el tiempo restante se calcula contra el
    reloj, no acumulando ticks. Un tick perdido no alarga el examen.
* **Riesgo 2: romper el repaso de fallos al cambiar `GetAttemptReviewUseCase`.**
  * *Mitigación:* la resolución global de ids se apoya en la unicidad que ya garantiza
    `ExamsContentTest`. Los tests existentes del caso de uso deben seguir en verde sin tocarlos.
* **Riesgo 3: que un simulacro contamine el récord de un modelo.**
  * *Mitigación:* `isExam` queda fuera del cálculo de mejor marca, igual que los repasos: se juega
    con otras reglas, así que su puntuación no es comparable.
* **Riesgo 4: probar un reloj con esperas reales hace la suite lenta y frágil.**
  * *Mitigación:* reloj virtual de `kotlinx-coroutines-test`; ni un `delay` real en los tests.
* **Riesgo 5: confundir al usuario con dos modos que se parecen.**
  * *Mitigación:* el simulacro se anuncia antes de empezar (cuántas preguntas, cuánto dura, que
    penaliza y que no corrige hasta el final).

---

## 4. Puntos de Verificación (Checkpoints)

* **Checkpoint 1:** la fórmula del neto cubierta por tests, incluidos los extremos.
* **Checkpoint 2:** el simulacro carga el modelo elegido, dura un minuto por pregunta y admite
  blancos.
* **Checkpoint 3:** al agotarse el tiempo el ejercicio se cierra solo, probado con reloj virtual.
* **Checkpoint 4:** ejecución real: cronómetro visible, blanco funcional, sin corrección durante.
* **Checkpoint final:** `:app:testDebugUnitTest` y `:app:compileKotlinWasmJs` en verde.
