# Guion de exposición y demo
Duración objetivo: 20 minutos. La guía de ejemplo de la rúbrica suma 20 minutos al incluir preguntas, pero sus bloques individuales suman más; ajustar el ensayo a la duración que confirme el docente.

## Apertura (1 minuto)
“Cuando una noticia habla del futuro, ¿cómo distinguimos una posibilidad de una certeza? I Don’t Know muestra la probabilidad del mercado y cuánto cambió desde ayer.”

Mostrar una noticia y su evolución. Si se usa el entorno local, indicar que es una demostración con datos de ejemplo.

## Problema (2 minutos)
Las preguntas de mercados de predicción suelen estar en inglés y organizadas para participar en ellos. Un lector necesita contexto, selección de temas y comparación con días anteriores.

No afirmar porcentajes de mercado o encuestas que el equipo no haya medido.

## Solución (3 minutos)
Presentar la portada en español, detalle con historial, mercados filtrables, archivo, guardados y seguimiento de temas. Explicar que la app no permite apostar.

## Arquitectura (2 minutos)
Usar el diagrama de ARCHITECTURE.md. Explicar React/React Native sobre la misma API Spring Boot, PostgreSQL, Axios compartido y las integraciones Polymarket/Groq/SMTP.

Contar una decisión concreta: guardar snapshots diarios permite mostrar cambios, mientras que consultar solo la probabilidad actual no permite reconstruir el historial.

## Demo en vivo (6 minutos)
- 0:00–1:00: portada, probabilidad y cambio en puntos porcentuales.
- 1:00–2:00: detalle y gráfico; mostrar la tabla accesible.
- 2:00–3:00: búsqueda/filtro, segunda página y archivo.
- 3:00–4:00: sesión preparada, guardar titular, seguir tema.
- 4:00–5:00: QR de la web a la app; negar permiso y mostrar alternativa solo si alcanza el tiempo.
- 5:00–6:00: lectura por inclinación y contexto complementario de un mercado real.

Tener las cuentas abiertas, el teléfono conectado y suficientes datos antes de empezar. No dedicar la demo al registro ni esperar al cron del día.

## Preguntas (5 minutos) y cierre (1 minuto)
Cada integrante debe explicar una funcionalidad y seguir una llamada completa: evento de UI → hook/contexto → Axios → controller → service → repository → DB → DTO → UI.

Preguntas para preparar:
- ¿Por qué componentes reutilizables en vez de copiar markup?
- ¿Qué provoca un render y para qué sirve useEffect?
- ¿Cómo se cancela un request al salir de una pantalla?
- ¿Qué se comparte en el Context y qué queda como estado local?
- ¿Cómo se diferencian 401, 403, 409 y errores de red?
- ¿Por qué no se reintenta automáticamente un POST?
- ¿Dónde está el access token en web y dónde en móvil?
- ¿Por qué HttpOnly y Secure? ¿Qué significa SameSite?
- ¿Cómo funciona la rotación del refresh y qué invalida logout?
- ¿Cómo se evitan dos renovaciones simultáneas?
- ¿Quién aplica realmente los permisos de administrador?
- ¿Cómo viajan page, size, q y categoria al backend?
- ¿Cómo se calculan los puntos porcentuales?
- ¿Qué ocurre si no hay snapshots, cámara o acelerómetro?
- ¿Por qué se detiene el sensor al perder el foco?
- ¿Qué depende de Polymarket directamente y qué depende del backend?
- ¿Qué diferencias hay entre exportar un bundle, generar un APK y publicarlo?
- ¿Qué evidencia prueba que la infraestructura usa PostgreSQL y HTTPS?

## Preparación final
Completar las URLs y capturas de la infraestructura real después del despliegue. Verificar correo y sensores físicos. Ensayar dos veces con cronómetro y conservar una grabación breve como respaldo, sin sustituir la demo en vivo.
