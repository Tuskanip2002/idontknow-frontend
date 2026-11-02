import { useParams, Link } from "react-router-dom";
import { useEffect, useState } from "react";
import QRCode from "qrcode";
import { QrCode, Link as LinkIcon, Bookmark, ArrowUpRight } from "lucide-react";
import { dateLabel } from "@idk/api";
import { api } from "../services/api";
import { useQuery } from "../hooks/useQuery";
import { useAuth, useToast } from "../contexts/AppContext";
import {
  PageHeading,
  Probability,
  ChangeBadge,
  QueryState,
  Button,
  Dialog,
} from "../components/ui";
import HistoryChart from "../components/HistoryChart";
function ShareDialog({ onClose }: { onClose: () => void }) {
  const [src, setSrc] = useState("");
  const [error, setError] = useState(false);
  const toast = useToast();
  useEffect(() => {
    let active = true;
    QRCode.toDataURL(window.location.href, {
      width: 260,
      margin: 2,
      color: { dark: "#142c25", light: "#ffffff" },
    })
      .then((v) => {
        if (active) setSrc(v);
      })
      .catch(() => setError(true));
    return () => {
      active = false;
    };
  }, []);
  return (
    <Dialog title="Lleva esta noticia contigo" onClose={onClose}>
      <p>Escanea el QR desde la app móvil para abrir esta misma noticia.</p>
      {src && (
        <img
          src={src}
          alt="Código QR con el enlace de esta noticia"
          width="260"
          height="260"
        />
      )}
      {error && (
        <p>No se pudo generar el QR. Copia el enlace para compartirlo.</p>
      )}
      <input
        readOnly
        aria-label="Enlace para compartir"
        value={window.location.href}
        onFocus={(e) => e.target.select()}
      />
      <Button
        onClick={async () => {
          try {
            await navigator.clipboard.writeText(window.location.href);
            toast("Enlace copiado.");
          } catch {
            toast("Selecciona el enlace y cópialo manualmente.", true);
          }
        }}
      >
        <LinkIcon size={16} />
        Copiar enlace
      </Button>
    </Dialog>
  );
}
export default function Detail({ kind }: { kind: "headline" | "market" }) {
  const { id } = useParams();
  const query = useQuery(
    async (s) =>
      kind === "market"
        ? { kind: "market" as const, value: await api.market(Number(id), s) }
        : {
            kind: "headline" as const,
            value: await api.headline(Number(id), s),
          },
    kind + id,
  );
  const [share, setShare] = useState(false);
  const [busy, setBusy] = useState(false);
  const { user, savedIds, updateSaved } = useAuth();
  const toast = useToast();
  const data = query.data;
  return (
    <>
      <QueryState query={query}>
        {data && (
          <>
            <PageHeading
              eyebrow={
                kind === "market"
                  ? "MERCADO EN SEGUIMIENTO"
                  : "LA NOTICIA EN CONTEXTO"
              }
              title={
                data.kind === "market"
                  ? data.value.preguntaOriginal
                  : data.value.textoEspanol
              }
            />
            <div className="detail-layout">
              <section>
                <div className="detail-summary">
                  <Probability
                    value={
                      data.kind === "market"
                        ? data.value.probabilidadActual
                        : data.value.probabilidadHoy
                    }
                    large
                  />
                  {data.kind === "headline" && (
                    <ChangeBadge value={data.value.cambioDesdeAyer} />
                  )}
                  <p>
                    Probabilidad estimada por participantes del mercado. No
                    representa una certeza.
                  </p>
                </div>
                <HistoryChart points={data.value.historial} />
              </section>
              <aside className="detail-aside">
                <h2>La perspectiva completa</h2>
                <p className="muted">
                  Las probabilidades reflejan expectativas colectivas y pueden
                  cambiar con nueva información.
                </p>
                <dl>
                  <dt>Resolución estimada</dt>
                  <dd>
                    {data.value.fechaResolucionEstimada
                      ? dateLabel(data.value.fechaResolucionEstimada)
                      : "Por confirmar"}
                  </dd>
                  {data.kind === "market" && (
                    <>
                      <dt>Estado</dt>
                      <dd>
                        {data.value.resuelto ? "Resuelto" : "En seguimiento"}
                      </dd>
                      <dt>Temas</dt>
                      <dd>{data.value.categorias.join(" · ") || "General"}</dd>
                    </>
                  )}
                </dl>
                <Button variant="outline" onClick={() => setShare(true)}>
                  <QrCode size={17} />
                  Compartir por QR
                </Button>
                {data.kind === "headline" &&
                  (user ? (
                    <Button
                      busy={busy}
                      onClick={async () => {
                        setBusy(true);
                        try {
                          const stored = savedIds.has(data.value.id);
                          if (stored) await api.unsave(data.value.id);
                          else await api.save(data.value.id);
                          updateSaved(data.value.id, !stored);
                          toast(
                            stored
                              ? "Titular eliminado de guardados."
                              : "Titular guardado.",
                          );
                        } catch (e) {
                          toast((e as Error).message, true);
                        } finally {
                          setBusy(false);
                        }
                      }}
                    >
                      <Bookmark size={17} />
                      {savedIds.has(data.value.id)
                        ? "Quitar de guardados"
                        : "Guardar titular"}
                    </Button>
                  ) : (
                    <Link
                      className="button"
                      to="/login"
                      state={{ from: location.pathname }}
                    >
                      Inicia sesión para guardar
                    </Link>
                  ))}
                <Link className="text-link" to="/acerca">
                  Cómo se calculan las probabilidades <ArrowUpRight size={15} />
                </Link>
              </aside>
            </div>
          </>
        )}
      </QueryState>
      {share && <ShareDialog onClose={() => setShare(false)} />}
    </>
  );
}
