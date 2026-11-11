# Cobertura y evidencias de la rúbrica

Revisión al 10 de noviembre de 2026. AWS responde por HTTPS y las cuatro categorías tienen mercados reales. La traducción al español se comprobó por la API pública después de configurar un modelo Groq disponible. Rance confirmó la revisión funcional del sitio web. El APK está disponible en Expo y la prueba física en Android queda a cargo de los compañeros. Los resultados medidos y las limitaciones se registran en [Evidencias de entrega](EVIDENCIAS.md).

| Criterio | Implementación | Evidencia / pendiente |
| --- | --- | --- |
| Mobile: integración 1.5 | Cliente REST compartido, métodos según endpoints, JWT, SecureStore, interceptores | Typecheck y export Android; falta prueba en teléfono contra producción |
| Mobile: arquitectura 1 | screens/components/services/hooks/contexts, React Navigation, componentes funcionales | Código organizado; contratos TypeScript |
| Mobile: sensores/API 1.5 | Cámara QR + acelerómetro para lectura por inclinación; Gamma API complementaria | Permisos, botones/enlace alternativos y limpieza; validar ambos sensores y API con un mercado real en dispositivo |
| Mobile: errores 1 | QueryState, carga, mensajes amigables, retry GET, validaciones, bloqueo de submit | Revisar permisos denegados y modo avión en teléfono |
| Web: integración 2 | Axios centralizado, JWT en memoria, refresh HttpOnly, AbortController | Registro, login y renovación comprobados por HTTPS; revisión web confirmada por Rance |
| Web: arquitectura 1 | Más de 10 componentes reutilizables, custom hooks, contextos, props tipadas | Compilación y comprobación TypeScript |
| Web: routing 1 | React Router, :id/:date, filtros/page/size/sort en URL, Protected, lazy/Suspense, 404 | Flujo de login y paginación revisado en navegador |
| Web: errores 1 | ErrorBoundary, mensajes por HTTP, loading, formularios, toasts, retry seguro | Pruebas del cliente y gráfico; ampliar QA manual |
| UI/UX: diseño .5 | Diario responsive, estilos por variables, tipografía, iconos, tema oscuro | Revisión visual escritorio/móvil |
| UI/UX: experiencia .5 | Labels, foco visible, skip link, búsqueda debounce, filtros, confirmación destructiva | Probar teclado y filtros finales |
| UI/UX: vistas .5 | Portada, registro, login, mercados, detalle, archivo, edición, guardados, perfil, temas, admin, acerca | Backend real, sin mocks funcionales en frontend |
| UI/UX: paginación .5 | Mercados y ediciones, controles/números/tamaños 10/25/50/100, total y query params | Prueba HTTP de páginas y prueba visual en mercados |
| Deployment 3 | EC2 con Caddy y Compose, RDS privado, APK distribuido por Expo | Cinco endpoints respondieron 200 con medias inferiores a 500 ms en la medición registrada; pendiente validación del APK físico |
| Exposición 5 individual | Guion y preguntas técnicas de estudio | Ensayar y demostrar dominio individual; no se garantiza una nota por entregar código |

## Comprobaciones registradas
- Backend: última ejecución completa de 32 pruebas aprobadas, incluidas 6 HTTP de integración. Prueba adicional de respuesta Groq aprobada en ejecución dirigida.
- Cliente compartido: 5 pruebas aprobadas.
- Web: 5 pruebas aprobadas: gráfico y servidor de producción (rutas, archivos y proxy).
- TypeScript: web, móvil y API compartida.
- Web: build de producción aprobado.
- Android: bundle Hermes final exportado con React Navigation; dependencias compatibles con SDK 54 comprobadas sin conexión.
- Android: APK generado por EAS y enlazado desde el README; pendiente prueba física por el equipo.
- Correo: funcionalidad de bienvenida y alertas implementada; desactivada en producción hasta configurar y validar SMTP.
- Navegador: login, guardado persistente, segunda página de mercados, detalle con historial, QR y portada compilada.

## Prueba manual de aceptación antes de entregar
1. Registrar un usuario real de prueba; iniciar sesión y recargar la página.
2. Verificar que USER no entra a administración y que ADMIN sí puede crear y eliminar una categoría de prueba.
3. Buscar, filtrar y ordenar mercados. Pasar páginas y compartir la URL.
4. Repetir paginación y cambio de tamaño en archivo.
5. Abrir titular y mercado; contrastar el gráfico con sus snapshots.
6. Guardar/quitar titular y seguir/dejar de seguir un tema; recargar para confirmar persistencia.
7. Probar 400/401/403/404/409, backend caído, red lenta y lista vacía.
8. En móvil, reiniciar la app; revisar sesión guardada en SecureStore.
9. Escanear un QR válido; rechazar uno ajeno; negar cámara y usar enlace manual.
10. Activar inclinación, pasar titulares y abrir otra pantalla; comprobar que se detiene el sensor. Probar sin sensor.
11. Abrir un mercado real y comprobar descripción/volumen de Gamma. Desactivar Internet y comprobar su error independiente.
12. Verificar rutas públicas, HTTPS, variables de entorno, ausencia de secretos y métricas de latencia en producción.
