# Spec: Selección de Oposición

Este documento define la especificación técnica para introducir una pantalla de selección de oposición en la aplicación A2Madrid.

---

## 1. Objetivo
Permitir al usuario elegir qué oposición desea preparar al abrir la aplicación, en lugar de ingresar directamente a los exámenes de Auxiliar C2.
* Se listarán 4 oposiciones específicas de la Comunidad de Madrid.
* 3 de ellas se mostrarán desactivadas (disabled) por no contar con exámenes disponibles en este momento.
* Solo la oposición de "Administración General, Grupo C, Subgrupo C2" estará activa y permitirá la navegación al listado de exámenes.

---

## 2. Detalles de las Oposiciones a Listar

| ID | Nombre de la Oposición | Estado |
| :--- | :--- | :--- |
| `c1_admin` | Cuerpo de Administrativos, de Administración General, Grupo C, Subgrupo C1 | **Desactivada** (Sin contenido) |
| `c2_auxiliar` | Administración General, Grupo C, Subgrupo C2 | **Activa** (Con contenido actual) |
| `a1_tecnico` | Técnicos Superiores de Administración General - A1 | **Desactivada** (Sin contenido) |
| `a2_gestion` | Técnicos de Gestión, A2 | **Desactivada** (Sin contenido) |

---

## 3. Impacto en la Arquitectura y Código

### Capa de Dominio (`domain`)
* **Modelo de Datos:** Crear la entidad `Opposition` en `domain/model/Opposition.kt`:
  ```kotlin
  data class Opposition(
      val id: String,
      val name: String,
      val isActive: Boolean
  )
  ```
* **Repositorio:** Proveer un método en el repositorio para listar las oposiciones. De momento se servirá una lista estática (mocked) en memoria.

### Capa de Presentación (`presentation`)
* **Rutas (`Routes.kt`):**
  * Definir `OppositionSelectionRoute` como `data object`.
  * Modificar `ExamSelectionRoute` para que acepte un argumento: `data class ExamSelectionRoute(val oppositionId: String)`.
* **Navegación (`A2MadridNavDisplay.kt`):**
  * Configurar `startDestination` del `NavHost` a `OppositionSelectionRoute`.
  * Configurar la navegación: `OppositionSelectionRoute` ➔ `ExamSelectionRoute(oppositionId)`.
  * Configurar el callback de retroceso en `ExamSelectionScreen` para poder volver atrás en el back stack a `OppositionSelectionRoute`.
* **Vistas (Screens):**
  * **`OppositionSelectionScreen`:** Nueva vista compuesta que renderiza el listado de oposiciones (usando el estado `isActive` para habilitar/deshabilitar los botones de selección).
  * **`ExamSelectionScreen`:** Modificar la vista para:
    * Recibir el `oppositionId` y mostrar la cabecera correspondiente.
    * Mostrar un botón de retroceso (`←`) en la barra superior o cabecera que ejecute `navController.popBackStack()`.

---

## 4. Criterios de Éxito

- [ ] Al iniciar la aplicación, la primera pantalla que ve el usuario es la de **Selección de Oposición**.
- [ ] Se listan de manera clara las 4 oposiciones especificadas en la sección 2.
- [ ] Los botones/tarjetas de las oposiciones C1, A1 y A2 se muestran visualmente deshabilitados (por ejemplo, con un canal alfa/opacidad reducida al 50% y sin efecto ripple de clic).
- [ ] El botón de la oposición C2 está completamente operativo, tiene respuesta visual al clic (ripple) y navega a la pantalla de selección de exámenes.
- [ ] Al estar en la pantalla de selección de exámenes de C2, hay un botón de retroceso visible (`←`) que permite volver a la selección de oposiciones.
- [ ] El código compila sin errores en Android, iOS y Web.
