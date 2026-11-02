import { Link } from "react-router-dom";
import {
  ArrowRight,
  ArrowUpRight,
  Newspaper,
  Activity,
  Bookmark,
} from "lucide-react";
import { useMemo } from "react";
import { dateLabel } from "@idk/api";
import { api } from "../services/api";
import { useQuery } from "../hooks/useQuery";
import { useAuth } from "../contexts/AppContext";
import HeadlineCard from "../components/HeadlineCard";
import DailyBriefing from "../components/DailyBriefing";
import TopicLinks from "../components/TopicLinks";
import { PageHeading, QueryState, EmptyState } from "../components/ui";
export default function Home() {
  const query = useQuery((s) => api.edition("hoy", s), "today");
  const { user } = useAuth();
  const headlines = query.data?.titulares || [];
  const rising = useMemo(
    () => headlines.filter((h) => h.cambioDesdeAyer > 0).length,
    [query.data],
  );
  return (
    <>
      <div className="edition-line">
        <span>
          <i className="live-dot" />
          Edición diaria
        </span>
        <span>
          {dateLabel(
            query.data?.fecha ||
              new Intl.DateTimeFormat("en-CA", {
                timeZone: "America/Lima",
              }).format(new Date()),
          )}
        </span>
      </div>
      <section className="intro intro-editorial">
        <div className="intro-copy">
          <p className="eyebrow">El futuro, en perspectiva</p>
          <h1>
            Las noticias cambian.
            <br />
            Las probabilidades <em>también.</em>
          </h1>
          <p>
            Lo que el mundo espera, lo que cambió desde ayer y los temas que
            vale la pena seguir.
          </p>
          <div className="intro-actions">
            <a href="#portada" className="text-link">
              Leer la edición <ArrowRight size={15} />
            </a>
            <Link to="/acerca" className="intro-guide">
              Cómo interpretar los datos
            </Link>
          </div>
        </div>
        <DailyBriefing headlines={headlines} loading={query.loading} />
      </section>
      <TopicLinks />
      <div className="stat-strip edition-stats">
        <div>
          <Newspaper size={18} />
          <span>En esta edición</span>
          <strong>
            {query.data ? headlines.length : "—"} titulares seleccionados
          </strong>
        </div>
        <div>
          <Activity size={18} />
          <span>Probabilidades en movimiento</span>
          <strong>
            {query.data ? rising : "—"} al alza ·{" "}
            {query.data
              ? headlines.filter((h) => h.cambioDesdeAyer < 0).length
              : "—"}{" "}
            a la baja
          </strong>
        </div>
        <div>
          <Bookmark size={18} />
          <span>Tu biblioteca personal</span>
          <Link to={user ? "/guardados" : "/registro"}>
            {user ? "Continuar mis lecturas" : "Guardar lo que importa"}
            <ArrowUpRight size={15} />
          </Link>
        </div>
      </div>
      <section
        id="portada"
        className="front-page"
        aria-label="Titulares de la edición"
      >
        <PageHeading eyebrow="Las señales de hoy" title="La portada" level={2}>
          <Link to="/mercados" className="text-link">
            Explorar todos los mercados <ArrowRight size={16} />
          </Link>
        </PageHeading>
        {query.error?.status === 404 ? (
          <EmptyState
            title="La edición de hoy aún está en preparación"
            description="Mientras se publica, explora los mercados o consulta una edición anterior."
            action="Consultar el archivo"
            to="/archivo"
          />
        ) : (
          <QueryState query={query}>
            {headlines.length ? (
              <div className="headline-grid">
                {headlines.map((headline, i) => (
                  <HeadlineCard
                    key={headline.id}
                    headline={headline}
                    lead={i === 0}
                  />
                ))}
              </div>
            ) : (
              <EmptyState
                title="Una nueva edición está por llegar"
                description="Puedes explorar los mercados disponibles mientras tanto."
                action="Explorar mercados"
                to="/mercados"
              />
            )}
          </QueryState>
        )}
      </section>
      <section className="reading-guide" aria-label="Cómo leer una noticia">
        <div>
          <p className="eyebrow">Un minuto de contexto</p>
          <h2>
            Lee el cambio.
            <br />
            Entiende la perspectiva.
          </h2>
          <Link to="/acerca" className="text-link">
            Conoce cómo funciona <ArrowUpRight size={15} />
          </Link>
        </div>
        <ol>
          <li>
            <span>01</span>
            <div>
              <h3>La probabilidad</h3>
              <p>
                Lo que el mercado estima que podría ocurrir. Una expectativa que
                puede cambiar.
              </p>
            </div>
          </li>
          <li>
            <span>02</span>
            <div>
              <h3>El movimiento</h3>
              <p>
                Los puntos porcentuales muestran cuánto cambió la expectativa
                frente a ayer.
              </p>
            </div>
          </li>
          <li>
            <span>03</span>
            <div>
              <h3>La historia completa</h3>
              <p>
                Abre el gráfico y compara las observaciones de cada día antes de
                sacar conclusiones.
              </p>
            </div>
          </li>
        </ol>
      </section>
    </>
  );
}
