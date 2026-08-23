# Spec: Modo simulacro de examen

## Objetivo

La app corrige al instante, no cronometra y no deja dejar una pregunta en blanco: no se avanza sin
contestar. Su umbral de aprobado es el 60 % de aciertos brutos. Nada de eso se parece al examen.

El simulacro reproduce las condiciones reales del primer ejercicio, que están escritas en las
instrucciones del propio cuestionario (Orden 2411/2017, ejercicio del 29-09-2018):

| Regla | Texto de las instrucciones |
| --- | --- |
| Formato | «90 preguntas tipo test, con 4 respuestas alternativas (A, B, C y D), siendo solo una de ellas la correcta» |
| Acierto | «se valorará en positivo» |
| Error | «se penalizará con la tercera parte del valor asignado a la contestación correcta» |
| En blanco | «la pregunta no contestada … no tendrá valoración» |
| Tiempo | «la duración del ejercicio será de 90 minutos» |

De ahí salen las tres reglas del simulacro: **penalización de −1/3 por error**, **el blanco no
puntúa**, y **un minuto por pregunta**.

## Alcance

**El simulacro es de UN modelo, con su propio reloj.** Cualquiera de los tres se puede lanzar en
modo simulacro: psicotécnico y ortografía (44 min), legislación (45 min) u ofimática (48 min).

La alternativa —encadenar los tres en una sola sentada— se descartó: serían más de dos horas
seguidas, y eso no se parece a ningún examen. El primer ejercicio real dura 90 minutos.

Y encadenar no hace falta para ganar fidelidad: el primer ejercicio son 90 preguntas en 90
minutos, así que hacer psicotécnico y legislación seguidos son 89 preguntas en 89 minutos. Casi
exacto, pero decidiéndolo el usuario.

* **Orden:** las preguntas se barajan dentro del modelo, como en el modo práctica.
* **Duración:** un minuto por pregunta, la proporción del examen real. La regla se aplica sola si
  cambia el número de preguntas de un modelo.

## Diferencias de comportamiento respecto al modo práctica

1. **Sin corrección inmediata.** No se confirma ni se explica nada hasta el final: en el examen
   nadie te dice si acertaste. Se elige opción y se avanza.
2. **Se puede dejar en blanco**, que es una decisión táctica real cuando la penalización existe.
3. **Cronómetro descendente**, con aviso a falta de 5 minutos, igual que el aula. Al llegar a cero
   el ejercicio se cierra con lo que haya contestado.
4. **Sin veredicto de aprobado.** Las instrucciones no fijan nota de corte —eso va en las bases—,
   así que el simulacro da la puntuación y no se inventa un aprobado.

## Puntuación

```
neto = aciertos − (errores / 3)
```

Se muestran las cuatro cifras: aciertos, errores, blancos y neto. El desglose pregunta a pregunta
—que ya existe— aparece al terminar, y desde él se pueden repasar los fallos con la mecánica que
ya está construida.

## Modelo de datos

`ScoreEntry` gana `isExam: Boolean = false`, con el mismo criterio de compatibilidad que
`isReview`: valor por defecto, de modo que los historiales ya guardados se siguen leyendo. Los
blancos no necesitan campo propio: son las preguntas que no aparecen en `answers`.

El simulacro se guarda bajo el `examId` de su modelo, marcado con `isExam`. Queda fuera del
cálculo de la mejor marca —se juega con otras reglas, así que su puntuación no es comparable— y
el historial lo etiqueta para que no se confunda con un intento de práctica.

`GetAttemptReviewUseCase` pasa a resolver las preguntas contra todos los modelos en vez de contra
uno. No es imprescindible ahora que el simulacro es por modelo, pero elimina una dependencia y es
seguro porque `ExamsContentTest` ya garantiza que los ids de pregunta son únicos en todo el
fichero.

## Testing Strategy

* `ExamScoreTest` — la fórmula del neto, incluidos los casos de cero errores y de todo en blanco.
* `QuizViewModelTest` — el simulacro carga un solo modelo; dura un minuto por pregunta; dejar en
  blanco avanza sin registrar respuesta; al agotarse el tiempo se cierra con lo contestado.
* `GetAttemptReviewUseCaseTest` — el desglose de un simulacro resuelve preguntas de los tres
  modelos.
* El cronómetro se prueba con el reloj virtual de `kotlinx-coroutines-test`, sin esperas reales.

## Boundaries

* **Siempre:** el tiempo se calcula contra un instante de fin, no contando ticks, para que salir y
  volver a la app no regale minutos.
* **Preguntar antes:** añadir navegación hacia atrás entre preguntas; inventar una nota de corte.
* **Nunca:** corregir durante el simulacro; que un simulacro compita por el récord de un modelo.

## Success Criteria

1. Se puede lanzar cualquier modelo en modo simulacro, con su propio cronómetro.
2. Se puede dejar una pregunta en blanco y avanzar.
3. No hay corrección hasta el final.
4. El resultado muestra aciertos, errores, blancos y neto con la penalización de un tercio.
5. Al agotarse el tiempo, el ejercicio se cierra solo.
6. El desglose y el repaso de fallos funcionan sobre un simulacro.
7. `:app:testDebugUnitTest` y `:app:compileKotlinWasmJs` en verde.

## Open Questions

* La nota de corte no consta en las instrucciones del cuestionario. Si aparece en las bases de la
  264/2026, el simulacro podría mostrar el veredicto; hasta entonces solo da la puntuación.
* No hay navegación hacia atrás entre preguntas. En el examen real se puede revisar; aquí no.
  Queda anotado como limitación conocida, no como olvido.
