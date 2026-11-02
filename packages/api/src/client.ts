import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import type { Session } from "./types";
export interface SessionStore {
  get(): Promise<Session | null>;
  set(session: Session | null): Promise<void>;
}
export class ApiError extends Error {
  constructor(
    message: string,
    public status: number = 0,
  ) {
    super(message);
    this.name = "ApiError";
  }
}
export function friendlyError(error: unknown): ApiError {
  if (error instanceof ApiError) return error;
  if (!axios.isAxiosError(error))
    return new ApiError("No pudimos completar la acción. Inténtalo otra vez.");
  const status = error.response?.status || 0;
  if (status === 401 && error.config?.url?.includes("/auth/login"))
    return new ApiError("Correo o contraseña incorrectos.", 401);
  const messages: Record<number, string> = {
    400: "Revisa los datos del formulario.",
    401: "Tu sesión terminó. Vuelve a iniciar sesión.",
    403: "No tienes permiso para realizar esta acción.",
    404: "No encontramos lo que buscas.",
    409: "Este registro ya existe.",
    429: "Hay muchas solicitudes. Espera un momento.",
    500: "El servicio tiene un problema. Inténtalo en unos minutos.",
    502: "El servidor no está disponible por ahora.",
    503: "El servicio se está iniciando. Inténtalo de nuevo.",
  };
  return new ApiError(
    status === 0
      ? error.code === "ECONNABORTED"
        ? "El servidor tarda en responder. Puedes volver a intentarlo."
        : "No pudimos conectar. Comprueba tu conexión."
      : messages[status] || "No pudimos completar la solicitud.",
    status,
  );
}
type RequestConfig = InternalAxiosRequestConfig & {
  _retried?: boolean;
  _attempt?: number;
};
export function createClient(
  baseURL: string,
  store: SessionStore,
  platform: "web" | "mobile",
  onExpired: () => void = () => {},
) {
  const client = axios.create({
    baseURL,
    timeout: 70000,
    withCredentials: platform === "web",
    headers: { "Content-Type": "application/json", "X-Client-Type": platform },
  });
  const auth = axios.create({
    baseURL,
    timeout: 70000,
    withCredentials: platform === "web",
    headers: { "Content-Type": "application/json", "X-Client-Type": platform },
  });
  let refreshing: Promise<Session> | null = null;
  async function refresh(): Promise<Session> {
    if (!refreshing)
      refreshing = (async () => {
        const session = await store.get();
        try {
          const { data } = await auth.post<Session>(
            "/auth/refresh",
            platform === "mobile"
              ? { refreshToken: session?.refreshToken }
              : {},
          );
          await store.set(data);
          return data;
        } catch (error) {
          await store.set(null);
          onExpired();
          throw friendlyError(error);
        } finally {
          refreshing = null;
        }
      })();
    return refreshing;
  }
  client.interceptors.request.use(async (config) => {
    const session = await store.get();
    if (session) config.headers.Authorization = "Bearer " + session.token;
    return config;
  });
  client.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      if (axios.isCancel(error)) throw error;
      const config = error.config as RequestConfig | undefined;
      if (
        config &&
        error.response?.status === 401 &&
        !config._retried &&
        !config.url?.startsWith("/auth/")
      ) {
        config._retried = true;
        const session = await refresh();
        config.headers.Authorization = "Bearer " + session.token;
        return client(config);
      }
      const transient =
        !error.response || [502, 503, 504].includes(error.response.status);
      if (
        config &&
        config.method === "get" &&
        transient &&
        (config._attempt || 0) < 2 &&
        !config.signal?.aborted
      ) {
        config._attempt = (config._attempt || 0) + 1;
        await new Promise((resolve) =>
          setTimeout(resolve, 300 * config._attempt!),
        );
        if (config.signal?.aborted) throw new axios.CanceledError();
        return client(config);
      }
      throw friendlyError(error);
    },
  );
  async function authenticate(path: string, body: unknown) {
    try {
      const { data } = await auth.post<Session>(path, body);
      await store.set(data);
      return data;
    } catch (e) {
      throw friendlyError(e);
    }
  }
  async function logout() {
    try {
      const session = await store.get();
      await auth.post(
        "/auth/logout",
        platform === "mobile" ? { refreshToken: session?.refreshToken } : {},
      );
    } catch (e) {
      throw friendlyError(e);
    } finally {
      await store.set(null);
    }
  }
  return {
    client,
    refresh,
    login: (body: unknown) => authenticate("/auth/login", body),
    register: (body: unknown) => authenticate("/auth/register", body),
    logout,
  };
}
