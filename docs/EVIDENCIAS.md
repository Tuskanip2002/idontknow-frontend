# Evidencias de entrega

Registro del 10 de noviembre de 2026. Los resultados corresponden a las comprobaciones descritas y pueden cambiar con nuevas sincronizaciones.

## Accesos

- [Web pública](https://18-223-255-39.sslip.io/)
- [Estado de la API](https://18-223-255-39.sslip.io/api/v1/estado)
- [APK Android](https://expo.dev/accounts/tuskanips-team/projects/idontknow-dbp/builds/dcb8354c-bf62-45cd-b983-68365e6959c6)

EC2 ejecuta web y backend mediante Docker Compose. Caddy publica HTTPS y RDS guarda los datos en PostgreSQL privado. Expo distribuye el APK. Los cambios de modelos y categorías están en los commits `15ff350` y `cb770d0`.

## Comprobaciones de producción

- Estado de la API: HTTP 200 con `status=ok` y `demo=false`.
- Registro: HTTP 201; login y renovación: HTTP 200; cierre de sesión: HTTP 204, usando el cuerpo JSON que envía la web. Cookie Secure y HttpOnly.
- Traducción: HTTP 200 en `/mercados/1/traduccion`, con texto original y versión en español. Modelo Groq validado en EC2.
- Mercados por categoría tras la sincronización puntual: política 119, economía 50, deportes 50 y tecnología 47. Las categorías pueden superponerse.
- La portada y el archivo contienen ediciones. Rance confirmó que revisó el sitio web y lo encontró funcional.

## Latencia con datos reales

Tres solicitudes consecutivas por endpoint, medidas desde el equipo de desarrollo contra HTTPS mediante `scripts/check-endpoints.mjs`. Incluyen el tiempo de red desde ese equipo. Esta muestra puntual permite contrastar el objetivo de 500 ms de media y debe repetirse antes de exponer.

| Endpoint bajo `/api/v1` | HTTP | Muestras (ms) | Media (ms) |
| --- | --- | --- | --- |
| `/estado` | 200 | 423, 342, 125 | 297 |
| `/categorias` | 200 | 130, 130, 129 | 130 |
| `/mercados?page=0&size=10` | 200 | 161, 165, 158 | 161 |
| `/ediciones?page=0&size=10` | 200 | 151, 146, 162 | 153 |
| `/ediciones/hoy` | 200 | 138, 127, 130 | 132 |

## Pruebas y validaciones locales

- Backend: 32 pruebas aprobadas en la última ejecución completa, incluidas ingestión por temas y traducción persistida. Prueba adicional de lectura de respuesta Groq aprobada por separado.
- API compartida y web: 10 pruebas aprobadas.
- TypeScript de los tres workspaces y compilación web aprobados.
- Exportación del bundle Android aprobada y APK generado por EAS.
- Búsqueda básica de credenciales en archivos y revisiones Git sin coincidencias de los patrones inspeccionados. Sin archivos `.env` o `.pem` en el historial revisado.

## Pendientes del equipo

- Android físico: un compañero debe registrar dispositivo, fecha y resultado de login, persistencia de sesión, guardados, QR y lectura por inclinación. Añadir también pruebas con cámara denegada y sin conexión.
- Correo: configurar y comprobar el envío de bienvenida y alertas; actualmente permanece desactivado en producción.
- Confirmar acceso del docente y de los cuatro integrantes a GitHub. Dos invitaciones siguen pendientes al momento de la revisión.
- Ensayo y exposición individual a cargo del equipo.
