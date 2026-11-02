import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { ArrowUpRight, Check } from "lucide-react";
import { validPassword } from "@idk/api";
import { useAuth } from "../contexts/AppContext";
import { Field, Button } from "../components/ui";
export default function AuthPage({ register = false }: { register?: boolean }) {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [touched, setTouched] = useState(false);
  const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  const valid =
    emailValid &&
    password.length > 0 &&
    (!register ||
      (!!name.trim() &&
        name.trim().length <= 100 &&
        validPassword(password) &&
        confirm === password));
  return (
    <div className="auth-layout">
      <section className="auth-story">
        <p className="eyebrow">TU PERSPECTIVA IMPORTA</p>
        <h1>
          Un poco menos
          <br />
          de incertidumbre.
          <br />
          <em>
            Un poco más
            <br />
            de contexto.
          </em>
        </h1>
        <ul>
          <li>
            <Check />
            Guarda las noticias que te interesan.
          </li>
          <li>
            <Check />
            Sigue los temas que mueven tu mundo.
          </li>
          <li>
            <Check />
            Recibe alertas cuando cambien las expectativas.
          </li>
        </ul>
      </section>
      <section className="auth-form">
        <p className="eyebrow">BIENVENIDO AL DIARIO</p>
        <h2>{register ? "Crea tu cuenta" : "Qué bueno verte de nuevo"}</h2>
        <p className="muted">
          {register
            ? "Empieza a construir tu propia perspectiva."
            : "Inicia sesión para continuar tu lectura."}
        </p>
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            setTouched(true);
            if (!valid) return;
            setBusy(true);
            setError("");
            try {
              await signIn(
                {
                  email: email.trim(),
                  password,
                  ...(register ? { nombre: name.trim() } : {}),
                },
                register,
              );
              const from = location.state?.from;
              navigate(
                typeof from === "string" &&
                  from.startsWith("/") &&
                  !from.startsWith("//")
                  ? from
                  : "/",
                { replace: true },
              );
            } catch (e) {
              setError((e as Error).message);
            } finally {
              setBusy(false);
            }
          }}
          noValidate
        >
          {register && (
            <Field
              label="Nombre"
              value={name}
              maxLength={100}
              autoComplete="name"
              required
              onChange={(e) => setName(e.target.value)}
              error={touched && !name.trim() ? "Escribe tu nombre." : undefined}
            />
          )}
          <Field
            label="Correo electrónico"
            type="email"
            value={email}
            autoComplete="email"
            required
            onChange={(e) => setEmail(e.target.value)}
            onBlur={() => setTouched(true)}
            error={
              touched && !emailValid ? "Escribe un correo válido." : undefined
            }
          />
          <Field
            label="Contraseña"
            type="password"
            value={password}
            autoComplete={register ? "new-password" : "current-password"}
            required
            onChange={(e) => setPassword(e.target.value)}
            hint={
              register
                ? "Al menos 8 caracteres, una mayúscula, una minúscula y un número."
                : undefined
            }
            error={
              register && password && !validPassword(password)
                ? "La contraseña aún no cumple los requisitos."
                : undefined
            }
          />
          {register && (
            <Field
              label="Confirmar contraseña"
              type="password"
              value={confirm}
              autoComplete="new-password"
              required
              onChange={(e) => setConfirm(e.target.value)}
              error={
                confirm && confirm !== password
                  ? "Las contraseñas no coinciden."
                  : undefined
              }
            />
          )}
          {error && (
            <p className="form-error" role="alert">
              {error}
            </p>
          )}
          <Button type="submit" busy={busy} className="full">
            {register ? "Crear mi cuenta" : "Iniciar sesión"}
            <ArrowUpRight size={17} />
          </Button>
        </form>
        <p className="auth-switch">
          {register ? "¿Ya tienes cuenta?" : "¿Aún no tienes cuenta?"}{" "}
          <Link to={register ? "/login" : "/registro"}>
            {register ? "Inicia sesión" : "Regístrate"}
          </Link>
        </p>
      </section>
    </div>
  );
}
