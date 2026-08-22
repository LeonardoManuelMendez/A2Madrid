# Spec: Bloque de preguntas de ofimática

## Objetivo

La app cubre el primer ejercicio de la oposición (psicotécnico, ortografía y legislación) y no
tiene ni una pregunta de ofimática, que es el temario de los Temas 16 a 21 de la convocatoria
264/2026: Windows 10, Word, Excel, Access y Power BI, Outlook y trabajo en la nube.

**Lo que este bloque NO es.** La prueba de ofimática del proceso selectivo no es un test: es un
ejercicio práctico de 30 minutos en el que se reproduce un documento en Word siguiendo una lista
de instrucciones. Eso no se puede practicar en una app de preguntas, y esta funcionalidad no lo
pretende.

**Lo que sí es.** El práctico penaliza no saber el dato exacto —si un borde de 2¼ pto va «al
texto» o «al párrafo», si `$A$1` se adapta al arrastrar, dónde vive *Bordes y sombreado*—. Ese
dato sí se machaca con preguntas. El bloque prepara el conocimiento que el práctico da por
supuesto y que se paga caro dudando con el reloj corriendo.

**Origen del contenido.** Preguntas **redactadas de cero** a partir del resumen de temario del
propio usuario (`guia/resumen_temario.tex`, Temas 16-21). No se transcribe ningún cuestionario
ajeno.

## Alcance

Un modelo nuevo en `exams.json`, `ofimatica_c2_2026`, con unas 48 preguntas repartidas con el
peso que tienen en el examen:

| Tema | Contenido | Preguntas |
| --- | --- | ---: |
| 16 | Windows 10, gestión de archivos y mantenimiento | ~7 |
| 17 | Word: configuración de página, estilos, párrafo, tabulaciones, objetos | ~12 |
| 18 | Excel: referencias, errores, jerarquía de operadores | ~12 |
| 19 | Access y Power BI | ~7 |
| 20 | Outlook: protocolos y archivos de datos | ~5 |
| 21 | OneDrive y SharePoint | ~5 |

Word y Excel pesan más porque son los que decide el ejercicio práctico.

## Formato de la explicación

Se reutiliza el modelo existente (`summary` + `points` + `source`), con una diferencia que
conviene dejar escrita:

* En legislación, `source.quote` reproduce el artículo literal. Los textos legales no son obra
  protegida, así que citarlos es seguro.
* En ofimática **no se copia documentación de Microsoft**. `source.label` nombra el concepto y la
  aplicación («Excel · referencias relativas, absolutas y mixtas») y `source.quote`, cuando
  aparece, es un enunciado propio de la regla, igual que ya se hace con las reglas de la RAE en el
  examen de aptitudes.

## Riesgo principal: un dato equivocado

La capa de explicaciones es lo que distingue a esta app. Una explicación errónea es peor que no
tener explicación, porque el opositor la memoriza. Medidas:

1. Cada pregunta sale de un punto concreto del resumen de temario. Si algo no está en el resumen
   y hace falta, se marca en la entrega en vez de colarlo en silencio.
2. Nada de preguntas sobre detalles que dependen de la versión del programa o de dónde cae un
   botón en la cinta, salvo que el temario lo fije explícitamente.
3. Se evitan los puntos donde el propio resumen es ambiguo. Por ejemplo, que el Liberador de
   espacio «no desinstala software» es cierto de la herramienta en sí, pero su pestaña *Más
   opciones* enlaza con el desinstalador: no se construye ninguna pregunta sobre esa frontera.

## Riesgo secundario: contenido sin validar

Hoy nada comprueba `exams.json`. Un `correctAnswerIndex` fuera de rango revienta en el
`init` de `Question` y el usuario ve «No se pudo cargar el examen», en producción. Añadir 48
preguntas escritas a mano sin red es pedirlo. Antes del contenido se añade un test que valida el
fichero real.

## Commands

```
Test:   ./gradlew :app:testDebugUnitTest
Web:    ./gradlew :app:compileKotlinWasmJs
```

## Testing Strategy

* `ExamsContentTest` (nuevo, en `androidUnitTest` porque necesita leer el fichero del disco):
  parsea el `exams.json` real y comprueba ids únicos, 4 opciones por pregunta,
  `correctAnswerIndex` en rango, explicación con `summary` no vacío, y que toda pregunta con
  opciones autorreferentes esté marcada con `lockOptionOrder`.
* El resto de la suite (46 tests) debe seguir en verde: el bloque nuevo no toca código de dominio.

## Boundaries

* **Siempre:** preguntas redactadas a partir del temario propio; 4 opciones; explicación con
  desglose; revisar si alguna opción es autorreferente y marcarla.
* **Preguntar antes:** ampliar a temas fuera del 16-21; cambiar el formato de `Explanation`.
* **Nunca:** copiar texto de documentación ajena; inventar una fuente que no se ha consultado;
  dar por buena una explicación que no se puede sostener.

## Success Criteria

1. La app ofrece un tercer modelo con las preguntas de ofimática, y se puede completar entero.
2. Toda pregunta tiene explicación con desglose.
3. `ExamsContentTest` valida los tres modelos y falla si se corrompe el contenido.
4. `:app:testDebugUnitTest` y `:app:compileKotlinWasmJs` en verde.

## Open Questions

* La estructura y el reparto de puntos del examen de 2026 no se han podido confirmar desde la
  Orden oficial: el calendario de la convocatoria viene con el texto protegido y no se extrae.
  Lo que se sabe de los Temas 16-21 sale del resumen del usuario, no del BOCM.
