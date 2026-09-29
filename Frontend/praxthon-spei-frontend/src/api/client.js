import axios from 'axios';

/**
 * Cliente HTTP centralizado hacia el backend Sandbox SPEI.
 * La URL base se toma de VITE_API_BASE_URL (ver .env.example); si no se
 * define, cae a http://localhost:8080.
 */
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
});

/**
 * Normaliza cualquier error de axios a una forma predecible para la UI:
 * { status, referenciaSeguimiento, errores: [{codigo, campo, mensaje}], mensaje }
 *
 * Cubre las 3 formas de error que devuelve el backend:
 *  - 422 / 409 con { referenciaSeguimiento, errores: [...] }  (validación, idempotencia, transición)
 *  - 404 / 500 con { codigo, mensaje, momento }
 *  - Sin respuesta del servidor (red caída, CORS, backend apagado)
 */
export function normalizarError(error) {
  if (!error.response) {
    return {
      status: 0,
      referenciaSeguimiento: null,
      errores: [],
      mensaje: 'No se pudo conectar con el backend. Verifica que esté corriendo y que VITE_API_BASE_URL sea correcta.',
    };
  }
  const { status, data } = error.response;
  if (data && Array.isArray(data.errores)) {
    return {
      status,
      referenciaSeguimiento: data.referenciaSeguimiento ?? null,
      errores: data.errores,
      mensaje: null,
    };
  }
  return {
    status,
    referenciaSeguimiento: null,
    errores: [],
    mensaje: (data && (data.mensaje || data.message)) || `Error ${status} al comunicarse con el backend`,
  };
}

export default apiClient;
