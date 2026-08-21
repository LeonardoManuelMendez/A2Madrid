# Plan de Implementación: Selección de Oposición

Este plan detalla los pasos para reestructurar la navegación de la aplicación A2Madrid e introducir la pantalla de Selección de Oposición.

---

## 1. Arquitectura y Componentes
Siguiendo las pautas de **Clean Architecture**, la funcionalidad se dividirá en tres niveles:
1. **Dominio:**
   * Entidad de datos `Opposition`.
   * Interfaz de repositorio extendida para proveer la lista de oposiciones.
   * Caso de uso `GetOppositionsUseCase`.
2. **Navegación:**
   * Reconfiguración del host de navegación para cambiar el destino de inicio.
   * Ajuste de rutas serializables para propagar el `oppositionId`.
3. **Presentación:**
   * Componentes Compose de UI para la pantalla de selección.
   * ViewModel asociado para proveer las oposiciones de manera reactiva e independiente de la UI.
   * Modificación de la pantalla de selección de exámenes para admitir retroceso y cabecera dinámica.

---

## 2. Mitigación de Riesgos
* **Riesgo 1: Error de compilación en serialización de rutas.**
  * *Mitigación:* Usar el serializador oficial de Kotlin (`kotlinx.serialization`) con `@Serializable` para todos los destinos de Compose Navigation, tal como ya lo implementa el proyecto actual.
* **Riesgo 2: Romper compatibilidad multiplataforma (Android, iOS y Web).**
  * *Mitigación:* No utilizar ninguna API nativa específica (como Android Context o iOS Foundation clases). Mantener toda la lógica dentro del source set `commonMain` con Compose Multiplatform común.
* **Riesgo 3: Acoplamiento de datos.**
  * *Mitigación:* Aunque los datos de las oposiciones inactivas son simulados, se servirán a través del repositorio y del caso de uso. De esta forma, si mañana se conectan a un archivo local JSON o Firebase, el cambio será transparente para las vistas.

---

## 3. Puntos de Verificación (Checkpoints)
* **Checkpoint 1:** Compilación exitosa tras modificar las rutas de navegación.
* **Checkpoint 2:** Verificación visual de los 4 botones de oposición en la pantalla inicial (3 de ellos correctamente desactivados).
* **Checkpoint 3:** Comprobación del correcto funcionamiento del botón de volver atrás (`←`) al navegar a la pantalla de exámenes.
