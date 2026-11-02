import { useEffect, useState, useReducer, useCallback } from "react";
import { ApiError } from "@idk/api";
export function useQuery<T>(
  load: (signal: AbortSignal) => Promise<T>,
  key: string,
) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(true);
  const [version, reload] = useReducer((v) => v + 1, 0);
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);
    setData(null);
    load(controller.signal)
      .then((result) => {
        if (!controller.signal.aborted) setData(result);
      })
      .catch((e) => {
        if (!controller.signal.aborted)
          setError(
            e instanceof ApiError
              ? e
              : new ApiError("No pudimos cargar esta información."),
          );
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [key, version]);
  return { data, error, loading, reload: useCallback(() => reload(), []) };
}
export function useDebounce<T>(value: T, delay = 350) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);
  return debounced;
}
