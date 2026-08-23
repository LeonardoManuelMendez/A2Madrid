# TODO: Elegir entre estudiar y simular

Spec: [`docs/spec_eleccion_modo.md`](../docs/spec_eleccion_modo.md)

- [x] **Tarea 1: Pantalla de instrucciones del simulacro**
  - **Aceptación:** `SimulationBriefingRoute(examId, examTitle, questionCount)`; pantalla con las
    cuatro reglas y la limitación de no poder volver atrás; botón de empezar y de volver.
  - **Verificación:** compila y se ve en el emulador.
  - **Archivos:** `presentation/simulation/SimulationBriefingScreen.kt` (nuevo),
    `presentation/navigation/Routes.kt`

- [x] **Tarea 2: Dos botones en la tarjeta de modelo**
  - **Aceptación:** la tarjeta deja de ser pulsable entera; «Estudiar» y «Simulacro», cada uno con
    su línea de explicación; el de simulacro lleva a las instrucciones.
  - **Verificación:** emulador.
  - **Archivos:** `presentation/examselection/ExamSelectionScreen.kt`,
    `presentation/navigation/A2MadridNavDisplay.kt`

- [x] **Tarea 3: Verificación y cierre**
  - **Aceptación:** los cuatro criterios de la spec comprobados en el emulador, incluido que atrás
    desde el simulacro no rebote en las instrucciones.
  - **Verificación:** emulador + ambos comandos de Gradle.
  - **Archivos:** —

---

## Verificado en dispositivo

Emulador Pixel_8 headless, 23-08-2026. Los cuatro criterios de la spec:

- [x] Cada tarjeta muestra las dos acciones con su explicación, sin tener que adivinar nada.
- [x] «Estudiar» abre el modo práctica: sin reloj y con botón de confirmar.
- [x] «Simulacro» abre las instrucciones; el reloj solo arranca desde «Empezar simulacro».
- [x] Atrás desde el simulacro vuelve a la lista de modelos, no a las instrucciones.

## Añadido: el favicon que faltaba en la app

Reportado desde la consola del navegador: `GET /favicon.ico 404`.

La landing sí declaraba icono (un SVG en línea), pero `app/src/wasmJsMain/resources/index.html`
no tenía ninguno, así que al abrir `/app/` el navegador caía al `/favicon.ico` por defecto y se
llevaba el 404. Se le pone el MISMO icono que la landing, para que la pestaña no cambie de cara al
entrar en la app.

Comprobado sobre el artefacto que se publica de verdad
(`app/build/dist/wasmJs/productionExecutable/index.html`), no solo sobre el fuente.
