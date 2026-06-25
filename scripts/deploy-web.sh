#!/usr/bin/env bash
# Construye el target web (wasmJs) y ensambla el sitio (landing + app) para Firebase Hosting,
# luego despliega. Requiere haber elegido el proyecto antes: `firebase use <project-id>`.
set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> 1/3 · Compilando la web (wasmJs, producción)…"
./gradlew :app:wasmJsBrowserDistribution

echo "==> 2/3 · Ensamblando build/site (landing + app)…"
rm -rf build/site
mkdir -p build/site/app
cp -r landing/. build/site/
cp -r app/build/dist/wasmJs/productionExecutable/. build/site/app/

# Nota: el APK NO se aloja aquí. El botón "Descargar para Android" apunta a GitHub Releases
# (releases/latest/download/a2madrid.apk), con ancho de banda gratis y sin riesgo de coste.

echo "==> 3/3 · Desplegando a Firebase Hosting…"
firebase deploy --only hosting

echo "==> Listo."
