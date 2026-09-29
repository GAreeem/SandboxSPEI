import apiClient, { normalizarError } from './client';

/** GET /api/v1/catalogos/instituciones */
export async function listarInstituciones() {
  try {
    const respuesta = await apiClient.get('/api/v1/catalogos/instituciones');
    return respuesta.data;
  } catch (error) {
    throw normalizarError(error);
  }
}

/** GET /api/v1/catalogos/errores */
export async function listarCatalogoErrores() {
  try {
    const respuesta = await apiClient.get('/api/v1/catalogos/errores');
    return respuesta.data;
  } catch (error) {
    throw normalizarError(error);
  }
}
