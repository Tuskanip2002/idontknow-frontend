# I Don’t Know
Un diario donde cada noticia es una probabilidad. Proyecto CS2031 DBP, UTEC, 2026-2.

Repositorio del proyecto completo: [idontknow-frontend](https://github.com/Tuskanip2002/idontknow-frontend).

## Enlaces de entrega

- [Web pública](https://18-223-255-39.sslip.io/)
- [Estado de la API](https://18-223-255-39.sslip.io/api/v1/estado)
- [APK de Android en Expo](https://expo.dev/accounts/tuskanips-team/projects/idontknow-dbp/builds/dcb8354c-bf62-45cd-b983-68365e6959c6)

La web y el backend se ejecutan en Amazon EC2 con Docker Compose. Caddy sirve la web y la API por HTTPS en el mismo dominio. El backend guarda los datos en PostgreSQL privado en Amazon RDS. La configuración de los contenedores está en [compose.aws.yaml](compose.aws.yaml).

Para usar la web, crea una cuenta de lector desde [Registrarse](https://18-223-255-39.sslip.io/registro). En Android, instala el APK desde Expo e inicia sesión con esa misma cuenta. En iPhone, abre la web en Safari.

La compilación del APK está lista. Queda pendiente probar en un Android físico el inicio de sesión, la persistencia al reabrir la app, el escáner QR y la lectura por inclinación.

La web pública fue revisada por Rance. Las cuatro categorías tienen mercados y el detalle permite leer una versión en español. Consulta las [evidencias de entrega](docs/EVIDENCIAS.md) para ver las comprobaciones y mediciones de la API. La prueba Android está asignada a los compañeros del equipo. El envío de correo sigue pendiente de configuración SMTP.

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

## Desarrollo web local (opcional)
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

El perfil `demo` carga datos de ejemplo en una base H2 bajo `backend/.local/`. Usa el registro de la web local para crear una cuenta de desarrollo. Para probar el rol de administrador local, consulta las cuentas que inicializa [DemoData.java](backend/src/main/java/utec/idontknowbackend/config/DemoData.java). La web pública usa su propia base en AWS.

## Desarrollo móvil local
1. Copia `mobile/.env.example` a `mobile/.env`.
2. Cambia `EXPO_PUBLIC_API_URL` por la IP de tu computadora: `http://TU_IP:8080/api/v1`. El teléfono y la computadora deben compartir red. En un teléfono, `localhost` significa el teléfono.
3. Configura `EXPO_PUBLIC_WEB_URL` con el origen exacto de la web. El lector QR verifica ese origen.
4. Ejecuta `npm run dev:mobile` y abre el proyecto con un desarrollo compatible con SDK 54 o un development build. La aplicación Expo Go más reciente puede exigir un SDK distinto; para evitar esa dependencia se puede generar el APK con EAS.
5. Si deseas abrir también la web desde el teléfono: `npm run dev -w web -- --host 0.0.0.0`. Añade ese origen a `CORS_ALLOWED_ORIGINS` del backend antes de arrancarlo.

La cámara abre titulares/mercados por QR. El acelerómetro permite pasar los titulares con una inclinación lateral, activada voluntariamente. Se mantienen el enlace manual y los botones Anterior/Siguiente como alternativas.

## PostgreSQL y Docker local (opcional)
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

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## Entregables de la fase 2
 
| Componente | Tecnología | Acceso |
| --- | --- | --- |
| Backend (API REST) | Spring Boot 4.1.1 · Java 21 · PostgreSQL | [Estado de la API](https://18-223-255-39.sslip.io/api/v1/estado) · código en [`backend/`](backend/) (origen: [jg42-2/idontknow-backend](https://github.com/jg42-2/idontknow-backend)) |
| Frontend web | React 19 · TypeScript · Vite · React Router | [Web pública](https://18-223-255-39.sslip.io/) · código en [`web/`](web/) |
| Frontend móvil | React Native · Expo SDK 54 · React Navigation | [APK de Android en Expo](https://expo.dev/accounts/tuskanips-team/projects/idontknow-dbp/builds/dcb8354c-bf62-45cd-b983-68365e6959c6) · código en [`mobile/`](mobile/) |
| Deployment | AWS EC2 + Docker Compose · Caddy (HTTPS) · Amazon RDS (PostgreSQL) · Expo EAS | Configuración en [`compose.aws.yaml`](compose.aws.yaml) y [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md) |
 
## Cobertura de la rúbrica
 
| Criterio | Qué implementamos | Dónde verlo |
| --- | --- | --- |
| Mobile: integración con el backend | Todas las funciones principales consumen nuestra API REST. JWT guardado en Expo SecureStore. Headers `Authorization` y `Content-Type` en cada request e interceptores centralizados. | `mobile/src/services/api.ts`, `packages/api/src/client.ts` |
| Mobile: arquitectura | Carpetas `screens`, `components`, `services`, `hooks` y `contexts`. Custom hooks (`useQuery`, `useTiltReader`), Context API y React Navigation con stack y pestañas. | `mobile/src/` |
| Mobile: sensores y API externa | Cámara (lector QR) y acelerómetro (lectura por inclinación), con permisos y alternativas si no hay sensor o permiso. API externa Gamma de Polymarket como contexto complementario. | `mobile/src/screens/Sensors.tsx`, `mobile/src/hooks/useTiltReader.ts` |
| Mobile: errores y carga | Mensajes amigables por código HTTP, estados de carga, reintento de GET ante fallas de red, validaciones y botones bloqueados durante el envío. | `mobile/src/components/ui.tsx` (`QueryState`) |
| Web: integración con el backend | Axios con `baseURL`, headers e interceptores centralizados. JWT solo en memoria y refresh token en cookie HttpOnly con rotación. `AbortController` para cancelar requests. | `web/src/services/api.ts`, `packages/api/src/client.ts`, `web/src/hooks/useQuery.ts` |
| Web: componentes y arquitectura | Más de 10 componentes reutilizables (`Button`, `Field`, `Pagination`, `QueryState`, `EmptyState`, `Dialog`, `HeadlineCard`, `HistoryChart`, entre otros). Custom hooks, Context API (`AuthProvider`, `ToastProvider`) y props tipadas con TypeScript. | `web/src/components/`, `web/src/contexts/` |
| Web: routing | React Router con rutas dinámicas (`mercados/:id`, `titulares/:id`, `archivo/:date`), query params (`page`, `size`, `q`, `categoria`, `sort`), rutas protegidas, `React.lazy` + `Suspense` y página 404. | `web/src/App.tsx` |
| Web: errores, carga y validaciones | `ErrorBoundary`, mensajes diferenciados por código (400, 401, 403, 404, 409, 500), estados de carga, toasts de confirmación, validaciones de formularios y reintento seguro solo para GET. | `web/src/components/ui.tsx`, `packages/api/src/client.ts` |
| UI/UX | Diseño responsive con estilos por variables, tema oscuro, iconos consistentes, búsqueda con debounce, filtros y orden, confirmación de acciones destructivas y estados vacíos con acción. | `web/src/styles.css`, `web/src/pages/` |
| Vistas | Portada, registro, login, mercados, detalle, archivo, edición, guardados, perfil, temas, administración y acerca de. | `web/src/pages/` |
| Paginación | En `/mercados` y `/ediciones`, con anterior/siguiente y números de página, tamaños 10/25/50/100, total de resultados y estado guardado en la URL. | `web/src/pages/Markets.tsx`, `web/src/pages/Archive.tsx` |
| Deployment | Backend y web en AWS EC2 con Docker Compose, Caddy con HTTPS, PostgreSQL en Amazon RDS y APK distribuido con Expo. Variables de entorno en el servidor, sin secretos en el repositorio. Latencia media medida entre 130 y 300 ms en los endpoints principales. | [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md), [`docs/EVIDENCIAS.md`](docs/EVIDENCIAS.md) |
 
## Equipo y participación
 
El porcentaje indica el grado de cumplimiento de las responsabilidades de cada integrante, de 0 % a 100 %, donde 100 % representa el cumplimiento completo, según la rúbrica del curso.
 
| Integrante | Código | Principales aportes | Participación |
| --- | --- | --- | --- |
| Joseph Geraldo Soto | 202510412 | **Backend.** Diseño e implementación de la API REST en Spring Boot con arquitectura por funcionalidad (model, DTO, application, infrastructure). Entidades Usuario, Categoría, Mercado, Snapshot, Edición, Titular y Guardado, con sus servicios, controladores y manejo global de excepciones. Integración con la API de Polymarket y con Groq para traducir titulares. Sincronización diaria programada y armado de la portada por puntaje (`ScoreCalculator`, `PortadaBuilderService`). Envío de correos mediante eventos. Configuración de PostgreSQL y variables de entorno, pruebas unitarias y colección de Postman. | 100 % |
| Rance Blondet Borja | 202110360 | **Frontend.** Aplicación web en React con TypeScript: vistas, rutas dinámicas y protegidas, carga diferida, paginación con query params, manejo de errores y estados de carga. App móvil en React Native con Expo: navegación por stack y pestañas, sesión en SecureStore, lector QR con la cámara, lectura por inclinación con el acelerómetro y contexto de la API de Polymarket. Cliente Axios compartido con interceptores, renovación de sesión y reintentos seguros. Despliegue de la web y publicación del APK en Expo. | 100 % |
| Hideki Aldo Kunigami Chia | 202510390 | **Mixto.** Documentación del backend en el README. Revisión del manejo de errores y la experiencia de usuario en web y móvil: mensajes por código HTTP (400, 401, 403, 404, 409), listas vacías y estado sin conexión. Pruebas manuales de aceptación de los flujos principales. Planteamiento del problema y la solución en la presentación. | 100 % |
| Sergio Peña Andia | 202320068 | **Mixto.** Documentación del backend en el README. Pruebas de los endpoints de la API con la colección de Postman. Verificación de la web en producción: búsqueda, filtros, paginación, guardados y temas seguidos. Comprobación del despliegue (HTTPS y latencia de los endpoints principales). Preparación de la demo web en vivo. | 100 % |
 
