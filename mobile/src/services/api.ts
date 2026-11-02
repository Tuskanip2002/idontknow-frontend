import * as SecureStore from "expo-secure-store";
import { createClient, createApi, type Session, ApiError } from "@idk/api";
const key = "idk.session";
let current: Session | null = null;
let loaded = false;
export const sessionStore = {
  async get() {
    if (!loaded) {
      const raw = await SecureStore.getItemAsync(key);
      current = raw ? JSON.parse(raw) : null;
      loaded = true;
    }
    return current;
  },
  async set(value: Session | null) {
    current = value;
    loaded = true;
    if (value)
      await SecureStore.setItemAsync(key, JSON.stringify(value), {
        keychainAccessible: SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
      });
    else await SecureStore.deleteItemAsync(key);
  },
};
const baseURL =
  process.env.EXPO_PUBLIC_API_URL || "http://localhost:8080/api/v1";
let expired = () => {};
export const setExpiredHandler = (handler: () => void) => {
  expired = handler;
};
export const auth = createClient(baseURL, sessionStore, "mobile", () =>
  expired(),
);
export const api = createApi(auth.client);
export const webOrigin =
  process.env.EXPO_PUBLIC_WEB_URL || "http://localhost:5173";
export interface ExternalMarket {
  description?: string;
  volume?: string;
  liquidity?: string;
  question?: string;
}
export async function externalMarket(
  id: string,
  signal: AbortSignal,
): Promise<ExternalMarket> {
  const controller = new AbortController();
  const abort = () => controller.abort();
  signal.addEventListener("abort", abort);
  const timer = setTimeout(abort, 12000);
  try {
    const response = await fetch(
      "https://gamma-api.polymarket.com/markets/" + encodeURIComponent(id),
      { signal: controller.signal },
    );
    if (!response.ok) throw new Error();
    const data = await response.json();
    if (!data || typeof data !== "object") throw new Error();
    return data;
  } catch {
    throw new ApiError(
      "La fuente externa no está disponible. El historial del diario sigue disponible.",
    );
  } finally {
    clearTimeout(timer);
    signal.removeEventListener("abort", abort);
  }
}
