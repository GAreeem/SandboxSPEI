import Chip from '@mui/material/Chip';

/**
 * Colores semánticos por estado de la operación. Se usan los colores de
 * estado estándar (éxito/error/advertencia) para que el semáforo sea
 * legible de un vistazo; el resto de la interfaz usa la paleta de marca
 * (azul #32539F / naranja #F28429).
 */
const ESTILOS_POR_ESTADO = {
  RECIBIDO: { color: 'info', variant: 'outlined' },
  EN_PROCESO: { color: 'warning', variant: 'filled' },
  LIQUIDADO: { color: 'success', variant: 'filled' },
  DEVUELTO: { color: 'error', variant: 'filled' },
  RECHAZADO: { color: 'default', variant: 'filled' },
  EN_INVESTIGACION: { color: 'secondary', variant: 'filled' },
};

export default function EstadoChip({ estado, size = 'small' }) {
  const estilo = ESTILOS_POR_ESTADO[estado] || { color: 'default', variant: 'outlined' };
  return <Chip label={estado} color={estilo.color} variant={estilo.variant} size={size} />;
}
