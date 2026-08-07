# Lista de Tareas: Selección de Oposición

Esta lista de tareas describe las acciones de desarrollo necesarias para implementar la selección de oposición, ordenadas por su orden de dependencia.

---

- [x] **Tarea 1: Modelo de Dominio `Opposition` y Métodos de Repositorio**
  - **Aceptación:** 
    * Se crea la data class `Opposition(val id: String, val name: String, val isActive: Boolean)`.
    * La interfaz `QuizRepository` declara `suspend fun getOppositions(): List<Opposition>`.
    * `QuizRepositoryImpl` implementa dicho método y devuelve una lista estática con las 4 oposiciones especificadas (C1, C2, A1, A2), donde solo C2 está activa (`isActive = true`).
  - **Verificación:** Ejecutar `./gradlew compileKotlinMetadata` o compilar el proyecto para verificar que no hay errores de sintaxis en el repositorio.
  - **Archivos:**
    * `domain/model/Opposition.kt` (nuevo)
    * `domain/repository/QuizRepository.kt` (modificado)
    * `data/repository/QuizRepositoryImpl.kt` (modificado)

- [x] **Tarea 2: Caso de Uso y Registro DI (Koin)**
  - **Aceptación:**
    * Se crea `GetOppositionsUseCase` que inyecta `QuizRepository` y llama a `getOppositions()`.
    * Se registra el caso de uso en `di/Koin.kt` usando `factoryOf(::GetOppositionsUseCase)`.
  - **Verificación:** Ejecutar la compilación del proyecto para asegurar el correcto enlazado de dependencias.
  - **Archivos:**
    * `domain/usecase/GetOppositionsUseCase.kt` (nuevo)
    * `di/Koin.kt` (modificado)

- [x] **Tarea 3: Reconfigurar Rutas y Host de Navegación**
  - **Aceptación:**
    * `Routes.kt` añade `@Serializable data object OppositionSelectionRoute`.
    * `Routes.kt` cambia `ExamSelectionRoute` a `@Serializable data class ExamSelectionRoute(val oppositionId: String)`.
    * `A2MadridNavDisplay.kt` actualiza `startDestination = OppositionSelectionRoute`.
    * `A2MadridNavDisplay.kt` añade el destino `composable<OppositionSelectionRoute>` y actualiza `composable<ExamSelectionRoute>` para pasarle el argumento `oppositionId`.
  - **Verificación:** Ejecutar `./gradlew compileKotlinMetadata` para comprobar que las rutas compilan y la deserialización funciona correctamente.
  - **Archivos:**
    * `presentation/navigation/Routes.kt` (modificado)
    * `presentation/navigation/A2MadridNavDisplay.kt` (modificado)

- [x] **Tarea 4: Crear la UI y ViewModel de Selección de Oposición**
  - **Aceptación:**
    * Crear `OppositionSelectionViewModel` exponiendo un estado reactivo con la lista de oposiciones.
    * Crear `OppositionSelectionScreen` renderizando una cabecera con el logotipo/texto y una columna con los 4 botones/tarjetas de las oposiciones.
    * Los ítems inactivos (C1, A1, A2) se muestran visualmente deshabilitados (opacidad reducida, no clickables). El ítem de C2 está activo y navega a la selección de exámenes.
    * Registrar el ViewModel en `di/Koin.kt` con `viewModelOf(::OppositionSelectionViewModel)`.
  - **Verificación:** Lanzar la aplicación en local (web, android, o emulador) y comprobar que el renderizado inicial muestra los 4 botones y solo C2 responde a la acción del clic.
  - **Archivos:**
    * `presentation/oppositionselection/OppositionSelectionViewModel.kt` (nuevo)
    * `presentation/oppositionselection/OppositionSelectionScreen.kt` (nuevo)
    * `di/Koin.kt` (modificado)

- [x] **Tarea 5: Adaptar la Pantalla de Selección de Exámenes**
  - **Aceptación:**
    * Modificar `ExamSelectionScreen` para admitir una acción de retroceso `onBack: () -> Unit` y mostrar un botón `ArrowBack` en la barra superior o cabecera.
    * Conectar la acción en `A2MadridNavDisplay.kt` de modo que al pulsar atrás vuelva a la selección de oposiciones.
    * Mostrar la cabecera correspondiente a la oposición seleccionada de forma dinámica en base al `oppositionId` recibido.
  - **Verificación:** Navegar a la pantalla de exámenes de C2 y pulsar el botón de volver (`←`). Asegurarse de que regresa limpiamente a la pantalla de selección de oposición.
  - **Archivos:**
    * `presentation/examselection/ExamSelectionScreen.kt` (modificado)
    * `presentation/navigation/A2MadridNavDisplay.kt` (modificado)
