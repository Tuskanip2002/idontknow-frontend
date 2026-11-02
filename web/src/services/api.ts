import { createClient, createApi, type Session } from "@idk/api";
let session: Session | null = null;
export const sessionStore = {
  get: async () => session,
  set: async (value: Session | null) => {
    session = value;
    window.dispatchEvent(new Event("idk:session"));
  },
};
export const auth = createClient("/api/v1", sessionStore, "web", () =>
  window.dispatchEvent(new Event("idk:expired")),
);
export const api = createApi(auth.client);
