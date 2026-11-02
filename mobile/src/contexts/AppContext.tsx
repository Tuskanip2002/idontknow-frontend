import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { Alert } from "react-native";
import type { Profile } from "@idk/api";
import { api, auth, sessionStore, setExpiredHandler } from "../services/api";
const AuthContext = createContext<{
  user: Profile | null;
  ready: boolean;
  savedIds: Set<number>;
  updateSaved: (id: number, saved: boolean) => void;
  reload: () => Promise<void>;
  login: (body: object, register?: boolean) => Promise<void>;
  logout: () => Promise<void>;
}>(null!);
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Profile | null>(null);
  const [ready, setReady] = useState(false);
  const [savedIds, setSavedIds] = useState<Set<number>>(new Set());
  const updateSaved = (id: number, saved: boolean) =>
    setSavedIds((previous) => {
      const next = new Set(previous);
      if (saved) next.add(id);
      else next.delete(id);
      return next;
    });
  useEffect(() => {
    const c = new AbortController();
    setSavedIds(new Set());
    if (user)
      api
        .saved(c.signal)
        .then((items) => {
          if (!c.signal.aborted)
            setSavedIds(new Set(items.map((s) => s.titular.id)));
        })
        .catch(() => {});
    return () => c.abort();
  }, [user?.id]);

  async function reload() {
    setUser(await api.profile());
  }
  useEffect(() => {
    setExpiredHandler(() => setUser(null));
    let active = true;
    sessionStore
      .get()
      .then((s) => (s ? auth.refresh() : null))
      .then((s) => (s ? api.profile() : null))
      .then((p) => {
        if (active) setUser(p);
      })
      .catch(() => {})
      .finally(() => {
        if (active) setReady(true);
      });
    return () => {
      active = false;
    };
  }, []);
  async function login(body: object, register = false) {
    await (register ? auth.register(body) : auth.login(body));
    await reload();
  }
  async function logout() {
    try {
      await auth.logout();
    } finally {
      setUser(null);
    }
  }
  return (
    <AuthContext.Provider
      value={{ user, ready, reload, login, logout, savedIds, updateSaved }}
    >
      {children}
    </AuthContext.Provider>
  );
}
export const useAuth = () => useContext(AuthContext);
export const notify = (message: string) => Alert.alert("I Don’t Know", message);
