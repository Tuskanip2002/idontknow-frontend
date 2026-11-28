# Despliegue del equipo

## Estado
El backend, la web y la app móvil están en [idontknow-frontend](https://github.com/Tuskanip2002/idontknow-frontend), en la rama `master`. La entrega usa EC2 con Caddy y Docker Compose, PostgreSQL privado en RDS y un APK distribuido por Expo. Los enlaces están en el README.

## AWS actual

Usar `compose.aws.yaml` desde EC2. Configurar `.env` con `RDS_HOST`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` y `WEB_ORIGIN`. Descargar el certificado oficial de RDS como `rds-ca.pem`. Caddy apunta a `127.0.0.1:3000` y publica HTTPS. RDS permite TCP 5432 desde el grupo de seguridad de EC2.

Configurar `GROQ_API_KEY` para traducir titulares. Para correo, configurar `MAIL_USERNAME` y `MAIL_APP_PASSWORD`, y activar `EMAIL_ENABLED=true` después de validar las credenciales SMTP. Para crear un administrador inicial, configurar `ADMIN_EMAIL` y `ADMIN_PASSWORD`. Tras editar las variables, ejecutar `sudo docker compose -f compose.aws.yaml up -d --build` y comprobar los logs. La sincronización se ejecuta a las 6 a. m. de Lima. Los titulares de una edición existente que conservaron el texto original reintentan la traducción al procesarse otra vez.

El modelo predeterminado actual es `openai/gpt-oss-20b`, verificado con la cuenta de Groq del equipo. Puede cambiarse mediante `GROQ_MODEL`. El modelo anterior devolvió `model_not_found`. La sincronización consulta hasta 50 mercados por cada tema de Polymarket (política, economía, deportes y tecnología) y elimina duplicados por identificador. La traducción solicitada desde el detalle se almacena en PostgreSQL para reutilizarla.

Los secretos se guardan en el servidor. La clave privada SSH, `.env` y el estado de la base de datos deben excluirse de Git.

Para el puntaje completo hacen falta URLs verificables de la web, API y distribución móvil, PostgreSQL administrado, HTTPS y autenticación de extremo a extremo. Un localhost o un bundle exportado no cumple esa parte.

## Actualizar el servidor (método actual)

La carpeta del servidor (`/home/ubuntu/idontknow-app`) no es un clon de Git: el código se descarga como archivo comprimido desde `master`. Por eso `git pull` no funciona ahí. Cada integrante entra con su propia llave SSH, agregada a `~/.ssh/authorized_keys` del servidor; no se comparte la llave privada de nadie.

```bash
ssh -i ~/.ssh/id_ed25519 ubuntu@18.223.255.39
cd /home/ubuntu
cp -a idontknow-app idontknow-app-respaldo-$(date +%F-%H%M)
rm -rf /tmp/idk-nuevo /tmp/idk.tar.gz
curl -L -o /tmp/idk.tar.gz https://github.com/Tuskanip2002/idontknow-frontend/archive/refs/heads/master.tar.gz
mkdir -p /tmp/idk-nuevo
tar -xzf /tmp/idk.tar.gz -C /tmp/idk-nuevo --strip-components=1
rsync -a --exclude '.env' --exclude 'rds-ca.pem' /tmp/idk-nuevo/ /home/ubuntu/idontknow-app/
cd /home/ubuntu/idontknow-app
sudo docker compose -f compose.aws.yaml up -d --build
sudo docker compose -f compose.aws.yaml ps
rm -rf /tmp/idk-nuevo /tmp/idk.tar.gz
```

`rsync` sin `--delete` no borra archivos, y los `--exclude` protegen `.env` y el certificado de RDS. Para volver atrás, copiar el respaldo sobre `idontknow-app` con `rsync -a idontknow-app-respaldo-FECHA/ idontknow-app/` y volver a ejecutar `up -d --build`. No reiniciar la instancia sin una Elastic IP: AWS podría asignarle otra IP y el dominio `sslip.io` y el APK dejarían de apuntar al servidor.

Variables opcionales de las funciones de la final:

| Variable | Valor por defecto | Uso |
| --- | --- | --- |
| `NOTICIAS_RSS_ENABLED` | `true` | Respaldo de Google News cuando GDELT no devuelve noticias. Ya se pasa al contenedor en ambos Compose. |
| `NOTICIAS_RSS_URL` | `https://news.google.com/rss/search` | Endpoint de búsqueda RSS. Ya se pasa al contenedor. |
| `EXPLICACIONES_ENABLED` | `true` | Apagar la generación de explicaciones. Para usarla hay que agregarla al `environment` del servicio `backend` en Compose. |
| `SUSCRIPCION_DEMO` | `true` | Apagar la suscripción simulada. Mismo requisito que la anterior. |
| `SUSCRIPCION_DEMO_DIAS` | `30` | Días de Premium por activación. Mismo requisito que la anterior. |

## Alternativa Render + EAS
`render.yaml` define dos servicios Docker y PostgreSQL administrado con `plan: free`. Los servicios comparten 750 horas mensuales por workspace, se suspenden tras 15 minutos sin tráfico y pueden tardar cerca de un minuto en despertar. PostgreSQL caduca a los 30 días. Antes de la exposición, abrir la web y comprobar el endpoint de estado de la API. Medir también el arranque en frío por separado. Los nombres de servicios deben ajustarse si ya están ocupados. No activar el perfil demo en producción.

1. Conectar la cuenta de Render con GitHub y conceder acceso al repositorio privado `Tuskanip2002/idontknow-frontend`. Seleccionar la rama `master`.
2. En la cuenta Render del equipo, importar el Blueprint. Revisar recursos, región y precios.
3. Establecer los secretos de Groq y del administrador inicial en la interfaz de Render. ADMIN_PASSWORD requiere 12 caracteres, mayúscula, minúscula y número. El plan gratuito bloquea los puertos SMTP 25, 465 y 587. Dejar las variables de correo sin configurar hasta integrar un proveedor por HTTPS.
4. Configurar BACKEND_ORIGIN de la web con el **origen HTTPS** de la API, sin `/api/v1` ni slash final. El servidor añade la ruta solicitada.
5. Configurar CORS_ALLOWED_ORIGINS del backend con el origen HTTPS exacto de la web, sin ruta. Varios orígenes se separan con comas.
6. El Blueprint suministra DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD desde el servicio administrado. La propiedad de Spring construye una URL JDBC válida; también se puede proporcionar DB_URL explícitamente.
7. JWT_SECRET se genera en Render. No copiar el secreto de desarrollo. El perfil prod activa la cookie Secure y desactiva logs SQL.
8. Desplegar manualmente después de cada revisión autorizada. autoDeployTrigger está apagado en la plantilla.
9. Comprobar `https://API/api/v1/estado`, categorías, portada y dos listados. Completar las credenciales de Groq antes de validar la sincronización. Validar el correo cuando tenga un transporte compatible con el hosting.
10. Crear usuarios por la interfaz, preparar suficientes mercados y ediciones reales y comprobar las métricas. No presentar datos demo como noticias reales.

El backend sincroniza diariamente a las 6 a.m. de Lima. Una instalación nueva no tiene historial; se necesitan observaciones de varios días para mostrar cambios reales. Para una demo académica local se puede usar el perfil demo, claramente identificado. En producción no se deben fabricar observaciones.

## Publicación móvil
1. El equipo inicia sesión en su cuenta Expo mediante EAS CLI.
2. En `mobile/`, configurar EXPO_PUBLIC_API_URL con `https://API/api/v1` y EXPO_PUBLIC_WEB_URL con el origen HTTPS exacto de la web.
3. El proyecto actual ya está vinculado a `tuskanips-team/idontknow-dbp`; el projectId está en `mobile/app.json`. Usar una cuenta Expo con acceso al proyecto.
4. Las URLs públicas del perfil `preview` están en `mobile/eas.json`. Las variables EXPO_PUBLIC se incluyen en el binario: nunca colocar secretos ahí.
5. Ejecutar `npx eas-cli@latest build --platform android --profile preview`. El perfil incluido genera un APK para distribución interna.
6. Instalar el APK en un teléfono físico, verificar cámara/acelerómetro/API/secure session y compartir la URL del build con el docente.
7. La exportación local `npx expo export --platform android` sirve para validar el bundle, pero no crea el APK ni la URL de distribución.

La app se configuró con Expo SDK 54. Usar un development build compatible; comprobar compatibilidad si se cambia el SDK o se elige otra versión de Expo Go.

## Verificación

### Explicaciones con fuentes
Los comentarios se obtienen del evento asociado al mercado: primero `/markets?id=ID`
y después `/comments?parent_entity_type=Event&parent_entity_id=EVENTO_ID`.
El tipo `market` no está admitido por Gamma y la respuesta individual de un mercado
puede omitir su relación con eventos.

Si GDELT responde 429, el cliente respeta `Retry-After`, con una pausa mínima de un
minuto, y continúa con los comentarios disponibles. Esto reduce las solicitudes,
pero no garantiza que el proveedor vuelva a aceptar la IP del servidor. Una respuesta
no JSON deja un aviso en los logs; no debe interpretarse como ausencia de noticias.

Cuando GDELT no devuelve noticias, se consulta como respaldo el RSS de búsqueda de
Google News, sin una clave de API adicional. Se utilizan únicamente título, medio,
enlace y fecha: no se descarga el texto completo de los artículos. Se descartan
enlaces distintos de HTTP/HTTPS, duplicados, artículos sin medio o fecha, fechas
futuras y noticias de hace más de tres días. Los enlaces de Google News pueden
redirigir al medio original. El resumen en español sigue dependiendo de Groq y solo
debe atribuir una causa cuando las fuentes la respalden.

El respaldo viene habilitado en Compose local y AWS. Puede deshabilitarse con
`NOTICIAS_RSS_ENABLED=false` en `.env`, seguido de `docker compose ... up -d`.
`NOTICIAS_RSS_URL` permite configurar el endpoint de búsqueda; el lector espera RSS
con el formato de Google News. Es un servicio externo sin garantía de disponibilidad
ni de resultados para todos los mercados. Si ambos proveedores fallan, se conservan
las fuentes anteriores del titular. Las pruebas verifican errores HTTP y el bloqueo
de entidades XML externas.

Validación del respaldo (25 de noviembre de 2026): desde EC2, la búsqueda RSS
respondió HTTP 200 con `application/xml`. En desarrollo, el cliente Java integrado
recuperó cuatro noticias por consulta para Netanyahu, NVIDIA y el estrecho de Ormuz
con un servidor GDELT de prueba que respondía 429. Pasaron 61 pruebas del backend.
Estas comprobaciones no sustituyen la regeneración y revisión del resumen Premium
en producción después de desplegar el cambio.

Después de corregir y desplegar, regenerar la portada mediante
`POST /api/v1/explicaciones/regenerar` con una sesión ADMIN. El arranque solo completa
las explicaciones inexistentes, por lo que no reemplaza las que ya tenían cero fuentes.
Esperar a que termine el trabajo en segundo plano y comprobar los recuentos en los
logs y en `GET /api/v1/titulares/ID/explicacion`. Un 202 solo confirma que se inició
la generación; no confirma que se hayan recuperado fuentes. Verificar además el
resumen con una cuenta Premium. Los comentarios se identifican como opiniones de
participantes, no como noticias periodísticas.

Al regenerar, una consulta sin fuentes nuevas conserva el resumen, las fuentes y la
fecha de generación anteriores. Si solo uno de los proveedores devuelve resultados,
se conservan las fuentes previas del otro tipo para ese mismo titular y se vuelve a
generar el resumen con la combinación resultante. Esto no recupera fuentes que ya
se hubieran borrado ni garantiza fuentes para todos los mercados.

```powershell
node scripts/check-endpoints.mjs https://TU-API/api/v1
```

El script registra tres lecturas por endpoint y su promedio. La rúbrica exige una latencia media menor de 500 ms y disponibilidad verificable. Guardar evidencias de la infraestructura real; no atribuir a producción medidas de localhost. Los costos y disponibilidad dependen de las cuentas/planes elegidos.

## Fuentes técnicas
- [Blueprints de Render](https://render.com/docs/blueprint-spec)
- [Límites de Render Free](https://render.com/docs/free)
- [Distribución con EAS Build](https://docs.expo.dev/build/introduction/)
- [Variables de entorno de Expo](https://docs.expo.dev/guides/environment-variables/)
