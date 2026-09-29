import Alert from '@mui/material/Alert';
import AlertTitle from '@mui/material/AlertTitle';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

/**
 * Muestra de forma uniforme cualquier error normalizado por
 * `api/client.js:normalizarError`. Si trae una lista `errores` (422/409),
 * los enumera con su código PRX-xxx, campo y mensaje.
 */
export default function ErrorAlert({ error, onClose }) {
  if (!error) return null;

  const titulo =
    error.status === 0
      ? 'Sin conexión con el backend'
      : `Error ${error.status || ''}`.trim();

  return (
    <Alert severity="error" onClose={onClose} sx={{ mb: 2 }}>
      <AlertTitle>{titulo}</AlertTitle>
      {error.referenciaSeguimiento && (
        <Typography variant="body2" sx={{ mb: 0.5 }}>
          Referencia: <strong>{error.referenciaSeguimiento}</strong>
        </Typography>
      )}
      {error.errores && error.errores.length > 0 ? (
        <Box component="ul" sx={{ m: 0, pl: 2.5 }}>
          {error.errores.map((e, idx) => (
            <li key={idx}>
              <strong>{e.codigo}</strong>
              {e.campo ? ` · ${e.campo}` : ''} — {e.mensaje}
            </li>
          ))}
        </Box>
      ) : (
        <Typography variant="body2">{error.mensaje}</Typography>
      )}
    </Alert>
  );
}
