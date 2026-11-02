import { Link, useParams, useSearchParams } from "react-router-dom";
import { CalendarDays, ArrowUpRight } from "lucide-react";
import { pageQuery, dateLabel } from "@idk/api";
import { api } from "../services/api";
import { useQuery } from "../hooks/useQuery";
import {
  PageHeading,
  QueryState,
  EmptyState,
  Pagination,
} from "../components/ui";
import HeadlineCard from "../components/HeadlineCard";
export default function Archive() {
  const [params, setParams] = useSearchParams();
  const { page, size } = pageQuery(params);
  const query = useQuery(
    (s) => api.editions({ page, size, sort: "fecha,desc" }, s),
    params.toString(),
  );
  return (
    <>
      <PageHeading
        eyebrow="LA PERSPECTIVA DEL TIEMPO"
        title="Archivo de ediciones"
        description="Vuelve a las noticias de cada día y observa cómo cambiaron las expectativas."
      />
      <QueryState query={query}>
        {query.data?.content.length ? (
          <div className="archive-grid">
            {query.data.content.map((e) => (
              <Link
                to={"/archivo/" + e.fecha}
                className="archive-card"
                key={e.id}
              >
                <CalendarDays size={24} />
                <span className="eyebrow">EDICIÓN DEL DÍA</span>
                <h2>{dateLabel(e.fecha)}</h2>
                <p>{e.cantidadTitulares} titulares</p>
                <span className="text-link">
                  Leer edición <ArrowUpRight size={16} />
                </span>
              </Link>
            ))}
          </div>
        ) : (
          <EmptyState
            title="El archivo empieza con la primera edición"
            description="Aquí encontrarás las portadas publicadas por el diario."
          />
        )}
        {query.data && (
          <Pagination
            data={query.data}
            onChange={(page, size) =>
              setParams({ page: String(page), size: String(size) })
            }
          />
        )}
      </QueryState>
    </>
  );
}
export function EditionPage() {
  const { date } = useParams();
  const query = useQuery((s) => api.edition(date, s), "edition:" + date);
  return (
    <>
      <PageHeading
        eyebrow="ARCHIVO"
        title={query.data ? dateLabel(query.data.fecha) : "Edición del diario"}
      />
      <QueryState query={query}>
        {query.data?.titulares.length ? (
          <div className="headline-grid">
            {query.data.titulares.map((h, i) => (
              <HeadlineCard key={h.id} headline={h} lead={i === 0} />
            ))}
          </div>
        ) : (
          <EmptyState
            title="Esta edición no tiene titulares"
            description="Consulta otras fechas en el archivo."
            action="Ver archivo"
            to="/archivo"
          />
        )}
      </QueryState>
    </>
  );
}
