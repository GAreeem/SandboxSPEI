import { useEffect, useState } from 'react';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import Chip from '@mui/material/Chip';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import RefreshIcon from '@mui/icons-material/Refresh';
import FavoriteIcon from '@mui/icons-material/Favorite';
import StorageIcon from '@mui/icons-material/Storage';
import TimerIcon from '@mui/icons-material/Timer';
import ErrorAlert from '../components/ErrorAlert';
import { obtenerSalud } from '../api/salud';

function TarjetaIndicador({ icono, etiqueta, valor, color }) {
  return (
    <Paper variant="outlined" sx={{ p: 3, textAlign: 'center', height: '100%' }}>
      <Stack alignItems="center" gap={1}>
        <Stack sx={{ color: color || 'primary.main' }}>{icono}</Stack>
        <Typography variant="h4" fontWeight={800}>{valor}</Typography>
        <Typography variant="body2" color="text.secondary">{etiqueta}</Typography>
      </Stack>
    </Paper>
  );
}

export default function SaludPage() {
  const [salud, setSalud] = useState(null);
  const [error, setError] = useState(null);

  const cargar = () => {
    obtenerSalud().then(setSalud).catch(setError);
  };

  useEffect(() => {
    cargar();
    const intervalo = setInterval(cargar, 10000); // refresco automático cada 10 s
    return () => clearInterval(intervalo);
  }, []);

  return (
    <Stack gap={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4">Salud del servicio</Typography>
        <Tooltip title="Actualizar ahora">
          <IconButton color="primary" onClick={cargar}><RefreshIcon /></IconButton>
        </Tooltip>
      </Stack>

      <ErrorAlert error={error} onClose={() => setError(null)} />

      {salud && (
        <Grid container spacing={2}>
          <Grid item xs={12} sm={4}>
            <TarjetaIndicador
              icono={<FavoriteIcon fontSize="large" />}
              etiqueta="Estado"
              color={salud.estado === 'ok' ? '#2E9E6B' : '#D9463C'}
              valor={<Chip label={salud.estado.toUpperCase()} color={salud.estado === 'ok' ? 'success' : 'error'} />}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TarjetaIndicador icono={<StorageIcon fontSize="large" />} etiqueta="Operaciones registradas" valor={salud.operaciones} />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TarjetaIndicador icono={<TimerIcon fontSize="large" />} etiqueta="Retardo por paso (ms)" valor={salud.retardoMs} color="#F28429" />
          </Grid>
        </Grid>
      )}
    </Stack>
  );
}
