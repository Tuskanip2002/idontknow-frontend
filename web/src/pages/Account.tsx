import { useState } from "react";
import { Link } from "react-router-dom";
import { validPassword, categoryLabel } from "@idk/api";
import { api } from "../services/api";
import { useAuth, useToast } from "../contexts/AppContext";
import { useQuery } from "../hooks/useQuery";
import {
  PageHeading,
  QueryState,
  EmptyState,
  Field,
  Button,
  Dialog,
} from "../components/ui";
import HeadlineCard from "../components/HeadlineCard";
export default function Saved() {
  const query = useQuery(api.saved, "saved");
  return (
    <>
      <PageHeading
        eyebrow="TU BIBLIOTECA"
        title="Para leer con calma"
        description="Las noticias que elegiste guardar, en un solo lugar."
      />
      <QueryState query={query}>
        {query.data?.length ? (
          <div className="headline-grid">
            {query.data.map((s) => (
              <HeadlineCard
                key={s.id}
                headline={s.titular}
                saved
                onChange={query.reload}
              />
            ))}
          </div>
        ) : (
          <EmptyState
            title="Tu próxima lectura empieza aquí"
            description="Guarda un titular desde la portada y vuelve cuando quieras."
            action="Explorar la portada"
            to="/"
          />
        )}
      </QueryState>
    </>
  );
}
export function Topics() {
  const { user, reload } = useAuth();
  const query = useQuery(api.categories, "topics");
  const toast = useToast();
  const [busy, setBusy] = useState<number | null>(null);
  return (
    <>
      <PageHeading
        eyebrow="PERSONALIZA TU PERSPECTIVA"
        title="Los temas que te importan"
        description="Recibe un correo cuando un mercado de tus categorías cruce el 50 %."
      />
      <QueryState query={query}>
        <div className="topic-grid">
          {query.data?.map((c) => {
            const followed = user?.categoriasSeguidas.includes(c.nombre);
            return (
              <article className="topic-card" key={c.id}>
                <span className="topic-symbol" aria-hidden="true">
                  {categoryLabel(c.nombre).slice(0, 2)}
                </span>
                <h2>{categoryLabel(c.nombre)}</h2>
                <p className="muted">
                  {followed
                    ? "Estás al tanto de los cambios de perspectiva."
                    : "Añade este tema a tu radar de noticias."}
                </p>
                <Button
                  busy={busy === c.id}
                  disabled={busy !== null}
                  variant={followed ? "outline" : ""}
                  onClick={async () => {
                    setBusy(c.id);
                    try {
                      if (followed) await api.unfollow(c.id);
                      else await api.follow(c.id);
                      await reload();
                      toast(
                        followed
                          ? "Dejaste de seguir este tema."
                          : "Ahora sigues este tema.",
                      );
                    } catch (e) {
                      toast((e as Error).message, true);
                    } finally {
                      setBusy(null);
                    }
                  }}
                >
                  {followed ? "Dejar de seguir" : "+ Seguir tema"}
                </Button>
              </article>
            );
          })}
        </div>
        {query.data?.length === 0 && (
          <EmptyState
            title="Aún no hay categorías"
            description="Los temas aparecerán cuando se configuren en el diario."
          />
        )}
      </QueryState>
    </>
  );
}
export function Profile() {
  const { user, reload } = useAuth();
  const toast = useToast();
  const [name, setName] = useState(user?.nombre || "");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  return (
    <>
      <PageHeading
        eyebrow="TU ESPACIO EN EL DIARIO"
        title="Mi perfil"
        description="Actualiza tus datos y mantén tu cuenta al día."
      />
      <div className="profile-layout">
        <aside className="profile-card">
          <div className="avatar">{user?.nombre.slice(0, 1).toUpperCase()}</div>
          <h2>{user?.nombre}</h2>
          <p>{user?.email}</p>
          <Link className="text-link" to="/temas">
            {user?.categoriasSeguidas.length || 0} temas seguidos →
          </Link>
          <Link className="text-link" to="/guardados">
            Ver mis guardados →
          </Link>
        </aside>
        <form
          className="profile-form"
          onSubmit={async (e) => {
            e.preventDefault();
            if (!name.trim() || (password && !validPassword(password))) return;
            setBusy(true);
            try {
              await api.updateProfile({
                nombre: name.trim(),
                ...(password ? { password } : {}),
              });
              await reload();
              setPassword("");
              toast("Tu perfil se actualizó.");
            } catch (e) {
              toast((e as Error).message, true);
            } finally {
              setBusy(false);
            }
          }}
        >
          <h2>Información personal</h2>
          <Field
            label="Nombre"
            value={name}
            maxLength={100}
            required
            onChange={(e) => setName(e.target.value)}
          />
          <Field
            label="Correo electrónico"
            value={user?.email || ""}
            readOnly
            hint="El correo identifica tu cuenta y no se puede cambiar."
          />
          <h2>Cambiar contraseña</h2>
          <Field
            label="Nueva contraseña"
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            hint="Opcional. Al menos 8 caracteres, mayúscula, minúscula y número."
            error={
              password && !validPassword(password)
                ? "La contraseña no cumple los requisitos."
                : undefined
            }
          />
          <Button
            type="submit"
            busy={busy}
            disabled={!name.trim() || (!!password && !validPassword(password))}
          >
            Guardar cambios
          </Button>
        </form>
      </div>
    </>
  );
}
export function Admin() {
  const query = useQuery(api.categories, "admin-categories");
  const toast = useToast();
  const [name, setName] = useState("");
  const [busy, setBusy] = useState(false);
  const [deleting, setDeleting] = useState<{
    id: number;
    nombre: string;
  } | null>(null);
  return (
    <>
      <PageHeading
        eyebrow="ADMINISTRACIÓN"
        title="Categorías del diario"
        description="Organiza los temas disponibles para los lectores."
      />
      <form
        className="inline-form"
        onSubmit={async (e) => {
          e.preventDefault();
          if (!name.trim()) return;
          setBusy(true);
          try {
            await api.createCategory(name.trim());
            setName("");
            query.reload();
            toast("Categoría creada.");
          } catch (e) {
            toast((e as Error).message, true);
          } finally {
            setBusy(false);
          }
        }}
      >
        <Field
          label="Nombre de categoría"
          value={name}
          required
          maxLength={50}
          onChange={(e) => setName(e.target.value)}
        />
        <Button busy={busy} type="submit">
          Crear categoría
        </Button>
      </form>
      <QueryState query={query}>
        <div className="category-list">
          {query.data?.map((c) => (
            <div key={c.id}>
              <span>{categoryLabel(c.nombre)}</span>
              <Button variant="danger quiet" onClick={() => setDeleting(c)}>
                Eliminar
              </Button>
            </div>
          ))}
        </div>
      </QueryState>
      {deleting && (
        <Dialog
          title="¿Eliminar esta categoría?"
          onClose={() => !busy && setDeleting(null)}
        >
          <p>
            Se eliminará «{categoryLabel(deleting.nombre)}». Esta acción no se
            puede deshacer.
          </p>
          <div className="actions">
            <Button
              variant="outline"
              disabled={busy}
              onClick={() => setDeleting(null)}
            >
              Cancelar
            </Button>
            <Button
              variant="danger"
              busy={busy}
              onClick={async () => {
                setBusy(true);
                try {
                  await api.deleteCategory(deleting.id);
                  setDeleting(null);
                  query.reload();
                  toast("Categoría eliminada.");
                } catch (e) {
                  toast((e as Error).message, true);
                } finally {
                  setBusy(false);
                }
              }}
            >
              Eliminar categoría
            </Button>
          </div>
        </Dialog>
      )}
    </>
  );
}
