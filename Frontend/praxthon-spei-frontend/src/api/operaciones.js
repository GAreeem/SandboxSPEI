import apiClient, { normalizarError } from './client';

/**
 * POST /api/v1/operaciones
 * @param {object} body - OperacionRequestDTO
 * @param {{claveIdempotencia?: string, escenarioForzado?: string}} opciones
 * @returns {{status: number, data: object}} status 201 (nueva) o 200 (reintento idempotente)
 */
export async function crearOperacion(body, opciones = {}) {
  const headers = {};
  if (opciones.claveIdempotencia) headers['Clave-Idempotencia'] = opciones.claveIdempotencia;
  if (opciones.escenarioForzado) headers['X-Escenario-Forzado'] = opciones.escenarioForzado;
  try {
    const respuesta = await apiClient.post('/api/v1/operaciones', body, { headers });
    return { status: respuesta.status, data: respuesta.data };
  } catch (error) {
    throw normalizarError(error);
  }
}

/** GET /api/v1/operaciones/{id} */
export async function obtenerOperacion(id) {
  try {
    const respuesta = await apiClient.get(`/api/v1/operaciones/${encodeURIComponent(id)}`);
    return respuesta.data;
  } catch (error) {
    throw normalizarError(error);
  }
}

/** GET /api/v1/operaciones?pagina=&tamano= */
export async function listarOperaciones(pagina = 0, tamano = 10) {
  try {
    const respuesta = await apiClient.get('/api/v1/operaciones', { params: { pagina, tamano } });
    return respuesta.data; // PaginaResponseDTO
  } catch (error) {
    throw normalizarError(error);
  }
}

/** POST /api/v1/operaciones/{id}/transiciones */
export async function solicitarTransicion(id, estado, motivo) {
  try {
    const respuesta = await apiClient.post(`/api/v1/operaciones/${encodeURIComponent(id)}/transiciones`, {
      estado,
      motivo: motivo || null,
    });
    return respuesta.data;
  } catch (error) {
    throw normalizarError(error);
  }
}
