# Plan de Implementación: Rediseño de la Landing Page

Este plan detalla los pasos para aplicar el rediseño completo de la landing page de A2Madrid.

---

## 1. Fases del Rediseño

1. **Estructura y Contenido (HTML):**
   * Actualizar el título de la página, descripción meta, y etiquetas Open Graph.
   * Cambiar los textos del Hero y secciones de características para cubrir C1, C2, A1, A2.
   * Modificar el marcado del mockup del teléfono para permitir la interactividad mediante clases CSS.
2. **Estilo Visual (CSS):**
   * Importar la tipografía "Outfit" desde Google Fonts.
   * Actualizar las variables de color CSS, estableciendo el tono carmesí accesible (`#c8102e`) y sombras/degradados más suaves.
   * Añadir clases para los estados `.correct`, `.incorrect` y la animación `.shake`.
   * Estilizar la tarjeta de explicación dinámica del mockup de teléfono.
3. **Interactividad (JavaScript):**
   * Añadir un script de JS plano al final del archivo HTML que controle el flujo del simulador interactivo del mockup.

---

## 2. Mitigación de Riesgos
* **Riesgo 1: Degradación de rendimiento (LCP / FCP).**
  * *Mitigación:* No utilizar frameworks pesados (como React, Tailwind o bibliotecas externas). Todo se implementará en un único archivo HTML autocontenido con Vanilla CSS y Vanilla JS. La tipografía se importará usando `<link rel="preconnect">` para optimizar su velocidad de carga.
* **Riesgo 2: Fallo de retrocompatibilidad en navegadores móviles.**
  * *Mitigación:* Utilizar propiedades CSS estándar y seguras. Para la interactividad se usará JS ES6 básico soportado por todos los navegadores modernos.
