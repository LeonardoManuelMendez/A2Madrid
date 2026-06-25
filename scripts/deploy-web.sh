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

# APK opcional: si existe landing/a2madrid.apk, el botón de descarga funcionará.
if [ ! -f build/site/a2madrid.apk ]; then
  echo "    (nota: no hay APK en landing/a2madrid.apk; el botón 'Descargar para Android' dará 404 hasta que lo añadas)"
fi

echo "==> 3/3 · Desplegando a Firebase Hosting…"
firebase deploy --only hosting

echo "==> Listo."
