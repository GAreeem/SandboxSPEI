import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import { Link as RouterLink } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <Stack alignItems="center" gap={2} sx={{ py: 10 }}>
      <Typography variant="h2" color="primary" fontWeight={800}>404</Typography>
      <Typography color="text.secondary">Esta página no existe.</Typography>
      <Button component={RouterLink} to="/" variant="contained" color="secondary">Volver al inicio</Button>
    </Stack>
  );
}
