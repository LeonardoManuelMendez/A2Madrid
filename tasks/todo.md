# TODO: Bloque de preguntas de ofimática

Plan: [`tasks/plan.md`](plan.md) · Spec: [`docs/spec_ofimatica.md`](../docs/spec_ofimatica.md)

Verificación por defecto: `./gradlew :app:testDebugUnitTest`

---

## Slice 1 · Red de seguridad

- [x] **Tarea 1: Validar el `exams.json` real**
  - **Aceptación:**
    * Source set `androidUnitTest` nuevo (hace falta leer el fichero del disco, cosa que
      `commonTest` no puede hacer de forma multiplataforma).
    * `ExamsContentTest` comprueba sobre el fichero real: ids únicos en todo el fichero, 4
      opciones por pregunta, `correctAnswerIndex` dentro de rango, `explanation.summary` no
      vacío, y que toda opción autorreferente esté en una pregunta con `lockOptionOrder`.
  - **Verificación:** pasa con el contenido actual; prueba de mutación (corromper un índice) para
    comprobar que se pone rojo.
  - **Archivos:** `app/build.gradle.kts`, `androidUnitTest/.../ExamsContentTest.kt` (nuevo)

## Slice 2 · Contenido: Temas 16 a 18

- [x] **Tarea 2: Modelo nuevo y Tema 16 (Windows 10)**
  - **Aceptación:** examen `ofimatica_c2_2026` en `exams.json` con ~7 preguntas de Windows 10,
    ids desde 101, todas con explicación y desglose.
  - **Verificación:** `ExamsContentTest` en verde; el modelo aparece en la lista de la app.
  - **Archivos:** `composeResources/files/exams.json`

- [x] **Tarea 3: Tema 17 (Word)**
  - **Aceptación:** ~12 preguntas sobre configuración de página, estilos de párrafo y carácter,
    alineación, bordes al texto o al párrafo, listas multinivel, tabulaciones y ajuste de imagen.
  - **Verificación:** `ExamsContentTest` en verde.
  - **Archivos:** `composeResources/files/exams.json`

- [x] **Tarea 4: Tema 18 (Excel)**
  - **Aceptación:** ~12 preguntas sobre referencias relativas, absolutas y mixtas, códigos de
    error y jerarquía de operadores. Al menos una apoyada en un `context` con una hoja de cálculo
    compartida, para aprovechar el bloque de estímulo que la app ya sabe pintar.
  - **Verificación:** `ExamsContentTest` en verde.
  - **Archivos:** `composeResources/files/exams.json`

## Slice 3 · Contenido: Temas 19 a 21

- [x] **Tarea 5: Access, Power BI, Outlook y nube**
  - **Aceptación:** ~17 preguntas: clave principal e integridad referencial, Power Query/DAX y
    las tres vistas, POP3 frente a IMAP, `.pst` frente a `.ost`, OneDrive frente a SharePoint.
  - **Verificación:** `ExamsContentTest` en verde.
  - **Archivos:** `composeResources/files/exams.json`

## Slice 4 · Cierre

- [x] **Tarea 6: Verificación en dispositivo y documentación**
  - **Aceptación:** el modelo se abre y se recorre en el emulador; explicaciones legibles;
    `README.md` menciona el bloque; `:app:compileKotlinWasmJs` en verde.
  - **Verificación:** emulador + ambos comandos de Gradle.
  - **Archivos:** `README.md`

---

## Hallazgo durante la revisión: el barajado ya publicado tenía un hueco

Auditando el contenido nuevo apareció un defecto **en producción**, introducido con el barajado.
Mi auditoría de `lockOptionOrder` buscó opciones autorreferentes con un patrón demasiado estrecho
—«la respuesta b)» con paréntesis y «todas las anteriores»— y encontró solo dos preguntas. Se le
escaparon:

| Pregunta | Opción | Problema |
| --- | --- | --- |
| 72 | «a y b son correctas» | Se barajaba: las letras a las que apunta cambian |
| 73 | «Todas son correctas» | Comodín que solo se entiende leído al final |
| 51, 60 | «Ninguna respuesta es correcta» | Íd. |
| 76 | — | La **explicación** citaba «la opción a)» y «la opción d)» mientras las opciones sí se barajaban |

Corregido: 51, 60, 72 y 73 quedan con el orden bloqueado, y la explicación de la 76 nombra el
contenido en vez de la letra. La 112 (nueva) tenía el mismo defecto y se reescribió.

El arreglo duradero no es el parche sino los dos guardianes nuevos en `ExamsContentTest`, que
ahora comprueban tanto las opciones que dependen del orden como las explicaciones que señalan una
opción por su letra o su posición. Ambos verificados por mutación.

## Verificado en dispositivo

Emulador Pixel_8 headless, 22-08-2026:

- [x] El tercer modelo aparece en la lista con sus 48 preguntas.
- [x] La hoja de cálculo compartida se pinta alineada y **entera**: en la primera pasada la
      columna E quedaba fuera del ancho visible y la pregunta habla justo de E2, así que se
      estrechó el contexto de 58 a 42 caracteres.
- [x] Las opciones del bloque nuevo se barajan (dos aperturas seguidas, dos órdenes distintos).
- [x] La explicación despliega su desglose y el bloque de fuente sin cita literal.
