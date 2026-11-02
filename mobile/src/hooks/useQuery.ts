import { useIsFocused } from "@react-navigation/native";
import { useEffect, useState, useReducer } from "react";
export function useQuery<T>(
  load: (signal: AbortSignal) => Promise<T>,
  key: string,
) {
  const focused = useIsFocused();
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [version, reload] = useReducer((v) => v + 1, 0);
  useEffect(() => {
    if (!focused) return;
    const c = new AbortController();
    setLoading(true);
    setError(null);
    setData(null);
    load(c.signal)
      .then((v) => {
        if (!c.signal.aborted) setData(v);
      })
      .catch((e) => {
        if (!c.signal.aborted) setError(e);
      })
      .finally(() => {
        if (!c.signal.aborted) setLoading(false);
      });
    return () => c.abort();
  }, [key, version, focused]);
  return { data, loading, error, reload };
}
