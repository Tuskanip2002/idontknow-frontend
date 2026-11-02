import { projectTeam } from "@idk/api";
import { useNavigation } from "../navigation";
import { useState, useEffect } from "react";
import { View, Text, ScrollView, Pressable, Share } from "react-native";
import { dateLabel, categoryLabel, type Category } from "@idk/api";
import { api, externalMarket, webOrigin } from "../services/api";
import { notify } from "../contexts/AppContext";
import { useQuery } from "../hooks/useQuery";
import {
  Page,
  Heading,
  HeadlineCard,
  QueryState,
  Empty,
  Button,
  Field,
  Probability,
  styles,
  colors,
} from "../components/ui";
import HistoryChart from "../components/HistoryChart";
export function HomeScreen() {
  const query = useQuery((s) => api.edition("hoy", s), "home");
  const nav = useNavigation();
  return (
    <Page onRefresh={query.reload} refreshing={query.loading}>
      <Heading
        eyebrow="EL FUTURO, EN PERSPECTIVA"
        title="Las probabilidades también son noticia."
        description={
          query.data
            ? dateLabel(query.data.fecha)
            : "Una nueva mirada a lo que viene."
        }
      />
      <View style={styles.row}>
        <Button
          title="Lectura por inclinación"
          onPress={() => nav.go({ name: "reader" })}
        />
        <Button
          title="Escanear QR"
          outline
          onPress={() => nav.go({ name: "scan" })}
        />
      </View>
      <QueryState query={query}>
        {query.data?.titulares.length ? (
          query.data.titulares.map((h) => (
            <HeadlineCard key={h.id} headline={h} />
          ))
        ) : (
          <Empty
            title="La portada está en preparación"
            description="Explora los mercados o el archivo mientras llega la próxima edición."
          />
        )}
      </QueryState>
      <Button
        title="Cómo funciona el diario →"
        outline
        onPress={() => nav.go({ name: "about" })}
      />
    </Page>
  );
}
export function MarketsScreen() {
  const [search, setSearch] = useState("");
  const [q, setQ] = useState("");
  const [category, setCategory] = useState("");
  const [page, setPage] = useState(0);
  const [ascending, setAscending] = useState(false);
  const nav = useNavigation();
  useEffect(() => {
    const t = setTimeout(() => {
      setQ(search);
      setPage(0);
    }, 350);
    return () => clearTimeout(t);
  }, [search]);
  const categories = useQuery(api.categories, "categories");
  const query = useQuery(
    (s) =>
      api.markets(
        {
          q,
          categoria: category,
          page,
          size: 10,
          sort: "probabilidadActual," + (ascending ? "asc" : "desc"),
        },
        s,
      ),
    JSON.stringify({ q, category, page, ascending }),
  );
  return (
    <Page onRefresh={query.reload} refreshing={query.loading}>
      <Heading
        eyebrow="EXPLORA EL RADAR"
        title="Mercados"
        description="Busca un tema y observa cómo cambia."
      />
      <Field
        label="Buscar mercados"
        placeholder="Elecciones, economía, tecnología…"
        value={search}
        onChangeText={setSearch}
      />
      <ScrollView horizontal showsHorizontalScrollIndicator={false}>
        <Pressable
          accessibilityRole="button"
          style={[styles.chip, !category && styles.chipActive]}
          onPress={() => {
            setCategory("");
            setPage(0);
          }}
        >
          <Text>Todos</Text>
        </Pressable>
        {categories.data?.map((c: Category) => (
          <Pressable
            accessibilityRole="button"
            key={c.id}
            style={[styles.chip, category === c.nombre && styles.chipActive]}
            onPress={() => {
              setCategory(c.nombre);
              setPage(0);
            }}
          >
            <Text>{categoryLabel(c.nombre)}</Text>
          </Pressable>
        ))}
      </ScrollView>
      {categories.error && (
        <Button
          outline
          title="Reintentar categorías"
          onPress={categories.reload}
        />
      )}
      <Button
        outline
        title={
          ascending
            ? "Orden: menor probabilidad ↑"
            : "Orden: mayor probabilidad ↓"
        }
        onPress={() => {
          setAscending((v) => !v);
          setPage(0);
        }}
      />
      <QueryState query={query}>
        {query.data?.content.length ? (
          query.data.content.map((m) => (
            <Pressable
              accessibilityRole="link"
              key={m.id}
              style={styles.card}
              onPress={() => nav.go({ name: "market", id: m.id })}
            >
              <Text style={styles.eyebrow}>
                {m.categorias.map(categoryLabel).join(" · ") || "MERCADO"}
              </Text>
              <Text style={styles.subtitle}>{m.preguntaOriginal}</Text>
              <Probability value={m.probabilidadActual} />
              <Text style={styles.caption}>
                {m.resuelto ? "Resuelto" : "Ver historial →"}
              </Text>
            </Pressable>
          ))
        ) : (
          <Empty
            title="Sin resultados"
            description="Prueba otro término o cambia de categoría."
          />
        )}
        {query.data && (
          <>
            <Text style={styles.caption}>
              {query.data.totalElements} resultados · Página {page + 1} de{" "}
              {Math.max(1, query.data.totalPages)}
            </Text>
            <View style={styles.row}>
              <Button
                title="← Anterior"
                outline
                disabled={page === 0}
                onPress={() => setPage((v) => v - 1)}
              />
              <Button
                title="Siguiente →"
                outline
                disabled={page + 1 >= query.data.totalPages}
                onPress={() => setPage((v) => v + 1)}
              />
            </View>
          </>
        )}
      </QueryState>
    </Page>
  );
}
export function ArchiveScreen() {
  const [page, setPage] = useState(0);
  const nav = useNavigation();
  const query = useQuery(
    (s) => api.editions({ page, size: 10, sort: "fecha,desc" }, s),
    "archive" + page,
  );
  return (
    <Page onRefresh={query.reload} refreshing={query.loading}>
      <Heading eyebrow="LA PERSPECTIVA DEL TIEMPO" title="Archivo" />
      <QueryState query={query}>
        {query.data?.content.map((e) => (
          <Pressable
            accessibilityRole="link"
            style={styles.card}
            key={e.id}
            onPress={() => nav.go({ name: "edition", date: e.fecha })}
          >
            <Text style={styles.eyebrow}>EDICIÓN DEL DÍA</Text>
            <Text style={styles.subtitle}>{dateLabel(e.fecha)}</Text>
            <Text style={styles.body}>
              {e.cantidadTitulares} titulares · Leer edición →
            </Text>
          </Pressable>
        ))}
        {query.data?.content.length === 0 && (
          <Empty
            title="El archivo aún está vacío"
            description="Las ediciones publicadas aparecerán aquí."
          />
        )}
        {query.data && (
          <View style={styles.row}>
            <Button
              outline
              title="← Anterior"
              disabled={page === 0}
              onPress={() => setPage((v) => v - 1)}
            />
            <Text style={styles.caption}>
              {page + 1} / {Math.max(1, query.data.totalPages)}
            </Text>
            <Button
              outline
              title="Siguiente →"
              disabled={page + 1 >= query.data.totalPages}
              onPress={() => setPage((v) => v + 1)}
            />
          </View>
        )}
      </QueryState>
    </Page>
  );
}
export function EditionScreen({ date }: { date: string }) {
  const query = useQuery((s) => api.edition(date, s), "edition" + date);
  return (
    <Page>
      <Heading eyebrow="EDICIÓN DEL DIARIO" title={dateLabel(date)} />
      <QueryState query={query}>
        {query.data?.titulares.map((h) => (
          <HeadlineCard key={h.id} headline={h} />
        ))}
      </QueryState>
    </Page>
  );
}
function ExternalContext({ id }: { id: string }) {
  const query = useQuery((s) => externalMarket(id, s), "external" + id);
  return (
    <View style={styles.card}>
      <Text style={styles.subtitle}>Desde la fuente</Text>
      <Text style={styles.caption}>
        Información complementaria de la API de Polymarket.
      </Text>
      <QueryState query={query}>
        {query.data && (
          <>
            <Text style={[styles.body, { marginTop: 15 }]}>
              {query.data.description ||
                "La fuente no incluye una descripción adicional."}
            </Text>
            <Text style={[styles.caption, { marginTop: 15 }]}>
              Volumen:{" "}
              {Number.isFinite(Number(query.data.volume))
                ? new Intl.NumberFormat("es-PE", {
                    style: "currency",
                    currency: "USD",
                    maximumFractionDigits: 0,
                  }).format(Number(query.data.volume))
                : "No disponible"}
            </Text>
          </>
        )}
      </QueryState>
    </View>
  );
}
export function DetailScreen({
  kind,
  id,
}: {
  kind: "market" | "headline";
  id: number;
}) {
  const nav = useNavigation();
  const query = useQuery(
    async (s) =>
      kind === "market"
        ? { kind: "market" as const, value: await api.market(id, s) }
        : { kind: "headline" as const, value: await api.headline(id, s) },
    kind + id,
  );
  const d = query.data;
  return (
    <Page>
      <QueryState query={query}>
        {d && (
          <>
            <Heading
              eyebrow={
                kind === "market"
                  ? "MERCADO EN SEGUIMIENTO"
                  : "LA NOTICIA EN CONTEXTO"
              }
              title={
                d.kind === "market"
                  ? d.value.preguntaOriginal
                  : d.value.textoEspanol
              }
            />
            <View style={[styles.card, { backgroundColor: colors.mint }]}>
              <Probability
                value={
                  d.kind === "market"
                    ? d.value.probabilidadActual
                    : d.value.probabilidadHoy
                }
              />
              <Text style={styles.body}>
                Una estimación del mercado, no una certeza.
              </Text>
              {d.value.fechaResolucionEstimada && (
                <Text style={styles.caption}>
                  Resolución estimada:{" "}
                  {dateLabel(d.value.fechaResolucionEstimada)}
                </Text>
              )}
            </View>
            <HistoryChart points={d.value.historial} />
            {d.kind === "headline" && (
              <Button
                title="Abrir mercado y su contexto →"
                outline
                onPress={async () => {
                  try {
                    const edition = await api.edition();
                    const headline = edition.titulares.find((h) => h.id === id);
                    if (headline)
                      nav.go({ name: "market", id: headline.mercadoId });
                    else nav.go({ name: "markets" });
                  } catch {
                    nav.go({ name: "markets" });
                  }
                }}
              />
            )}
            {d.kind === "market" &&
              d.value.polymarketId &&
              !d.value.polymarketId.startsWith("demo-") && (
                <ExternalContext id={d.value.polymarketId} />
              )}
            <Button
              outline
              title="Compartir noticia ↗"
              onPress={() => {
                Share.share({
                  message:
                    webOrigin +
                    "/" +
                    (kind === "market" ? "mercados" : "titulares") +
                    "/" +
                    id,
                }).catch(() => {});
              }}
            />
          </>
        )}
      </QueryState>
    </Page>
  );
}
export function AboutScreen() {
  return (
    <Page>
      <Heading eyebrow="UNA MIRADA DISTINTA" title="Entender lo que viene" />
      <Text style={styles.subtitle}>Una probabilidad no es una promesa.</Text>
      <Text style={styles.body}>
        Este diario convierte las expectativas de mercados de predicción en
        noticias en español. Los datos principales vienen de nuestro backend,
        que guarda una observación diaria y selecciona los cambios más
        relevantes.
      </Text>
      <Text style={styles.subtitle}>Cómo leerlo</Text>
      <Text style={styles.body}>
        Un 70 % indica una expectativa del mercado, sin garantizar el resultado.
        El cambio se expresa en puntos porcentuales: pasar de 40 % a 55 % es una
        subida de 15 pp.
      </Text>
      <Text style={styles.subtitle}>Tu propio radar</Text>
      <Text style={styles.body}>
        Guarda titulares y sigue categorías. Si un tema que sigues cruza el 50 %
        al alza, el sistema puede enviarte un correo.
      </Text>
      <Text style={styles.body}>
        No se realizan apuestas en esta aplicación. Proyecto DBP · UTEC.
      </Text>
      <Text style={styles.subtitle}>Nuestro equipo</Text>
      <Text style={styles.body}>
        CS2031 · Desarrollo Basado en Plataformas · UTEC · 2026-2
      </Text>
      {projectTeam.map((member) => (
        <View style={styles.card} key={member.name}>
          <Text style={styles.label}>{member.name}</Text>
          <Text style={[styles.caption, { marginTop: 6 }]}>
            Integrante del equipo
          </Text>
        </View>
      ))}
    </Page>
  );
}
