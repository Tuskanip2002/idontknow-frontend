# Despliegue del equipo

## Estado
Hay una plantilla preparada, pero no se han creado servicios, aceptado planes de pago, publicado aplicaciones ni subido commits. La URL histórica de Render del README original no respondió en la comprobación de 20 segundos; esto no confirma que el servicio haya sido eliminado.

Para el puntaje completo hacen falta URLs verificables de la web, API y distribución móvil, PostgreSQL administrado, HTTPS y autenticación de extremo a extremo. Un localhost o un bundle exportado no cumple esa parte.

## Opción preparada: Render + EAS
`render.yaml` define dos servicios Docker y PostgreSQL administrado. Declara planes de pago para evitar suspensión del servidor durante la demo: el equipo debe revisar precios vigentes y aprobar el presupuesto antes de crear recursos. Los nombres de servicios deben ajustarse si ya están ocupados. No activar el perfil demo en producción.

1. Decidir el repositorio de entrega del monorepo. Revisar y confirmar commits y push por separado antes de subir.
2. En la cuenta Render del equipo, importar el Blueprint. Revisar recursos, región y precios.
3. Establecer los secretos en la interfaz de Render: Groq, SMTP y administrador inicial. ADMIN_PASSWORD requiere 12 caracteres, mayúscula, minúscula y número.
4. Configurar BACKEND_ORIGIN de la web con el **origen HTTPS** de la API, sin `/api/v1` ni slash final. El servidor añade la ruta solicitada.
5. Configurar CORS_ALLOWED_ORIGINS del backend con el origen HTTPS exacto de la web, sin ruta. Varios orígenes se separan con comas.
6. El Blueprint suministra DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD desde el servicio administrado. La propiedad de Spring construye una URL JDBC válida; también se puede proporcionar DB_URL explícitamente.
7. JWT_SECRET se genera en Render. No copiar el secreto de desarrollo. El perfil prod activa la cookie Secure y desactiva logs SQL.
8. Desplegar manualmente después de cada revisión autorizada. autoDeployTrigger está apagado en la plantilla.
9. Comprobar `https://API/api/v1/estado`, categorías, portada y dos listados. Completar credenciales de Groq/SMTP antes de validar correos y sincronización.
10. Crear usuarios por la interfaz, preparar suficientes mercados y ediciones reales y comprobar las métricas. No presentar datos demo como noticias reales.

El backend sincroniza diariamente a las 6 a.m. de Lima. Una instalación nueva no tiene historial; se necesitan observaciones de varios días para mostrar cambios reales. Para una demo académica local se puede usar el perfil demo, claramente identificado. En producción no se deben fabricar observaciones.

## Publicación móvil
1. El equipo inicia sesión en su cuenta Expo mediante EAS CLI.
2. En `mobile/`, configurar EXPO_PUBLIC_API_URL con `https://API/api/v1` y EXPO_PUBLIC_WEB_URL con el origen HTTPS exacto de la web.
3. Ejecutar `npx eas-cli@latest init` y guardar el projectId que Expo asigne. Esto requiere la cuenta del equipo; no inventar un ID.
4. Crear las variables para el entorno de build en EAS. Las variables EXPO_PUBLIC se incluyen en el binario: nunca colocar secretos ahí.
5. Ejecutar `npx eas-cli@latest build --platform android --profile preview`. El perfil incluido genera un APK para distribución interna.
6. Instalar el APK en un teléfono físico, verificar cámara/acelerómetro/API/secure session y compartir la URL del build con el docente.
7. La exportación local `npx expo export --platform android` sirve para validar el bundle, pero no crea el APK ni la URL de distribución.

La app se configuró con Expo SDK 54. Usar un development build compatible; comprobar compatibilidad si se cambia el SDK o se elige otra versión de Expo Go.

## Verificación
```powershell
node scripts/check-endpoints.mjs https://TU-API/api/v1
```

El script registra tres lecturas por endpoint y su promedio. La rúbrica exige una latencia media menor de 500 ms y disponibilidad verificable. Guardar evidencias de la infraestructura real; no atribuir a producción medidas de localhost. Los costos y disponibilidad dependen de las cuentas/planes elegidos.

## Fuentes técnicas
- [Blueprints de Render](https://render.com/docs/blueprint-spec)
- [Distribución con EAS Build](https://docs.expo.dev/build/introduction/)
- [Variables de entorno de Expo](https://docs.expo.dev/guides/environment-variables/)
