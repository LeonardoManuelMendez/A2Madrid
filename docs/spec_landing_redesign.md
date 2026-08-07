# Spec: Rediseño de la Landing Page (UI/UX)

Este documento define la especificación para el rediseño completo de la página de aterrizaje (landing page) de la aplicación A2Madrid.

---

## 1. Objetivos

1. **Mensaje Multi-Oposición:** Actualizar todos los textos para reflejar que la app sirve para preparar múltiples oposiciones de la Comunidad de Madrid (C1, C2, A1, A2).
2. **Interactividad (UX):** Convertir el mockup estático de teléfono en un simulador interactivo donde el usuario pueda hacer clic en una pregunta de prueba y ver la validación en tiempo real.
3. **Estética Visual (UI):** Implementar la tipografía "Outfit", efectos de degradado suave (glows), bordes más finos, y microanimaciones interactivas.
4. **Accesibilidad (a11y) y SEO:** Asegurar un contraste WCAG AA mínimo de 4.5:1 utilizando un tono carmesí accesible (#c8102e), añadir metadatos actualizados y estructurar semánticamente los encabezados.

---

## 2. Paleta de Colores y Tipografía

* **Tipografía:** "Outfit" (Google Fonts) para títulos y cuerpo de texto para dar un aspecto premium y moderno.
* **Colores principales:**
  * `--carmesi`: `#c8102e` (Rojo de la Comunidad de Madrid, verificado para cumplir contraste de 4.6:1 sobre fondo blanco).
  * `--carmesi-dark`: `#8b0000`.
  * `--glow`: `rgba(200, 16, 46, 0.04)` (para fondos degradados).
  * `--surface-glass`: `rgba(255, 255, 255, 0.85)` (con `backdrop-filter: blur(12px)` para componentes tipo tarjeta).

---

## 3. Comportamiento del Mockup Interactivo de Teléfono

El teléfono móvil en la cabecera será un minijuego/demo con el siguiente flujo de estados:
* **Estado Inicial:** Se muestra la pregunta: *"¿Qué oposición permite el acceso con el título de Bachiller o equivalente?"* (Pregunta típica de legislación C1/C2).
  * Opciones: A) Subgrupo C2, B) Subgrupo C1, C) Grupo B, D) Subgrupo A2.
* **Estado de Selección:**
  * Si el usuario hace clic en una opción incorrecta (como la A, C o D): se añade la clase `.incorrect` (fondo rosa suave, borde rojo, vibración con CSS shake) y no se realiza acción.
  * Si el usuario hace clic en la opción correcta (B):
    1. Se añade la clase `.correct` con fondo verde suave y borde verde.
    2. Se llena la barra de progreso al 100%.
    3. Aparece una tarjeta explicativa en la parte inferior del mockup detallando el Artículo 76 del EBEP.

---

## 4. Criterios de Éxito

- [ ] La landing page tiene un titular centrado en preparar múltiples oposiciones de la Comunidad de Madrid.
- [ ] La tipografía cargada es "Outfit" y los encabezados son semánticamente correctos.
- [ ] El contraste del color rojo utilizado para textos es accesible (cumple WCAG AA).
- [ ] El mockup de teléfono responde de forma interactiva e inmediata a los clics del usuario:
  * Al hacer clic en la opción correcta, se activa la visualización correcta (verde) y se despliega la tarjeta de explicación.
  * Al hacer clic en las opciones incorrectas, se activa la visualización incorrecta (rojo) con efecto shake de vibración.
- [ ] La landing page es 100% responsiva (se visualiza perfectamente en smartphones, tablets y ordenadores).
- [ ] La velocidad de carga y rendimiento siguen siendo excelentes (sin dependencias pesadas de frameworks de JavaScript).
