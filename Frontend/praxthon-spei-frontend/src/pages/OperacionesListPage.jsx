import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Paper from '@mui/material/Paper';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TablePagination from '@mui/material/TablePagination';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import RefreshIcon from '@mui/icons-material/Refresh';
import AddIcon from '@mui/icons-material/Add';
import VisibilityIcon from '@mui/icons-material/Visibility';
import EstadoChip from '../components/EstadoChip';
import ErrorAlert from '../components/ErrorAlert';
import { listarOperaciones } from '../api/operaciones';

function formatoMoneda(valor, divisa) {
  if (valor === null || valor === undefined) return '—';
  return new Intl.NumberFormat('es-MX', { style: 'currency', currency: divisa || 'MXN' }).format(valor);
}

function formatoFecha(iso) {
  if (!iso) return '—';
  try {
    return new Intl.DateTimeFormat('es-MX', { dateStyle: 'short', timeStyle: 'medium' }).format(new Date(iso));
  } catch {
    return iso;
  }
}

export default function OperacionesListPage() {
  const navigate = useNavigate();
  const [pagina, setPagina] = useState(0);
  const [tamano, setTamano] = useState(10);
  const [respuesta, setRespuesta] = useState({ contenido: [], totalElementos: 0 });
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const datos = await listarOperaciones(pagina, tamano);
      setRespuesta(datos);
    } catch (err) {
      setError(err);
    } finally {
      setCargando(false);
    }
  }, [pagina, tamano]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  return (
    <Stack gap={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" flexWrap="wrap" gap={1}>
        <Typography variant="h4">Operaciones</Typography>
        <Stack direction="row" gap={1}>
          <Tooltip title="Actualizar">
            <IconButton onClick={cargar} color="primary">
              <RefreshIcon />
            </IconButton>
          </Tooltip>
          <Button variant="contained" color="secondary" startIcon={<AddIcon />} onClick={() => navigate('/nueva')}>
            Nueva operación
          </Button>
        </Stack>
      </Stack>

      <ErrorAlert error={error} onClose={() => setError(null)} />

      <Paper variant="outlined">
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Referencia</TableCell>
                <TableCell>Tipo</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Importe</TableCell>
                <TableCell>Emisor → Receptor</TableCell>
                <TableCell>Fecha de registro</TableCell>
                <TableCell align="center">Detalle</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {cargando && (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                    <CircularProgress size={28} />
                  </TableCell>
                </TableRow>
              )}
              {!cargando && respuesta.contenido.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                    <Typography color="text.secondary">Aún no hay operaciones registradas.</Typography>
                  </TableCell>
                </TableRow>
              )}
              {!cargando &&
                respuesta.contenido.map((op) => (
                  <TableRow key={op.id} hover>
                    <TableCell>
                      <Typography variant="body2" fontWeight={600}>{op.referenciaSeguimiento}</Typography>
                      <Typography variant="caption" color="text.secondary">{op.id}</Typography>
                    </TableCell>
                    <TableCell>{op.tipoOperacion}</TableCell>
                    <TableCell><EstadoChip estado={op.estado} /></TableCell>
                    <TableCell align="right">{formatoMoneda(op.importe?.valor, op.importe?.divisa)}</TableCell>
                    <TableCell>
                      <Typography variant="caption" display="block">{op.emisor?.institucion} · {op.emisor?.nombre || '—'}</Typography>
                      <Typography variant="caption" display="block" color="text.secondary">→ {op.receptor?.institucion} · {op.receptor?.nombre}</Typography>
                    </TableCell>
                    <TableCell>{formatoFecha(op.fechaRegistro)}</TableCell>
                    <TableCell align="center">
                      <Tooltip title="Ver detalle">
                        <IconButton size="small" color="primary" onClick={() => navigate(`/operaciones/${op.id}`)}>
                          <VisibilityIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          component="div"
          count={respuesta.totalElementos || 0}
          page={pagina}
          onPageChange={(_, nuevaPagina) => setPagina(nuevaPagina)}
          rowsPerPage={tamano}
          onRowsPerPageChange={(evento) => {
            setTamano(parseInt(evento.target.value, 10));
            setPagina(0);
          }}
          rowsPerPageOptions={[5, 10, 20, 50]}
          labelRowsPerPage="Filas por página"
          labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count}`}
        />
      </Paper>
    </Stack>
  );
}
