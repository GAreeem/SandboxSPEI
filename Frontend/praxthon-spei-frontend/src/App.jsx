import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import OperacionesListPage from './pages/OperacionesListPage';
import CrearOperacionPage from './pages/CrearOperacionPage';
import OperacionDetallePage from './pages/OperacionDetallePage';
import CatalogosPage from './pages/CatalogosPage';
import SaludPage from './pages/SaludPage';
import NotFoundPage from './pages/NotFoundPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<OperacionesListPage />} />
          <Route path="nueva" element={<CrearOperacionPage />} />
          <Route path="operaciones/:id" element={<OperacionDetallePage />} />
          <Route path="catalogos" element={<CatalogosPage />} />
          <Route path="salud" element={<SaludPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
