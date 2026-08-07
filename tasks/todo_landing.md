# Lista de Tareas: Rediseño de la Landing Page

Esta es la lista de tareas específicas para la remodelación de la landing page.

---

- [x] **Tarea 1: Actualización de Contenidos y SEO (HTML)**
  - **Aceptación:**
    * Actualizar títulos y meta descripciones del encabezado de `index.html` para incorporar el enfoque multi-oposición (C1, C2, A1, A2).
    * Modificar textos del Hero, tarjetas de características y la sección de Android.
  - **Verificación:** Abrir el archivo y validar visualmente los textos modificados.
  - **Archivos:**
    * `landing/index.html` (modificado)

- [x] **Tarea 2: Diseño Visual y Estilos (CSS)**
  - **Aceptación:**
    * Importar la fuente "Outfit" en el `<head>`.
    * Actualizar la sección de estilos (`<style>`) para usar la fuente, el rojo carmesí accesible (`#c8102e`), gradientes radiales de fondo y sombras suaves.
    * Crear las clases CSS `.correct`, `.incorrect`, la animación `@keyframes shake` y la transición para la tarjeta explicativa dentro del mockup.
  - **Verificación:** Comprobar que el diseño se ve moderno, el contraste de color cumple con WCAG AA, y las clases CSS están presentes.
  - **Archivos:**
    * `landing/index.html` (modificado)

- [x] **Tarea 3: Implementar Interactividad del Mockup (JS)**
  - **Aceptación:**
    * Agregar código JavaScript (Vanilla) al final del archivo HTML que controle el evento de clic de las opciones del mockup.
    * Si el usuario hace clic en A, C o D: aplicar la clase `.incorrect`, ejecutar el shake y limpiar al cabo de 600ms.
    * Si el usuario hace clic en B: aplicar la clase `.correct`, llenar la barra de progreso, mostrar un icono de check y deslizar la tarjeta de explicación técnica en la parte inferior del mockup.
  - **Verificación:** Probar haciendo clics en local sobre el mockup en un navegador para asegurar que las respuestas incorrectas vibran en rojo y la correcta se colorea en verde mostrando la tarjeta explicativa.
  - **Archivos:**
    * `landing/index.html` (modificado)
