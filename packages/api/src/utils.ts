export const percent = (value: number) =>
  new Intl.NumberFormat("es-PE", {
    style: "percent",
    maximumFractionDigits: 1,
  }).format(value);
export const dateLabel = (value: string) =>
  new Intl.DateTimeFormat("es-PE", {
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(new Date(value.slice(0, 10) + "T12:00:00"));
export const categoryLabel = (value: string) =>
  ({
    POLITICA: "Política",
    ECONOMIA: "Economía",
    DEPORTES: "Deportes",
    TECNOLOGIA: "Tecnología",
  })[value] || value;
export const validPassword = (value: string) =>
  value.length >= 8 &&
  /[a-z]/.test(value) &&
  /[A-Z]/.test(value) &&
  /\d/.test(value);
export function parseNewsLink(
  value: string,
  webOrigin: string,
): { kind: "headline" | "market"; id: number } | null {
  try {
    const url = new URL(value);
    if (
      url.protocol !== "idontknow:" &&
      url.origin !== new URL(webOrigin).origin
    )
      return null;
    const path =
      url.protocol === "idontknow:"
        ? "/" + url.host + url.pathname
        : url.pathname;
    const match = path.match(/^\/(titulares|mercados)\/([1-9]\d*)\/?$/);
    return match
      ? {
          kind: match[1] === "titulares" ? "headline" : "market",
          id: Number(match[2]),
        }
      : null;
  } catch {
    return null;
  }
}
export function pageQuery(params: URLSearchParams) {
  const raw = Number(params.get("page") || 0);
  const size = Number(params.get("size") || 10);
  return {
    page: Number.isSafeInteger(raw) && raw >= 0 ? raw : 0,
    size: [10, 25, 50, 100].includes(size) ? size : 10,
  };
}
