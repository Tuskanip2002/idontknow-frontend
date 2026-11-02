import { lazy, Suspense } from "react";
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  Outlet,
  useLocation,
} from "react-router-dom";
import { AuthProvider, ToastProvider, useAuth } from "./contexts/AppContext";
import Layout from "./components/Layout";
import { LoadingState, ErrorBoundary, EmptyState } from "./components/ui";
const Home = lazy(() => import("./pages/Home"));
const Markets = lazy(() => import("./pages/Markets"));
const Archive = lazy(() => import("./pages/Archive"));
const Edition = lazy(() =>
  import("./pages/Archive").then((m) => ({ default: m.EditionPage })),
);
const Detail = lazy(() => import("./pages/Detail"));
const Auth = lazy(() => import("./pages/Auth"));
const Saved = lazy(() => import("./pages/Account"));
const Topics = lazy(() =>
  import("./pages/Account").then((m) => ({ default: m.Topics })),
);
const Profile = lazy(() =>
  import("./pages/Account").then((m) => ({ default: m.Profile })),
);
const Admin = lazy(() =>
  import("./pages/Account").then((m) => ({ default: m.Admin })),
);
const About = lazy(() => import("./pages/About"));
function Protected({ admin = false }: { admin?: boolean }) {
  const { user, ready } = useAuth();
  const location = useLocation();
  if (!ready) return <LoadingState />;
  if (!user)
    return (
      <Navigate
        to="/login"
        state={{ from: location.pathname + location.search }}
        replace
      />
    );
  if (admin && user.role !== "ADMIN")
    return (
      <EmptyState
        title="Esta sección es para administradores"
        description="Puedes seguir leyendo el diario desde la portada."
        action="Volver a portada"
        to="/"
      />
    );
  return <Outlet />;
}
export default function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <Suspense fallback={<LoadingState />}>
              <Routes>
                <Route element={<Layout />}>
                  <Route index element={<Home />} />
                  <Route path="mercados" element={<Markets />} />
                  <Route
                    path="mercados/:id"
                    element={<Detail kind="market" />}
                  />
                  <Route
                    path="titulares/:id"
                    element={<Detail kind="headline" />}
                  />
                  <Route path="archivo" element={<Archive />} />
                  <Route path="archivo/:date" element={<Edition />} />
                  <Route path="login" element={<Auth key="login" />} />
                  <Route
                    path="registro"
                    element={<Auth key="register" register />}
                  />
                  <Route path="acerca" element={<About />} />
                  <Route element={<Protected />}>
                    <Route path="guardados" element={<Saved />} />
                    <Route path="temas" element={<Topics />} />
                    <Route path="perfil" element={<Profile />} />
                  </Route>
                  <Route element={<Protected admin />}>
                    <Route path="admin" element={<Admin />} />
                  </Route>
                  <Route
                    path="*"
                    element={
                      <EmptyState
                        title="Esta página se salió del radar"
                        description="El enlace no existe o cambió de dirección."
                        action="Volver a la portada"
                        to="/"
                      />
                    }
                  />
                </Route>
              </Routes>
            </Suspense>
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    </ErrorBoundary>
  );
}
