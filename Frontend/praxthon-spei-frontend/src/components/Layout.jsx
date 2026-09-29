import { NavLink, Outlet } from 'react-router-dom';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Stack from '@mui/material/Stack';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';

const ENLACES = [
  { to: '/', etiqueta: 'Operaciones', fin: true },
  { to: '/nueva', etiqueta: 'Nueva operación' },
  { to: '/catalogos', etiqueta: 'Catálogos' },
  { to: '/salud', etiqueta: 'Salud' },
];

const estiloEnlace = ({ isActive }) => ({
  color: '#FFFFFF',
  textDecoration: 'none',
  fontWeight: 600,
  fontSize: '0.95rem',
  padding: '6px 12px',
  borderRadius: 8,
  backgroundColor: isActive ? 'rgba(255,255,255,0.18)' : 'transparent',
  borderBottom: isActive ? '2px solid #F28429' : '2px solid transparent',
});

export default function Layout() {
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="sticky" elevation={0}>
        <Toolbar sx={{ gap: 3, flexWrap: 'wrap', py: 1 }}>
          <Stack direction="row" alignItems="center" gap={1.2} sx={{ mr: 2 }}>
            <AccountBalanceIcon sx={{ color: '#F28429' }} />
            <Typography variant="h6" sx={{ color: '#FFFFFF', fontWeight: 800, lineHeight: 1 }}>
              Sandbox SPEI
              <Typography component="span" sx={{ display: 'block', fontSize: '0.68rem', color: 'rgba(255,255,255,0.75)', fontWeight: 500 }}>
                PRAXTHON AMATEUR 2026
              </Typography>
            </Typography>
          </Stack>
          <Stack direction="row" gap={0.5} sx={{ flexGrow: 1, flexWrap: 'wrap' }}>
            {ENLACES.map((enlace) => (
              <NavLink key={enlace.to} to={enlace.to} end={enlace.fin} style={estiloEnlace}>
                {enlace.etiqueta}
              </NavLink>
            ))}
          </Stack>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Outlet />
      </Container>
    </Box>
  );
}
