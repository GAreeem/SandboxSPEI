import apiClient, { normalizarError } from './client';

/** GET /salud */
export async function obtenerSalud() {
  try {
    const respuesta = await apiClient.get('/salud');
    return respuesta.data;
  } catch (error) {
    throw normalizarError(error);
  }
}
