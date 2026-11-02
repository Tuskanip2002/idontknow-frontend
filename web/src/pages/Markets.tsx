import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Search, ArrowUpRight } from "lucide-react";
import { pageQuery, categoryLabel } from "@idk/api";
import { api } from "../services/api";
import { useQuery, useDebounce } from "../hooks/useQuery";
import {
  PageHeading,
  Probability,
  QueryState,
  EmptyState,
  Pagination,
} from "../components/ui";
export default function Markets() {
  const [params, setParams] = useSearchParams();
  const { page, size } = pageQuery(params);
  const q = params.get("q") || "";
  const [search, setSearch] = useState(q);
  const debounced = useDebounce(search);
  const categoria = params.get("categoria") || "";
  const sort = [
    "probabilidadActual,desc",
    "probabilidadActual,asc",
    "id,desc",
  ].includes(params.get("sort") || "")
    ? params.get("sort")!
    : "id,desc";
  const query = useQuery(
    (s) => api.markets({ page, size, q, categoria, sort }, s),
    params.toString(),
  );
  const categories = useQuery(api.categories, "categories");
  function change(values: Record<string, string | number>) {
    setParams((prev) => {
      const next = new URLSearchParams(prev);
      Object.entries(values).forEach(([k, v]) =>
        v === "" ? next.delete(k) : next.set(k, String(v)),
      );
      return next;
    });
  }
  useEffect(() => {
    setSearch(q);
  }, [q]);
  useEffect(() => {
    if (debounced !== q) change({ q: debounced, page: 0 });
  }, [debounced]);
  return (
    <>
      <PageHeading
        eyebrow="EXPLORA LAS POSIBILIDADES"
        title="El mundo, en probabilidades"
        description="Busca un tema, compara expectativas y sigue cómo evoluciona."
      />
      <section className="filters" aria-label="Buscar y filtrar">
        <label className="search-field">
          <Search size={19} />
          <span className="sr-only">Buscar mercados</span>
          <input
            type="search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="¿Qué está pasando en el mundo?"
          />
        </label>
        <label>
          Categoría
          <select
            value={categoria}
            onChange={(e) => change({ categoria: e.target.value, page: 0 })}
          >
            <option value="">Todos los temas</option>
            {categories.data?.map((c) => (
              <option key={c.id} value={c.nombre}>
                {categoryLabel(c.nombre)}
              </option>
            ))}
          </select>
        </label>
        <label>
          Ordenar por
          <select
            value={sort}
            onChange={(e) => change({ sort: e.target.value, page: 0 })}
          >
            <option value="id,desc">Más recientes</option>
            <option value="probabilidadActual,desc">Mayor probabilidad</option>
            <option value="probabilidadActual,asc">Menor probabilidad</option>
          </select>
        </label>
      </section>
      {categories.error && (
        <p className="muted">
          Las categorías no están disponibles.{" "}
          <button className="text-button" onClick={categories.reload}>
            Reintentar
          </button>
        </p>
      )}
      <QueryState query={query}>
        {query.data?.content.length ? (
          <div className="market-grid">
            {query.data.content.map((m) => (
              <article className="market-card" key={m.id}>
                <div className="tags">
                  {m.categorias.map((c) => (
                    <span key={c} className="tag">
                      {categoryLabel(c)}
                    </span>
                  ))}
                  <span className="muted">
                    {m.resuelto ? "Resuelto" : "En seguimiento"}
                  </span>
                </div>
                <h2>
                  <Link to={"/mercados/" + m.id}>{m.preguntaOriginal}</Link>
                </h2>
                <Probability value={m.probabilidadActual} />
                <Link className="text-link" to={"/mercados/" + m.id}>
                  Ver historial <ArrowUpRight size={16} />
                </Link>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState
            title="No encontramos mercados"
            description="Prueba con otra búsqueda o cambia la categoría."
          />
        )}
        {query.data && (
          <Pagination
            data={query.data}
            onChange={(page, size) => change({ page, size })}
          />
        )}
      </QueryState>
    </>
  );
}
