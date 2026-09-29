import { createTheme } from '@mui/material/styles';

/**
 * Tema de Material UI para el panel Sandbox SPEI.
 *
 * Paleta corporativa solicitada:
 *  - Azul PRAXTHON  #32539F  -> primary   (encabezados, navegación, acciones principales)
 *  - Naranja PRAXTHON #F28429 -> secondary (acentos, llamadas a la acción, alertas de escenario)
 *  - Blanco #FFFFFF -> fondo de superficies
 */
const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#32539F',
      light: '#5876B8',
      dark: '#213A75',
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#F28429',
      light: '#F5A05C',
      dark: '#C4651A',
      contrastText: '#FFFFFF',
    },
    background: {
      default: '#F4F6FB',
      paper: '#FFFFFF',
    },
    text: {
      primary: '#1B2436',
      secondary: '#5B6B8C',
    },
    divider: 'rgba(50, 83, 159, 0.14)',
    success: { main: '#2E9E6B' },
    warning: { main: '#F28429' },
    error: { main: '#D9463C' },
    info: { main: '#32539F' },
  },
  shape: {
    borderRadius: 10,
  },
  typography: {
    fontFamily: '"Inter", "Segoe UI", Roboto, Helvetica, Arial, sans-serif',
    h1: { fontWeight: 800 },
    h2: { fontWeight: 800 },
    h3: { fontWeight: 700 },
    h4: { fontWeight: 700 },
    h5: { fontWeight: 700 },
    h6: { fontWeight: 700 },
    button: { fontWeight: 600, textTransform: 'none' },
    subtitle2: { fontWeight: 600, letterSpacing: 0.2 },
  },
  components: {
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '#32539F',
          backgroundImage: 'linear-gradient(90deg, #32539F 0%, #2A4784 100%)',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 8, paddingInline: 18 },
        containedSecondary: { color: '#FFFFFF' },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-root': {
            fontWeight: 700,
            color: '#32539F',
            backgroundColor: '#EEF2FA',
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600 },
      },
    },
    MuiTab: {
      styleOverrides: {
        root: { fontWeight: 600 },
      },
    },
  },
});

export default theme;
