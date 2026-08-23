# Spec: Elegir entre estudiar y simular

## Problema

La tarjeta de un modelo es pulsable entera —y eso lanza el modo práctica— pero nada lo dice.
Dentro lleva un botón de simulacro que hace algo muy distinto. El usuario no tiene forma de saber
que tocar el título estudia, y el texto del simulacro no contrasta con nada porque la otra acción
es invisible.

## Solución

**Dos botones explicados, y una pantalla de instrucciones antes del simulacro.**

La tarjeta deja de ser pulsable en su conjunto. Cada acción es un botón con una línea debajo que
dice qué hace:

* **Estudiar** — «Corrige y explica cada pregunta».
* **Simulacro** — «45 min · con penalización».

## Por qué el simulacro lleva pantalla previa y estudiar no

No son simétricos. Estudiar es la acción frecuente, reversible y sin coste: merece ser directa.
Un simulacro compromete tres cuartos de hora, penaliza los errores, no corrige hasta el final y no
deja volver a una pregunta anterior. Eso no debería empezar de un toque por curiosidad.

Es además lo que ocurre en el aula: antes de abrir el cuadernillo se leen las instrucciones. La
pantalla previa recoge las mismas reglas que el examen enuncia, y es donde vive la explicación, de
modo que los botones pueden quedarse cortos.

La pantalla dice cuatro reglas y una limitación:

```
⏱  45 minutos, un minuto por pregunta
−⅓ Cada error resta un tercio de acierto
○  Puedes dejar preguntas en blanco: no puntúan ni penalizan
✗  No se corrige nada hasta el final
←  No se puede volver a una pregunta anterior
```

La última no es una regla del examen sino una limitación de la app. Decirla antes es más honesto
que dejar que se descubra a mitad del ejercicio.

## Alcance

* `ExamCard` pasa de superficie pulsable a contenedor con dos botones.
* Ruta y pantalla nuevas para las instrucciones, entre la selección y el simulacro.
* Al empezar, la pantalla de instrucciones se saca de la pila: volver atrás desde el simulacro
  lleva a la lista de modelos, no a las instrucciones otra vez.

## Testing Strategy

El cambio es de navegación y presentación pura, sin lógica que se pueda probar en `commonTest`
sin infraestructura de UI que este proyecto no tiene. Se verifica ejecutándolo: que cada botón
lleve a donde dice y que atrás desde el simulacro no rebote en las instrucciones.

La suite existente (71 tests) debe seguir en verde: no se toca dominio.

## Success Criteria

1. Desde la lista se ve, sin tocar nada, qué hace cada acción.
2. «Estudiar» abre el modo práctica directamente.
3. «Simulacro» abre las instrucciones; solo desde ahí arranca el reloj.
4. Volver atrás desde el simulacro lleva a la lista, no a las instrucciones.
5. `:app:testDebugUnitTest` y `:app:compileKotlinWasmJs` en verde.
