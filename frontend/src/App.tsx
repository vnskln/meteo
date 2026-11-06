import { NavLink, Outlet, Route, Routes } from 'react-router'
import AdminPage from './pages/AdminPage.tsx'
import LoginPage from './pages/LoginPage.tsx'
import ReaderPage from './pages/ReaderPage.tsx'

function Layout() {
  return (
    <>
      <header>
        <strong>Meteo</strong>
        <nav aria-label="Main">
          <NavLink to="/">Measurements</NavLink>
          <NavLink to="/admin">Admin</NavLink>
        </nav>
      </header>
      <main>
        <Outlet />
      </main>
      <footer>
        Data: <a href="https://open-meteo.com/">Open-Meteo</a>, CC BY 4.0
      </footer>
    </>
  )
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<ReaderPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="admin" element={<AdminPage />} />
      </Route>
    </Routes>
  )
}
