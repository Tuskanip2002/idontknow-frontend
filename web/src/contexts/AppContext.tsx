import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from "react";
import type { Profile } from "@idk/api";
import { api, auth, sessionStore } from "../services/api";
interface AuthValue {
  user: Profile | null;
  ready: boolean;
  savedIds: Set<number>;
  updateSaved: (id: number, saved: boolean) => void;
  reload: () => Promise<void>;
  signIn: (
    body: { email: string; password: string; nombre?: string },
    register?: boolean,
  ) => Promise<void>;
  signOut: () => Promise<void>;
}
const AuthContext = createContext<AuthValue>(null!);
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

  const reload = useCallback(async () => {
    setUser(await api.profile());
  }, []);
  useEffect(() => {
    let active = true;
    auth
      .refresh()
      .then(() => api.profile())
      .then((p) => {
        if (active) setUser(p);
      })
      .catch(() => {})
      .finally(() => {
        if (active) setReady(true);
      });
    const expired = () => setUser(null);
    window.addEventListener("idk:expired", expired);
    return () => {
      active = false;
      window.removeEventListener("idk:expired", expired);
    };
  }, []);
  const signIn = async (
    body: { email: string; password: string; nombre?: string },
    register = false,
  ) => {
    await (register ? auth.register(body) : auth.login(body));
    try {
      await reload();
    } catch (e) {
      await sessionStore.set(null);
      throw e;
    }
  };
  const signOut = async () => {
    try {
      await auth.logout();
    } finally {
      setUser(null);
    }
  };
  return (
    <AuthContext.Provider
      value={{ user, ready, reload, signIn, signOut, savedIds, updateSaved }}
    >
      {children}
    </AuthContext.Provider>
  );
}
export const useAuth = () => useContext(AuthContext);
type Toast = { id: number; text: string; error: boolean };
const ToastContext = createContext<(text: string, error?: boolean) => void>(
  () => {},
);
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const notify = useCallback((text: string, error = false) => {
    const id = Date.now() + Math.random();
    setToasts((t) => [...t, { id, text, error }]);
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 5000);
  }, []);
  return (
    <ToastContext.Provider value={notify}>
      {children}
      <div className="toasts" aria-live="polite">
        {toasts.map((t) => (
          <div
            key={t.id}
            role={t.error ? "alert" : "status"}
            className={"toast " + (t.error ? "error" : "")}
          >
            {t.text}
            <button
              onClick={() => setToasts((v) => v.filter((x) => x.id !== t.id))}
              aria-label="Cerrar aviso"
            >
              ×
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
export const useToast = () => useContext(ToastContext);
