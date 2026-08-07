# Despliegue

Cómo se publica A2Madrid: qué se genera, a dónde va y quién lo dispara.

## Resumen

| Artefacto | Destino | URL pública | Cuándo |
| --- | --- | --- | --- |
| Landing + app web (wasmJs) | Firebase Hosting | `https://a2madrid.web.app` | Cada push a `main` |
| APK de Android firmado | GitHub Releases, tag `android-latest` | `.../releases/download/android-latest/a2madrid.apk` | Cada push a `main` |

**No hay publicación en ninguna tienda.** Ni Google Play ni App Store. El APK se distribuye
directamente desde GitHub Releases y la app web desde Firebase. El código de iOS se compila en
CI (`ci.yml`, job `ios`) solo para verificar que la base común funciona en las tres plataformas;
nunca se sube nada a Apple.

## 1 · Web → Firebase Hosting

### Cómo se arma el sitio

Firebase publica el directorio `build/site`, que **no** está en el repositorio: se ensambla en
cada despliegue a partir de dos fuentes.

```text
build/site/
├── index.html, privacidad/…   ← copiado tal cual de landing/
└── app/                       ← copiado de app/build/dist/wasmJs/productionExecutable/
```

Es decir: la landing es HTML estático escrito a mano, y la app es el resultado de compilar el
target wasmJs con `./gradlew :app:wasmJsBrowserDistribution`.

### Configuración

- **`firebase.json`** — declara `public: build/site` y una cabecera que sirve los `.wasm` con
  `Content-Type: application/wasm`. Sin esa cabecera el navegador rechaza el módulo y la app no
  arranca.
- **`.firebaserc`** — asocia el alias `default` al proyecto `a2madrid`.

### Despliegue automático

`.github/workflows/deploy.yml`, job **`firebase-hosting`**. Se dispara en cada push a `main`.
Compila la web, ensambla `build/site` y despliega al canal `live`.

Dos detalles del job que parecen arbitrarios y no lo son:

- **Node 18, no 20.** Node 20 activa *network family autoselection* (Happy Eyeballs), que corta
  la conexión con el endpoint de token OAuth de Google y hace fallar la autenticación con
  «Premature close». De ahí también `NODE_OPTIONS: --dns-result-order=ipv4first`.
- **Reintentos.** El deploy se intenta hasta 3 veces con pausa: la API de Hosting falla de forma
  intermitente y un solo intento da rojos falsos.

Si falta el secreto `FIREBASE_SERVICE_ACCOUNT`, el job se salta a sí mismo y termina **en verde**
con un aviso, en lugar de fallar.

### Despliegue manual

```bash
./scripts/deploy-web.sh
```

Hace lo mismo en local: compila, ensambla `build/site` y despliega. Útil para publicar sin pasar
por un push.

### ⚠️ El proyecto va SIEMPRE explícito

Tanto el script como el workflow pasan `--project a2madrid` a `firebase deploy`. **No es
redundante con `.firebaserc`.** Sin ese flag, la CLI resuelve el *active project* buscando el
directorio actual y sus padres en `~/.config/configstore/firebase-tools.json`, y una entrada de
un directorio padre (por ejemplo `/home/usuario`) **gana** al alias `default` de `.firebaserc`.
El despliegue acabaría en otro proyecto. Ya ocurrió una vez.

## 2 · APK de Android → GitHub Releases

`.github/workflows/deploy.yml`, job **`apk-release`**. En cada push a `main`: restaura el keystore
desde los secretos, compila `:app:assembleRelease` y publica el APK firmado en el release del tag
rodante `android-latest`.

El botón «Descargar para Android» de la landing apunta a ese tag:

```text
https://github.com/LeonardoManuelMendez/A2Madrid/releases/download/android-latest/a2madrid.apk
```

La URL depende del **nombre** del tag, no del commit al que apunte, así que mover el tag no rompe
el enlace; borrarlo sí.

**Por qué no se aloja en Firebase:** GitHub Releases da ancho de banda gratis y sin límite de
descargas. Servir un APK de 14 MB desde Hosting sí consume cuota y puede generar coste.

Si faltan `KEYSTORE_BASE64` o `KEYSTORE_PASSWORD`, el job se salta y termina en verde.

## Secretos

Se configuran en *Settings → Secrets and variables → Actions*. Ninguno vive en el repositorio.

| Secreto | Job | Para qué |
| --- | --- | --- |
| `FIREBASE_SERVICE_ACCOUNT` | `firebase-hosting` | JSON de la service account con permiso de despliegue en Hosting. |
| `KEYSTORE_BASE64` | `apk-release` | El keystore de firma `.jks`, codificado en base64. |
| `KEYSTORE_PASSWORD` | `apk-release` | Contraseña del keystore y de la clave (`keyAlias: a2madrid`). |

El keystore nunca entra al repositorio: `.gitignore` bloquea `*.jks`, `*.keystore` y
`keystore.properties`. El job lo reconstruye en el runner y desaparece al terminar.

## Seguridad con el repositorio público

El repositorio es público. Lo que eso implica para el despliegue:

### Por qué un fork no puede desplegar

**Los secretos no se copian a los forks.** Son por repositorio. Quien forkee se lleva
`deploy.yml`, pero al ejecutarse en su repo el paso «¿Secreto configurado?» no encuentra
`FIREBASE_SERVICE_ACCOUNT` y el job se salta en verde. Para desplegar necesitaría su propio
proyecto de Firebase y su propia service account, y publicaría en su sitio, no en este.

**Un PR desde un fork tampoco.** GitHub ejecuta `pull_request` en el contexto de este repositorio
pero **retirando los secretos**, con un token de solo lectura, y exigiendo aprobación manual para
colaboradores nuevos. El patrón peligroso es `pull_request_target`, que aquí no se usa.

En un repositorio **público** no se puede desactivar el fork (GitHub solo lo permite en privados
y de organización) ni los pull requests. No hace falta: un fork es una copia de código ya
publicado, y no alcanza ni el Firebase, ni los Releases, ni los secretos.

### Lo demás que está bien por diseño

- **`ci.yml` no usa ningún secreto**: solo compila y pasa tests.
- **`deploy.yml` solo se dispara con `push` a `main` y `workflow_dispatch`**, y ambos exigen
  permiso de escritura en el repositorio.
- **Lo público es inofensivo**: el id de proyecto (`a2madrid`), `firebase.json` y la URL de
  Hosting no son credenciales. El certificado de firma que va dentro del APK publicado también es
  público por definición: va en cualquier app distribuida.

### Endurecimientos aplicados

- **Sin `--debug` en el deploy.** En un repositorio público **los logs de Actions son públicos**.
  GitHub enmascara los valores de los secretos que conoce, pero `--debug` ampliaba la superficie
  sin aportar nada con el deploy ya estable.
- **Acciones de terceros fijadas por SHA de commit**, no por etiqueta móvil. Si alguien
  comprometiera la etiqueta `v2` de una acción, su código correría con acceso a los secretos del
  job; un SHA es inmutable. Cada una lleva su versión en un comentario al lado. Las de
  `actions/*` siguen por etiqueta: las publica el mismo GitHub que ejecuta la plataforma.
- **`permissions: contents: read`** a nivel de workflow en ambos ficheros; `apk-release` se
  concede el `contents: write` que necesita para publicar en Releases. El valor por defecto del
  repositorio ya era `read`, así que esto es defensa en profundidad, no la corrección de un
  permiso excesivo: deja el mínimo privilegio escrito en el propio workflow.

**Contrapartida de fijar por SHA:** dejan de llegar las correcciones de las acciones. Conviene
revisarlas cada pocos meses, o configurar Dependabot (`.github/dependabot.yml`, ecosistema
`github-actions`) para que avise — eso sí, Dependabot funciona abriendo pull requests.

### Pendiente, fuera del repositorio

La service account de Firebase debería tener solo el rol *Firebase Hosting Admin*, no *Editor* ni
*Owner*. Se comprueba en la consola de Google Cloud (IAM), no aquí.

## Verificar un despliegue

```bash
# Estado de los workflows
gh run list --limit 5

# ¿Responde la web?
curl -sI https://a2madrid.web.app | head -1

# ¿Sigue vivo el APK que enlaza la landing?
curl -sSL -o /dev/null -w '%{http_code} · %{size_download} bytes\n' \
  https://github.com/LeonardoManuelMendez/A2Madrid/releases/download/android-latest/a2madrid.apk
```
