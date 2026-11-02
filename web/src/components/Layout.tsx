import { useEffect, useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { Bookmark, UserRound, ArrowUpRight, Moon, Sun } from "lucide-react";
import { useAuth, useToast } from "../contexts/AppContext";
import { Button } from "./ui";
import { api } from "../services/api";
import { useQuery } from "../hooks/useQuery";
export default function Layout() {
  const status = useQuery(api.status, "api-status");
  const { user, signOut } = useAuth();
  const toast = useToast();
  const location = useLocation();
  const [dark, setDark] = useState(
    () => window.matchMedia("(prefers-color-scheme: dark)").matches,
  );
  const [online, setOnline] = useState(navigator.onLine);
  useEffect(() => {
    document.documentElement.dataset.theme = dark ? "dark" : "light";
  }, [dark]);
  useEffect(() => {
    const update = () => setOnline(navigator.onLine);
    window.addEventListener("online", update);
    window.addEventListener("offline", update);
    return () => {
      window.removeEventListener("online", update);
      window.removeEventListener("offline", update);
    };
  }, []);
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [location.pathname]);
  const labels: Record<string, string> = {
    mercados: "Mercados",
    archivo: "Archivo",
    guardados: "Guardados",
    perfil: "Mi perfil",
    temas: "Mis temas",
    admin: "Categorías",
    login: "Iniciar sesión",
    registro: "Crear cuenta",
    titulares: "Titular",
    acerca: "Cómo funciona",
  };
  return (
    <>
      <a href="#contenido" className="skip-link">
        Saltar al contenido
      </a>
      <div className="top-strip">
        <span>Un diario donde cada noticia es una probabilidad.</span>
        <Link to="/acerca">
          Conoce el proyecto <ArrowUpRight size={12} />
        </Link>
      </div>
      <header className="masthead">
        <Link className="wordmark" to="/" aria-label="I Don't Know, portada">
          I Don’t Know<span className="brand-dot">.</span>
        </Link>
        <div className="header-actions">
          <Button
            variant="icon quiet"
            aria-label={dark ? "Activar tema claro" : "Activar tema oscuro"}
            onClick={() => setDark((v) => !v)}
          >
            {dark ? <Sun size={18} /> : <Moon size={18} />}
          </Button>
          {user ? (
            <>
              <Link to="/guardados" aria-label="Mis guardados">
                <Bookmark size={19} />
              </Link>
              <Link className="account-link" to="/perfil">
                <UserRound size={18} />
                <span>{user.nombre.split(" ")[0]}</span>
              </Link>
              <Button
                variant="quiet"
                onClick={async () => {
                  try {
                    await signOut();
                    toast("Sesión cerrada.");
                  } catch (e) {
                    toast((e as Error).message, true);
                  }
                }}
              >
                Salir
              </Button>
            </>
          ) : (
            <Link to="/login" className="button">
              Iniciar sesión <ArrowUpRight size={15} />
            </Link>
          )}
        </div>
      </header>
      <nav className="main-nav" aria-label="Navegación principal">
        <NavLink to="/" end>
          Portada
        </NavLink>
        <NavLink to="/mercados">Explorar mercados</NavLink>
        <NavLink to="/archivo">Archivo</NavLink>
        <NavLink to="/temas">Mis temas</NavLink>
        {user?.role === "ADMIN" && (
          <NavLink to="/admin">Administración</NavLink>
        )}
        <span className="nav-caption">Menos ruido. Más perspectiva.</span>
      </nav>
      {!online && (
        <div role="status" className="offline">
          Estás sin conexión. Vuelve a conectarte para cargar información
          actualizada.
        </div>
      )}
      {status.data?.demo && (
        <div className="offline" role="status">
          Entorno de demostración · Datos de ejemplo para pruebas, no
          probabilidades reales.
        </div>
      )}
      <main id="contenido" tabIndex={-1}>
        {location.pathname !== "/" && (
          <nav className="breadcrumbs" aria-label="Ubicación">
            <Link to="/">Portada</Link>
            <span>/</span>
            <span>{labels[location.pathname.split("/")[1]] || "Página"}</span>
          </nav>
        )}
        <Outlet />
      </main>
      <footer>
        <Link className="footer-brand" to="/">
          I Don’t Know.
        </Link>
        <p>
          El futuro no está escrito.
          <br />
          Las probabilidades sí se pueden leer.
        </p>
        <div>
          <Link to="/acerca">Sobre el proyecto y el equipo</Link>
          <span>Datos de Polymarket · Sin apuestas en esta app</span>
          <span>Proyecto DBP · UTEC</span>
        </div>
      </footer>
    </>
  );
}
