import { useEffect, useState } from 'react';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Table from '@mui/material/Table';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TableCell from '@mui/material/TableCell';
import TableBody from '@mui/material/TableBody';
import Chip from '@mui/material/Chip';
import CircularProgress from '@mui/material/CircularProgress';
import ErrorAlert from '../components/ErrorAlert';
import { listarInstituciones, listarCatalogoErrores } from '../api/catalogos';

export default function CatalogosPage() {
  const [tab, setTab] = useState(0);
  const [instituciones, setInstituciones] = useState([]);
  const [errores, setErrores] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([listarInstituciones(), listarCatalogoErrores()])
      .then(([insts, errs]) => {
        setInstituciones(insts);
        setErrores(errs);
      })
      .catch(setError)
      .finally(() => setCargando(false));
  }, []);

  return (
    <Stack gap={2}>
      <Typography variant="h4">Catálogos</Typography>
      <ErrorAlert error={error} onClose={() => setError(null)} />
      <Paper variant="outlined">
        <Tabs value={tab} onChange={(_, v) => setTab(v)} textColor="primary" indicatorColor="secondary">
          <Tab label="Instituciones" />
          <Tab label="Códigos de error (PRX-xxx)" />
        </Tabs>
        {cargando ? (
          <Stack alignItems="center" sx={{ py: 6 }}><CircularProgress /></Stack>
        ) : tab === 0 ? (
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Código</TableCell>
                <TableCell>Nombre</TableCell>
                <TableCell align="center">¿Puede emitir?</TableCell>
                <TableCell align="center">¿En mantenimiento?</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {instituciones.map((i) => (
                <TableRow key={i.clave} hover>
                  <TableCell><strong>{i.clave}</strong></TableCell>
                  <TableCell>{i.nombre}</TableCell>
                  <TableCell align="center">
                    <Chip size="small" label={i.puedeEmitir ? 'Sí' : 'No'} color={i.puedeEmitir ? 'success' : 'default'} />
                  </TableCell>
                  <TableCell align="center">
                    <Chip size="small" label={i.enMantenimiento ? 'Sí' : 'No'} color={i.enMantenimiento ? 'warning' : 'default'} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : (
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell width={120}>Código</TableCell>
                <TableCell>Mensaje</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {errores.map((e) => (
                <TableRow key={e.codigo} hover>
                  <TableCell><Chip size="small" color="secondary" label={e.codigo} /></TableCell>
                  <TableCell>{e.mensaje}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Paper>
    </Stack>
  );
}
