import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Paper from '@mui/material/Paper';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import Typography from '@mui/material/Typography';
import Stack from '@mui/material/Stack';
import ToggleButton from '@mui/material/ToggleButton';
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Switch from '@mui/material/Switch';
import FormControlLabel from '@mui/material/FormControlLabel';
import IconButton from '@mui/material/IconButton';
import RefreshIcon from '@mui/icons-material/Refresh';
import SendIcon from '@mui/icons-material/Send';
import Alert from '@mui/material/Alert';
import AlertTitle from '@mui/material/AlertTitle';
import ErrorAlert from '../components/ErrorAlert';
import EstadoChip from '../components/EstadoChip';
import { crearOperacion } from '../api/operaciones';
import { listarInstituciones } from '../api/catalogos';

const ESCENARIOS = [
  { valor: '', etiqueta: 'Automático (según cuenta receptora)' },
  { valor: 'S01', etiqueta: 'S01 · Liquidado' },
  { valor: 'S02', etiqueta: 'S02 · Devuelto — Fondos insuficientes (PRX-020)' },
  { valor: 'S03', etiqueta: 'S03 · Devuelto — Cuenta inexistente (PRX-021)' },
  { valor: 'S04', etiqueta: 'S04 · Devuelto — Institución no disponible (PRX-022)' },
  { valor: 'S05', etiqueta: 'S05 · Permanece en proceso (PRX-023)' },
  { valor: 'S06', etiqueta: 'S06 · Investigación (PRX-024)' },
];

const VACIO = {
  tipoOperacion: 'T2T',
  emisorInstitucion: '',
  emisorCuenta: '',
  emisorNombre: '',
  emisorSucursal: '',
  emisorDocTipo: '',
  emisorDocNumero: '',
  receptorInstitucion: '',
  receptorCuenta: '',
  receptorNombre: '',
  importeValor: '',
  concepto: '',
  folioNumerico: '',
  referenciaSeguimiento: '',
};

function generarReferencia() {
  return 'REF' + Date.now().toString(36).toUpperCase();
}

export default function CrearOperacionPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ ...VACIO, referenciaSeguimiento: generarReferencia() });
  const [instituciones, setInstituciones] = useState([]);
  const [escenarioForzado, setEscenarioForzado] = useState('');
  const [usarIdempotencia, setUsarIdempotencia] = useState(false);
  const [claveIdempotencia, setClaveIdempotencia] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);
  const [resultado, setResultado] = useState(null);

  useEffect(() => {
    listarInstituciones().then(setInstituciones).catch(() => setInstituciones([]));
  }, []);

  useEffect(() => {
    if (usarIdempotencia && !claveIdempotencia) {
      setClaveIdempotencia(crypto.randomUUID());
    }
  }, [usarIdempotencia]); // eslint-disable-line react-hooks/exhaustive-deps

  const esT2T = form.tipoOperacion === 'T2T';

  const cambiar = (campo) => (evento) => setForm((f) => ({ ...f, [campo]: evento.target.value }));

  const payload = useMemo(() => {
    const emisor = {
      institucion: form.emisorInstitucion || null,
      nombre: form.emisorNombre || null,
      cuenta: esT2T ? form.emisorCuenta || null : null,
      sucursal: !esT2T ? form.emisorSucursal || null : null,
      documentoIdentidad:
        !esT2T && (form.emisorDocTipo || form.emisorDocNumero)
          ? { tipo: form.emisorDocTipo || null, numero: form.emisorDocNumero || null }
          : null,
    };
    return {
      tipoOperacion: form.tipoOperacion,
      emisor,
      receptor: {
        institucion: form.receptorInstitucion || null,
        cuenta: form.receptorCuenta || null,
        nombre: form.receptorNombre || null,
      },
      importe: { valor: form.importeValor === '' ? null : Number(form.importeValor), divisa: 'MXN' },
      concepto: form.concepto || null,
      folioNumerico: form.folioNumerico === '' ? null : Number(form.folioNumerico),
      referenciaSeguimiento: form.referenciaSeguimiento || null,
    };
  }, [form, esT2T]);

  const enviar = async (evento) => {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    setResultado(null);
    try {
      const opciones = {};
      if (escenarioForzado) opciones.escenarioForzado = escenarioForzado;
      if (usarIdempotencia && claveIdempotencia) opciones.claveIdempotencia = claveIdempotencia;
      const { status, data } = await crearOperacion(payload, opciones);
      setResultado({ status, data });
    } catch (err) {
      setError(err);
    } finally {
      setEnviando(false);
    }
  };

  const limpiarFormulario = () => {
    setForm({ ...VACIO, referenciaSeguimiento: generarReferencia() });
    setResultado(null);
    setError(null);
  };

  return (
    <Stack gap={2}>
      <Typography variant="h4">Nueva operación</Typography>
      {resultado && (
        <Alert severity="success" onClose={() => setResultado(null)}>
          <AlertTitle>
            {resultado.status === 201 ? 'Operación creada (201 Created)' : 'Reintento idempotente (200 OK)'}
          </AlertTitle>
          <Stack direction="row" alignItems="center" gap={1} sx={{ mb: 1 }}>
            <Typography variant="body2">
              <strong>{resultado.data.id}</strong> — {resultado.data.referenciaSeguimiento}
            </Typography>
            <EstadoChip estado={resultado.data.estado} />
          </Stack>
          <Button size="small" variant="contained" color="secondary" onClick={() => navigate(`/operaciones/${resultado.data.id}`)}>
            Ver detalle
          </Button>
        </Alert>
      )}

      <ErrorAlert error={error} onClose={() => setError(null)} />

      <Paper variant="outlined" sx={{ p: 3 }}>
        <form onSubmit={enviar}>
          <Stack gap={3}>
            <div>
              <Typography variant="subtitle2" color="primary" gutterBottom>Tipo de operación</Typography>
              <ToggleButtonGroup
                exclusive
                color="primary"
                value={form.tipoOperacion}
                onChange={(_, valor) => valor && setForm((f) => ({ ...f, tipoOperacion: valor }))}
              >
                <ToggleButton value="T2T">T2T · Terceros a terceros</ToggleButton>
                <ToggleButton value="VNT">VNT · Ventanilla</ToggleButton>
              </ToggleButtonGroup>
            </div>

            <Divider />

            <Typography variant="subtitle2" color="primary">Emisor</Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={3}>
                <TextField select fullWidth label="Institución" value={form.emisorInstitucion} onChange={cambiar('emisorInstitucion')}>
                  {instituciones.map((i) => (
                    <MenuItem key={i.clave} value={i.clave} disabled={!i.puedeEmitir}>
                      {i.clave} · {i.nombre}{!i.puedeEmitir ? ' (no emisora)' : ''}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={5}>
                <TextField fullWidth label="Nombre" value={form.emisorNombre} onChange={cambiar('emisorNombre')} inputProps={{ maxLength: 40 }} />
              </Grid>
              {esT2T ? (
                <Grid item xs={12} sm={4}>
                  <TextField fullWidth label="Cuenta CLABE (18 dígitos)" value={form.emisorCuenta} onChange={cambiar('emisorCuenta')} inputProps={{ maxLength: 18 }} />
                </Grid>
              ) : (
                <Grid item xs={12} sm={4}>
                  <TextField fullWidth label="Sucursal" value={form.emisorSucursal} onChange={cambiar('emisorSucursal')} inputProps={{ maxLength: 40 }} />
                </Grid>
              )}
              {!esT2T && (
                <>
                  <Grid item xs={12} sm={4}>
                    <TextField fullWidth label="Documento de identidad · tipo" placeholder="INE, Pasaporte…" value={form.emisorDocTipo} onChange={cambiar('emisorDocTipo')} />
                  </Grid>
                  <Grid item xs={12} sm={4}>
                    <TextField fullWidth label="Documento de identidad · número" value={form.emisorDocNumero} onChange={cambiar('emisorDocNumero')} />
                  </Grid>
                </>
              )}
            </Grid>

            <Divider />

            <Typography variant="subtitle2" color="primary">Receptor</Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={3}>
                <TextField select fullWidth label="Institución" value={form.receptorInstitucion} onChange={cambiar('receptorInstitucion')}>
                  {instituciones.map((i) => (
                    <MenuItem key={i.clave} value={i.clave}>{i.clave} · {i.nombre}</MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={5}>
                <TextField fullWidth label="Nombre" value={form.receptorNombre} onChange={cambiar('receptorNombre')} inputProps={{ maxLength: 40 }} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  label="Cuenta CLABE (18 dígitos)"
                  value={form.receptorCuenta}
                  onChange={cambiar('receptorCuenta')}
                  inputProps={{ maxLength: 18 }}
                  helperText="Dígitos 14-17 controlan el escenario si no se fuerza uno abajo"
                />
              </Grid>
            </Grid>

            <Divider />

            <Typography variant="subtitle2" color="primary">Datos del pago</Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={3}>
                <TextField fullWidth type="number" label="Importe (MXN)" value={form.importeValor} onChange={cambiar('importeValor')} inputProps={{ step: '0.01', min: 0 }} />
              </Grid>
              <Grid item xs={12} sm={5}>
                <TextField fullWidth label="Concepto" value={form.concepto} onChange={cambiar('concepto')} inputProps={{ maxLength: 40 }} />
              </Grid>
              <Grid item xs={12} sm={2}>
                <TextField fullWidth type="number" label="Folio numérico" value={form.folioNumerico} onChange={cambiar('folioNumerico')} inputProps={{ min: 1, max: 9999999 }} />
              </Grid>
              <Grid item xs={12} sm={2}>
                <TextField fullWidth label="Referencia" value={form.referenciaSeguimiento} onChange={cambiar('referenciaSeguimiento')} inputProps={{ maxLength: 30 }} />
              </Grid>
            </Grid>

            <Divider />

            <Typography variant="subtitle2" color="primary">Opciones de prueba</Typography>
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={5}>
                <TextField
                  select
                  fullWidth
                  label="X-Escenario-Forzado"
                  value={escenarioForzado}
                  onChange={(e) => setEscenarioForzado(e.target.value)}
                  helperText="Sobrescribe la resolución determinista por cuenta"
                >
                  {ESCENARIOS.map((e) => (
                    <MenuItem key={e.valor} value={e.valor}>{e.etiqueta}</MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={7}>
                <Stack direction="row" alignItems="center" gap={1}>
                  <FormControlLabel
                    control={<Switch checked={usarIdempotencia} onChange={(e) => setUsarIdempotencia(e.target.checked)} />}
                    label="Enviar Clave-Idempotencia"
                  />
                  {usarIdempotencia && (
                    <>
                      <TextField size="small" fullWidth value={claveIdempotencia} onChange={(e) => setClaveIdempotencia(e.target.value)} />
                      <IconButton onClick={() => setClaveIdempotencia(crypto.randomUUID())} title="Generar nueva">
                        <RefreshIcon fontSize="small" />
                      </IconButton>
                    </>
                  )}
                </Stack>
              </Grid>
            </Grid>

            <Stack direction="row" gap={2} justifyContent="flex-end">
              <Button variant="text" onClick={limpiarFormulario}>Limpiar</Button>
              <Button type="submit" variant="contained" color="secondary" startIcon={<SendIcon />} disabled={enviando}>
                {enviando ? 'Enviando…' : 'Registrar operación'}
              </Button>
            </Stack>
          </Stack>
        </form>
      </Paper>
    </Stack>
  );
}
