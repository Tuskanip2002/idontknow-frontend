export * from "./project";
export * from "./types";
export * from "./client";
export * from "./utils";
import type { createClient } from "./client";
import type {
  Category,
  Market,
  MarketDetail,
  HeadlineDetail,
  Edition,
  EditionSummary,
  Page,
  PageQuery,
  Profile,
  SavedHeadline,
} from "./types";
export function createApi(client: ReturnType<typeof createClient>["client"]) {
  const get = async <T>(url: string, signal?: AbortSignal, params?: object) =>
    (await client.get<T>(url, { signal, params })).data;
  return {
    status: (signal?: AbortSignal) =>
      get<{ status: string; demo: boolean }>("/estado", signal),
    categories: (signal?: AbortSignal) =>
      get<Category[]>("/categorias", signal),
    markets: (query: PageQuery, signal?: AbortSignal) =>
      get<Page<Market>>("/mercados", signal, query),
    market: (id: number, signal?: AbortSignal) =>
      get<MarketDetail>("/mercados/" + id, signal),
    edition: (date = "hoy", signal?: AbortSignal) =>
      get<Edition>("/ediciones/" + date, signal),
    editions: (query: PageQuery, signal?: AbortSignal) =>
      get<Page<EditionSummary>>("/ediciones", signal, query),
    headline: (id: number, signal?: AbortSignal) =>
      get<HeadlineDetail>("/titulares/" + id, signal),
    profile: (signal?: AbortSignal) => get<Profile>("/usuarios/me", signal),
    updateProfile: async (body: { nombre?: string; password?: string }) =>
      (await client.put<Profile>("/usuarios/me", body)).data,
    saved: (signal?: AbortSignal) => get<SavedHeadline[]>("/guardados", signal),
    save: async (titularId: number) =>
      (await client.post<SavedHeadline>("/guardados", { titularId })).data,
    unsave: async (id: number) => {
      await client.delete("/guardados/" + id);
    },
    follow: async (id: number) => {
      await client.post("/usuarios/me/categorias/" + id);
    },
    unfollow: async (id: number) => {
      await client.delete("/usuarios/me/categorias/" + id);
    },
    createCategory: async (nombre: string) => {
      await client.post("/categorias", { nombre });
    },
    deleteCategory: async (id: number) => {
      await client.delete("/categorias/" + id);
    },
  };
}
