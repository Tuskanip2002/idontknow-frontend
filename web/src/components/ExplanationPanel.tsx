import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import {
  Lock,
  Newspaper,
  MessageSquare,
  ExternalLink,
  Sparkles,
} from "lucide-react";
import { dateLabel, isPremium, type Explanation } from "@idk/api";
import { api } from "../services/api";
import { useQuery } from "../hooks/useQuery";
import { useAuth, useToast } from "../contexts/AppContext";
import { QueryState, Button, Dialog } from "./ui";

const sourceWord = (n: number) => (n === 1 ? "fuente" : "fuentes");

function teaserText(data: Explanation) {
  const kinds = data.tiposFuente
    .map((t) => (t === "NOTICIA" ? "noticias" : "comentarios del mercado"))
    .join(" y ");
  return `${data.cantidadFuentes} ${sourceWord(data.cantidadFuentes)}${
    kinds ? " (" + kinds + ")" : ""
  } ayudan a entender este movimiento.`;
}

/** Diálogo de la suscripción simulada: deja claro que no se cobra nada. */
export function SubscribeDialog({
  onClose,
  onDone,
}: {
  onClose: () => void;
  onDone?: () => void;
}) {
  const { reload } = useAuth();
  const toast = useToast();
  const [busy, setBusy] = useState(false);
  return (
    <Dialog title="I Don’t Know Premium" onClose={onClose}>
      <p>
        El número siempre es gratis. Con Premium ves <b>por qué</b> se movió: un
        resumen con las noticias y los comentarios del mercado que coinciden con
        el cambio, cada uno con su fuente.
      </p>
      <p className="muted">
        Modo demostración: se activa por 30 días y no se realiza ningún cobro.
      </p>
      <Button
        busy={busy}
        onClick={async () => {
          setBusy(true);
          try {
            await api.subscribe();
            await reload();
            toast("Premium activado por 30 días.");
            onDone?.();
            onClose();
          } catch (e) {
            toast((e as Error).message, true);
          } finally {
            setBusy(false);
          }
        }}
      >
        <Sparkles size={16} />
        Activar Premium (demo)
      </Button>
    </Dialog>
  );
}

export default function ExplanationPanel({
  headlineId,
}: {
  headlineId: number;
}) {
  const { user } = useAuth();
  const location = useLocation();
  const premium = isPremium(user);
  // el plan va en la clave: al activar Premium se vuelve a pedir la explicación completa
  const query = useQuery(
    (s) => api.explanation(headlineId, s),
    "explanation" + headlineId + (premium ? "p" : "f") + (user?.id ?? ""),
  );
  const [subscribe, setSubscribe] = useState(false);
  const data = query.data;

  return (
    <section className="explanation" aria-labelledby="explanation-title">
      <p className="eyebrow">Premium</p>
      <h2 id="explanation-title">¿Por qué este número?</h2>
      <QueryState query={query}>
        {data && !data.disponible && (
          <p className="muted">
            Estamos reuniendo las fuentes de este titular. Vuelve en unos
            minutos.
          </p>
        )}
        {data?.disponible && data.bloqueado && (
          <div className="explanation-locked">
            <div className="explanation-blur" aria-hidden="true">
              <span />
              <span />
              <span />
            </div>
            <p>
              <Lock size={15} /> {teaserText(data)}
            </p>
            {user ? (
              <Button onClick={() => setSubscribe(true)}>
                <Sparkles size={16} />
                Ver por qué
              </Button>
            ) : (
              <Link
                className="button"
                to="/login"
                state={{ from: location.pathname }}
              >
                Inicia sesión para ver por qué
              </Link>
            )}
          </div>
        )}
        {data?.disponible && !data.bloqueado && (
          <>
            <p className="explanation-summary">{data.resumen}</p>
            {data.fuentes.length > 0 && (
              <ol className="source-list">
                {data.fuentes.map((f, i) => (
                  <li key={i}>
                    <span className="source-kind">
                      {f.tipo === "NOTICIA" ? (
                        <Newspaper size={14} aria-label="Noticia" />
                      ) : (
                        <MessageSquare size={14} aria-label="Comentario" />
                      )}
                    </span>
                    <div>
                      {f.url ? (
                        <a
                          href={f.url}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          {f.titulo} <ExternalLink size={12} />
                        </a>
                      ) : (
                        <q>{f.titulo}</q>
                      )}
                      <small>
                        {[f.medio, f.fecha ? dateLabel(f.fecha) : null]
                          .filter(Boolean)
                          .join(" · ")}
                      </small>
                    </div>
                  </li>
                ))}
              </ol>
            )}
            <p className="muted explanation-note">
              Posibles factores según fuentes públicas, no causas confirmadas.
              Resumen generado con IA a partir de las fuentes citadas.
            </p>
          </>
        )}
      </QueryState>
      {subscribe && (
        <SubscribeDialog
          onClose={() => setSubscribe(false)}
          onDone={query.reload}
        />
      )}
    </section>
  );
}
