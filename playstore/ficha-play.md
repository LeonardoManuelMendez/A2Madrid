# Ficha de Google Play — A2Madrid

Datos listos para copiar/pegar en Play Console. Paquete: `io.github.leonardomanuelmendez.a2madrid`.

## Identidad
- **Nombre de la app** (máx. 30): `A2Madrid: Tests oposición`
- **Categoría**: Educación
- **Etiquetas**: oposiciones, tests, educación
- **Tipo**: Aplicación · Gratuita · Sin compras integradas · Sin anuncios

## Descripción breve (máx. 80 caracteres)
```
Tests gratis para preparar la oposición de Auxiliar de la Comunidad de Madrid.
```

## Descripción completa (máx. 4000 caracteres)
```
A2Madrid es una app gratuita para preparar la oposición de Auxiliar (C2) de la Comunidad de Madrid. Practica con tests del examen oficial, con corrección al instante y la explicación de cada respuesta.

🎁 Es un regalo para quienes están opositando: sin registro, sin anuncios y sin coste.

QUÉ INCLUYE
• Corrección inmediata: confirmas cada respuesta y ves al momento si es correcta, con su explicación.
• Examen oficial: modelos basados en el examen real (psicotécnico, ortografía y legislación).
• Historial y récords: guarda tus puntuaciones, destaca tu mejor marca por modelo y mide tu progreso.
• Sin conexión: una vez instalada, funciona sin internet.
• Privacidad total: no se recoge ningún dato; tus puntuaciones se guardan solo en tu dispositivo.

También está disponible en el navegador (cualquier dispositivo) en a2madrid.web.app.

Hecho con cariño para quienes preparan esta oposición.
```

## Política de privacidad (obligatoria)
- URL: **https://a2madrid.web.app/privacidad/**

## Seguridad de los datos (formulario "Data safety")
- ¿La app recopila o comparte datos de usuario? → **No**
  - (Las puntuaciones se guardan solo en el dispositivo; según la definición de Google eso no es "recopilar", porque no salen del dispositivo.)
- ¿Cifra los datos en tránsito? → No aplica (no envía datos).
- ¿Se pueden eliminar los datos? → Sí, desde la propia app (Puntuaciones → borrar) o desinstalando.

## Clasificación de contenido (cuestionario IARC)
- App educativa/de tests, sin violencia, sexo, lenguaje ni sustancias → resultado esperado: **PEGI 3 / Para todos**.

## Recursos gráficos (carpeta playstore/)
- **Icono 512×512**: `playstore/icon-512.png`
- **Gráfico de funciones 1024×500**: `playstore/feature-1024x500.png`
- **Capturas** (mín. 2, te faltan): haz capturas de la app (selección de modelo + una pregunta con feedback). Sugerencia: usa la web (a2madrid.web.app/app/) o el emulador.

## Pasos para la primera publicación (manual, en Play Console)
1. Crear la app con el paquete `io.github.leonardomanuelmendez.a2madrid` (¡este id es definitivo!).
2. Rellenar ficha (textos de arriba), icono, gráfico de funciones y capturas.
3. Completar "Seguridad de los datos" y clasificación de contenido, y pegar la URL de privacidad.
4. Subir el **AAB** (genéralo con: Actions → Deploy → Run workflow → descarga el artefacto `a2madrid-play-aab`; o en local `./gradlew :app:bundleRelease`).
5. Empezar por la pista de **pruebas internas** y luego promover a producción.

> Para actualizaciones futuras: sube la `versionCode` (en `app/build.gradle.kts`, ahora 1) en cada release. La subida puede automatizarse con el secreto `PLAY_SERVICE_ACCOUNT_JSON` (ver workflow deploy.yml, job `aab-play`).
