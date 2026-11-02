# I Don’t Know
Un diario donde cada noticia es una probabilidad. Proyecto CS2031 DBP, UTEC, 2026-2.

Repositorio del proyecto completo: [idontknow-frontend](https://github.com/Tuskanip2002/idontknow-frontend).

## Equipo
- Rance Blondet Borja
- Hideki Aldo Kunigami Chia
- Sergio Peña Andia
- Joseph Geraldo Soto

## Estructura
- `backend/`: backend del equipo basado en Spring Boot 4.1.1 y Java 21, con extensiones para sesiones renovables, producción y demostración.
- `web/`: React 19, TypeScript, Vite y React Router.
- `mobile/`: React Native, Expo SDK 54 y React Navigation (stack y bottom tabs).
- `packages/api/`: contratos, cliente Axios e interceptores compartidos.
- `docs/`: cobertura de rúbrica, despliegue, arquitectura y preparación de la exposición.

El backend original procede de [jg42-2/idontknow-backend](https://github.com/jg42-2/idontknow-backend), revisión `b3e9e13683529129ab4a8f2548480acba82ab12b`. Se conserva su licencia MIT. Este directorio es una copia de fuentes, no un clon con el historial Git del repositorio remoto.

## Inicio local sin cuentas cloud
Requisitos: Node.js 22 o superior y Java 21. Desde esta carpeta:

```powershell
npm ci
```

En una terminal, inicia el backend aislado:

```powershell
cd backend
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=demo'
```

En otra terminal, desde la raíz:

```powershell
npm run dev:web
```

Abre la URL que imprime Vite, normalmente http://127.0.0.1:5173. El backend estará en http://localhost:8080/api/v1.

El perfil `demo` usa una base H2 persistente independiente en `backend/.local/`. Crea 24 mercados, 16 ediciones y dos cuentas de demostración. No modifica PostgreSQL ni consulta Polymarket o correo. Una franja identifica los datos de ejemplo en las interfaces: **no son probabilidades reales**.

| Cuenta local | Contraseña local | Rol |
| --- | --- | --- |
| lector@demo.local | LectorDemo123 | USER |
| admin@demo.local | AdminDemo123 | ADMIN |

Estas cuentas se crean únicamente con el perfil `demo`. No se deben usar como credenciales de producción.

## App móvil
1. Copia `mobile/.env.example` a `mobile/.env`.
2. Cambia `EXPO_PUBLIC_API_URL` por la IP de tu computadora: `http://TU_IP:8080/api/v1`. El teléfono y la computadora deben compartir red. En un teléfono, `localhost` significa el teléfono.
3. Configura `EXPO_PUBLIC_WEB_URL` con el origen exacto de la web. El lector QR verifica ese origen.
4. Ejecuta `npm run dev:mobile` y abre el proyecto con un desarrollo compatible con SDK 54 o un development build. La aplicación Expo Go más reciente puede exigir un SDK distinto; para evitar esa dependencia se puede generar el APK con EAS.
5. Si deseas abrir también la web desde el teléfono: `npm run dev -w web -- --host 0.0.0.0`. Añade ese origen a `CORS_ALLOWED_ORIGINS` del backend antes de arrancarlo.

La cámara abre titulares/mercados por QR. El acelerómetro permite pasar los titulares con una inclinación lateral, activada voluntariamente. Se mantienen el enlace manual y los botones Anterior/Siguiente como alternativas.

## PostgreSQL y Docker
Copia `.env.example` a `.env` y completa los valores. Genera un secreto local con:

```powershell
node -e "console.log(require('node:crypto').randomBytes(48).toString('base64'))"
docker compose up --build
```

La web sirve en http://localhost:3000 y el backend en http://localhost:8080/api/v1. PostgreSQL usa un volumen propio y no publica su puerto al host. Docker Desktop debe estar iniciado. Esta configuración trabaja con datos reales, inicialmente sin mercados hasta ejecutar la sincronización diaria; no activa el perfil de demostración.

Correo y titulares traducidos requieren las credenciales de SMTP y Groq. El administrador inicial se configura con `ADMIN_EMAIL` y `ADMIN_PASSWORD`; no se incluye una cuenta administrativa pública por defecto.

## Comprobaciones
```powershell
npm run typecheck
npm test
npm run build
cd backend
.\mvnw.cmd verify '-Dspring.profiles.active=test'
cd ../mobile
npx expo export --platform android
```

`npm run build` genera la web; `expo export` comprueba el bundle Android, **no crea un APK ni publica la app**. Las pruebas Java incluyen HTTP real con una base aislada. No prueban la infraestructura PostgreSQL de producción ni sensores físicos.

Consulta [despliegue](docs/DEPLOYMENT.md), [rúbrica](docs/RUBRIC.md), [arquitectura](docs/ARCHITECTURE.md) y [guion de exposición](docs/PRESENTATION.md). La presentación editable está en output/presentation/.

## Control de versiones
No se crearon commits ni se hizo push. La configuración CI incluida correrá cuando el equipo autorice subir el código. Antes de cualquier commit se debe revisar el conjunto de archivos y el mensaje propuesto; el push requiere una confirmación explícita independiente.
