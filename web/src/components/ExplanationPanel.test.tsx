// @vitest-environment jsdom
import { describe, it, expect, afterEach, vi, beforeEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import type { Explanation, Profile } from "@idk/api";

const explanation = vi.fn<() => Promise<Explanation>>();
let currentUser: Profile | null = null;

vi.mock("../services/api", () => ({
  api: { explanation: () => explanation(), subscribe: vi.fn() },
}));
vi.mock("../contexts/AppContext", () => ({
  useAuth: () => ({ user: currentUser, reload: vi.fn() }),
  useToast: () => vi.fn(),
}));

import ExplanationPanel from "./ExplanationPanel";

const base: Explanation = {
  titularId: 7,
  disponible: true,
  bloqueado: true,
  cantidadFuentes: 3,
  tiposFuente: ["NOTICIA", "COMENTARIO"],
  resumen: null,
  fuentes: [],
  generadaEn: "2026-11-24T06:01:00",
};
const reader: Profile = {
  id: 1,
  nombre: "Alisson",
  email: "alisson@example.com",
  role: "USER",
  fechaRegistro: "2026-11-01T10:00:00",
  categoriasSeguidas: [],
  plan: "FREE",
  premiumHasta: null,
};
const renderPanel = () =>
  render(
    <MemoryRouter>
      <ExplanationPanel headlineId={7} />
    </MemoryRouter>,
  );

afterEach(cleanup);
beforeEach(() => {
  explanation.mockReset();
  currentUser = null;
});

describe("¿Por qué este número?", () => {
  it("muestra solo el adelanto a quien no es Premium", async () => {
    currentUser = reader;
    explanation.mockResolvedValue(base);
    renderPanel();
    expect(
      await screen.findByText(/3 fuentes \(noticias y comentarios/),
    ).toBeTruthy();
    expect(screen.getByRole("button", { name: /Ver por qué/ })).toBeTruthy();
    expect(screen.queryByRole("list")).toBeNull();
  });

  it("pide iniciar sesión a un visitante", async () => {
    explanation.mockResolvedValue(base);
    renderPanel();
    expect(
      await screen.findByRole("link", {
        name: /Inicia sesión para ver por qué/,
      }),
    ).toBeTruthy();
  });

  it("muestra el resumen y las fuentes citadas a un lector Premium", async () => {
    currentUser = { ...reader, plan: "PREMIUM", premiumHasta: "2026-12-24" };
    explanation.mockResolvedValue({
      ...base,
      bloqueado: false,
      resumen: "El cambio coincide con nuevas encuestas [1].",
      fuentes: [
        {
          tipo: "NOTICIA",
          titulo: "Polls tighten ahead of runoff",
          url: "https://news.example.com/a",
          medio: "news.example.com",
          fecha: "2026-11-23T14:30:00",
        },
        {
          tipo: "COMENTARIO",
          titulo: "The debate changed my mind.",
          url: null,
          medio: "Comentario en Polymarket",
          fecha: null,
        },
      ],
    });
    renderPanel();
    expect(
      await screen.findByText(/coincide con nuevas encuestas/),
    ).toBeTruthy();
    const link = screen.getByRole("link", { name: /Polls tighten/ });
    expect(link.getAttribute("href")).toBe("https://news.example.com/a");
    expect(link.getAttribute("rel")).toContain("noopener");
    expect(screen.getByText(/The debate changed my mind/)).toBeTruthy();
    expect(screen.getByText(/no causas confirmadas/)).toBeTruthy();
  });

  it("avisa cuando la explicación aún no está lista", async () => {
    explanation.mockResolvedValue({
      ...base,
      disponible: false,
      cantidadFuentes: 0,
      tiposFuente: [],
    });
    renderPanel();
    expect(
      await screen.findByText(/Estamos reuniendo las fuentes/),
    ).toBeTruthy();
  });
});
