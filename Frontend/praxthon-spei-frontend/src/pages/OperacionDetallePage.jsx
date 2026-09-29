import { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate, Link as RouterLink } from 'react-router-dom';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Divider from '@mui/material/Divider';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import CircularProgress from '@mui/material/CircularProgress';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import RefreshIcon from '@mui/icons-material/Refresh';
import EstadoChip from '../components/EstadoChip';
import ErrorAlert from '../components/ErrorAlert';
import { obtenerOperacion, solicitarTransicion } from '../api/operaciones';

const ESTADOS_POSIBLES = ['RECIBIDO', 'EN_PROCESO', 'LIQUIDADO', 'DEVUELTO', 'RECHAZADO', 'EN_INVESTIGACION'];

function formatoFecha(iso) {
  if (!iso) return '—';
  try {
    return new Intl.DateTimeFormat('es-MX', { dateStyle: 'medium', timeStyle: 'medium' }).format(new Date(iso));
  } catch {
    return iso;
  }
}

function Campo({ etiqueta, valor }) {
  return (
    <Stack>
      <Typography variant="caption" color="text.secondary">{etiqueta}</Typography>
      <Typography variant="body2" fontWeight={600}>{valor ?? '—'}</Typography>
    </Stack>
  );
}

export default function OperacionDetallePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [operacion, setOperacion] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  const [estadoDestino, setEstadoDestino] = useState('');
  const [motivo, setMotivo] = useState('');
  const [enviandoTransicion, setEnviandoTransicion] = useState(false);
  const [errorTransicion, setErrorTransicion] = useState(null);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const datos = await obtenerOperacion(id);
      setOperacion(datos);
    } catch (err) {
      setError(err);
    } finally {
      setCargando(false);
    }
  }, [id]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const forzarTransicion = async (evento) => {
    evento.preventDefault();
    if (!estadoDestino) return;
    setEnviandoTransicion(true);
    setErrorTransicion(null);
    try {
      const actualizada = await solicitarTransicion(id, estadoDestino, motivo);
      setOperacion(actualizada);
      setMotivo('');
    } catch (err) {
      setErrorTransicion(err);
    } finally {
      setEnviandoTransicion(false);
    }
  };

  if (cargando && !operacion) {
    return (
      <Stack alignItems="center" sx={{ py: 8 }}>
        <CircularProgress />
      </Stack>
    );
  }

  if (error && !operacion) {
    return (
      <Stack gap={2}>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/')} sx={{ alignSelf: 'flex-start' }}>Volver</Button>
        <ErrorAlert error={error} />
      </Stack>
    );
  }

  return (
    <Stack gap={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" flexWrap="wrap" gap={1}>
        <Button startIcon={<ArrowBackIcon />} component={RouterLink} to="/">Volver al listado</Button>
        <Tooltip title="Actualizar">
          <IconButton color="primary" onClick={cargar}><RefreshIcon /></IconButton>
        </Tooltip>
      </Stack>

      <Paper variant="outlined" sx={{ p: 3 }}>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-start" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
          <Stack>
            <Typography variant="h5">{operacion.referenciaSeguimiento}</Typography>
            <Typography variant="caption" color="text.secondary">{operacion.id}</Typography>
          </Stack>
          <EstadoChip estado={operacion.estado} size="medium" />
        </Stack>

        <Grid container spacing={2}>
          <Grid item xs={6} sm={3}><Campo etiqueta="Tipo" valor={operacion.tipoOperacion} /></Grid>
          <Grid item xs={6} sm={3}>
            <Campo
              etiqueta="Importe"
              valor={new Intl.NumberFormat('es-MX', { style: 'currency', currency: operacion.importe?.divisa || 'MXN' }).format(operacion.importe?.valor || 0)}
            />
          </Grid>
          <Grid item xs={6} sm={3}><Campo etiqueta="Folio" valor={operacion.folioNumerico} /></Grid>
          <Grid item xs={6} sm={3}><Campo etiqueta="Registrada" valor={formatoFecha(operacion.fechaRegistro)} /></Grid>
          <Grid item xs={12}><Campo etiqueta="Concepto" valor={operacion.concepto} /></Grid>
        </Grid>

        <Divider sx={{ my: 2 }} />

        <Grid container spacing={3}>
          <Grid item xs={12} sm={6}>
            <Typography variant="subtitle2" color="primary" gutterBottom>Emisor</Typography>
            <Stack gap={1}>
              <Campo etiqueta="Institución" valor={operacion.emisor?.institucion} />
              {operacion.emisor?.cuenta && <Campo etiqueta="Cuenta CLABE" valor={operacion.emisor.cuenta} />}
              <Campo etiqueta="Nombre" valor={operacion.emisor?.nombre} />
              {operacion.emisor?.sucursal && <Campo etiqueta="Sucursal" valor={operacion.emisor.sucursal} />}
              {operacion.emisor?.documentoIdentidad && (
                <Campo
                  etiqueta="Documento de identidad"
                  valor={`${operacion.emisor.documentoIdentidad.tipo} · ${operacion.emisor.documentoIdentidad.numero}`}
                />
              )}
            </Stack>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Typography variant="subtitle2" color="primary" gutterBottom>Receptor</Typography>
            <Stack gap={1}>
              <Campo etiqueta="Institución" valor={operacion.receptor?.institucion} />
              <Campo etiqueta="Cuenta CLABE" valor={operacion.receptor?.cuenta} />
              <Campo etiqueta="Nombre" valor={operacion.receptor?.nombre} />
            </Stack>
          </Grid>
        </Grid>
      </Paper>

      <Paper variant="outlined" sx={{ p: 3 }}>
        <Typography variant="subtitle2" color="primary" gutterBottom>Historial de transiciones</Typography>
        <Stack gap={1.5} sx={{ mt: 1 }}>
          {operacion.transiciones.map((t, idx) => (
            <Stack key={idx} direction="row" alignItems="center" gap={2} sx={{ borderLeft: '3px solid #32539F', pl: 2, py: 0.5 }}>
              <EstadoChip estado={t.estado} />
              <Stack>
                <Typography variant="body2">{formatoFecha(t.momento)}</Typography>
                {t.motivo && <Typography variant="caption" color="text.secondary">Motivo: {t.motivo}</Typography>}
              </Stack>
            </Stack>
          ))}
        </Stack>
      </Paper>

      <Paper variant="outlined" sx={{ p: 3 }}>
        <Typography variant="subtitle2" color="primary" gutterBottom>Forzar transición manual (caso A21)</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Solicita un cambio de estado directo. Si no está permitido por la máquina de estados (por ejemplo,
          salir de un estado terminal), el backend responde 409 con el código PRX-014.
        </Typography>
        <ErrorAlert error={errorTransicion} onClose={() => setErrorTransicion(null)} />
        <form onSubmit={forzarTransicion}>
          <Stack direction="row" gap={2} flexWrap="wrap" alignItems="flex-start">
            <TextField
              select
              label="Estado destino"
              value={estadoDestino}
              onChange={(e) => setEstadoDestino(e.target.value)}
              sx={{ minWidth: 220 }}
              size="small"
            >
              {ESTADOS_POSIBLES.map((estado) => (
                <MenuItem key={estado} value={estado}>{estado}</MenuItem>
              ))}
            </TextField>
            <TextField
              label="Motivo (opcional)"
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
              size="small"
              sx={{ minWidth: 260, flexGrow: 1 }}
            />
            <Button type="submit" variant="contained" color="secondary" disabled={!estadoDestino || enviandoTransicion}>
              {enviandoTransicion ? 'Enviando…' : 'Solicitar transición'}
            </Button>
          </Stack>
        </form>
      </Paper>
    </Stack>
  );
}
