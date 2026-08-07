#!/usr/bin/env bash
# Construye el target web (wasmJs) y ensambla el sitio (landing + app) para Firebase Hosting,
# luego despliega.
set -euo pipefail
cd "$(dirname "$0")/.."

# El proyecto va SIEMPRE explícito. Sin --project, la CLI resuelve el "active project" buscando
# el directorio actual y sus padres en ~/.config/configstore/firebase-tools.json, y una entrada
# de un directorio padre (p. ej. /home/usuario) gana al alias `default` de .firebaserc: el
# despliegue acabaría en otro proyecto. Ya pasó una vez.
FIREBASE_PROJECT="${FIREBASE_PROJECT:-a2madrid}"

echo "==> 1/3 · Compilando la web (wasmJs, producción)…"
./gradlew :app:wasmJsBrowserDistribution

echo "==> 2/3 · Ensamblando build/site (landing + app)…"
rm -rf build/site
mkdir -p build/site/app
cp -r landing/. build/site/
cp -r app/build/dist/wasmJs/productionExecutable/. build/site/app/

# Nota: el APK NO se aloja aquí. El botón "Descargar para Android" apunta a GitHub Releases
# (releases/latest/download/a2madrid.apk), con ancho de banda gratis y sin riesgo de coste.

echo "==> 3/3 · Desplegando a Firebase Hosting (proyecto: $FIREBASE_PROJECT)…"
firebase deploy --only hosting --project "$FIREBASE_PROJECT"

echo "==> Listo."
