# Plan de Implementación: Bloque de preguntas de ofimática

Spec de referencia: [`docs/spec_ofimatica.md`](../docs/spec_ofimatica.md).

---

## 1. Naturaleza del trabajo

Esto no es una funcionalidad: es **contenido**. No se toca ni un fichero de dominio, datos o
presentación. Todo el cambio vive en `exams.json`, más un test que lo vigile.

Por eso el plan invierte el orden habitual: primero la red de seguridad, después el contenido.
Escribir 48 preguntas a mano sobre un fichero que hoy no valida nadie es la forma más fácil de
mandar a producción un `correctAnswerIndex` fuera de rango, que en el dispositivo del usuario se
ve como «No se pudo cargar el examen».

---

## 2. Orden de implementación

```
Slice 1 (red)  ──►  Slice 2 (Temas 16-18)  ──►  Slice 3 (Temas 19-21)  ──►  Slice 4 (cierre)
 validar el         Windows, Word, Excel        Access/Power BI,           dispositivo y
 exams.json real    (~31 preguntas)             Outlook, nube (~17)        multiplataforma
```

El contenido va en dos tandas para poder ver la primera funcionando en la app antes de escribir
la segunda, en vez de entregar 48 preguntas de golpe sin haber abierto ninguna.

---

## 3. Mitigación de Riesgos

* **Riesgo 1 (el que importa): una explicación equivocada.** Es peor que no explicar, porque el
  opositor la memoriza y la lleva al examen.
  * *Mitigación:* cada pregunta nace de un punto concreto del resumen de temario. Lo que no esté
    ahí y haga falta se señala en la entrega, no se cuela. Nada que dependa de la versión del
    programa ni de la posición de un botón en la cinta.
* **Riesgo 2: contenido malformado en producción.**
  * *Mitigación:* `ExamsContentTest` sobre el fichero real, con prueba de mutación para comprobar
    que el test tiene dientes.
* **Riesgo 3: romper el barajado recién desplegado.**
  * *Mitigación:* revisar toda opción autorreferente («todas las anteriores», «la b) es
    correcta») y marcarla con `lockOptionOrder`. El test lo comprueba.
* **Riesgo 4: colisión de identificadores.**
  * *Mitigación:* los ids existentes ocupan 1-90; el bloque nuevo empieza en 101. El test exige
    unicidad global.

---

## 4. Puntos de Verificación (Checkpoints)

* **Checkpoint 1 (fin de Slice 1):** el test valida los 89 existentes y falla si se corrompe uno.
* **Checkpoint 2 (fin de Slice 2):** el modelo aparece en la app y las preguntas de Word y Excel
  se responden y explican bien (emulador).
* **Checkpoint 3 (fin de Slice 3):** el modelo completo se recorre de principio a fin.
* **Checkpoint final:** `:app:testDebugUnitTest` y `:app:compileKotlinWasmJs` en verde.
