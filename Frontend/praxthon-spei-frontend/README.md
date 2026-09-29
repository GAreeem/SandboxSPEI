# Sandbox SPEI · Frontend (React + Material UI)

Panel web para el backend PRAXTHON AMATEUR 2026 - Sandbox SPEI. Construido con
React 18, Vite y Material UI v6, con la paleta de marca:

- Azul `#32539F` — encabezado, navegación, acciones principales
- Naranja `#F28429` — acentos y llamadas a la acción
- Blanco `#FFFFFF` — fondo de superficies (tarjetas, tablas)

## Requisitos

- Node.js 18 o superior
- El backend Sandbox SPEI corriendo (por defecto en `http://localhost:8080`,
  con CORS ya habilitado para `http://localhost:5173`)

## Puesta en marcha

```bash
npm install
cp .env.example .env    # ajusta VITE_API_BASE_URL si tu backend no está en localhost:8080
npm run dev
```

Abre `http://localhost:5173`.

## Páginas

| Ruta | Qué hace |
|---|---|
| `/` | Lista paginada de operaciones (`GET /api/v1/operaciones`) |
| `/nueva` | Formulario para registrar una operación (`POST /api/v1/operaciones`), con soporte para `Clave-Idempotencia` y `X-Escenario-Forzado` |
| `/operaciones/:id` | Detalle completo, historial de transiciones, y un panel para forzar transiciones manuales (`POST /api/v1/operaciones/{id}/transiciones`) — útil para probar reglas de la máquina de estados como el caso A21 |
| `/catalogos` | Instituciones y catálogo de códigos `PRX-xxx` |
| `/salud` | Estado del servicio (`GET /salud`), con auto-refresco cada 10 s |

## Estructura

```
src/
  api/            Cliente axios + un módulo por recurso (operaciones, catálogos, salud)
  components/     Layout (barra superior), EstadoChip, ErrorAlert
  pages/          Una página por ruta
  theme/          Tema de Material UI con la paleta de marca
```

## Notas de diseño

- El backend crea las operaciones en `RECIBIDO` y las avanza de forma
  asíncrona (dos pasos de ~1 s cada uno). Por eso el detalle de una
  operación trae un botón de refrescar en vez de reflejar el estado en
  tiempo real; si quieres eso, la vía más simple es hacer polling cada
  2-3 segundos mientras el estado no sea uno final (no está implementado
  aquí para mantener el ejemplo simple).
- Los colores de los `Chip` de estado (verde=LIQUIDADO, rojo=DEVUELTO, etc.)
  usan los colores semánticos estándar de Material UI en vez de la paleta
  de marca, porque necesitan ser distinguibles entre sí de un vistazo;
  el resto de la interfaz sí usa exclusivamente azul/naranja/blanco.
- `ErrorAlert` normaliza las 3 formas de error que devuelve el backend:
  `{referenciaSeguimiento, errores:[...]}` (422/409), `{codigo, mensaje}`
  (404/500), y errores de red (backend apagado / CORS).
