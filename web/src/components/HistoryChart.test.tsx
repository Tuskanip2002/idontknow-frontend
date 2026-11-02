// @vitest-environment jsdom
import { describe, it, expect, afterEach } from "vitest";
import { render, screen, fireEvent, cleanup } from "@testing-library/react";
import HistoryChart from "./HistoryChart";
afterEach(cleanup);
describe("probability history", () => {
  it("handles empty history without inventing data", () => {
    render(<HistoryChart points={[]} />);
    expect(screen.getByText(/Todavía no hay historial/)).toBeTruthy();
    expect(screen.queryByRole("img")).toBeNull();
  });
  it("exposes actual observations as an accessible table", () => {
    render(
      <HistoryChart
        points={[
          { fecha: "2026-10-31", probabilidad: 0.4 },
          { fecha: "2026-11-01", probabilidad: 0.55 },
        ]}
      />,
    );
    fireEvent.click(
      screen.getByRole("button", { name: "Ver datos del gráfico" }),
    );
    expect(screen.getByRole("table")).toBeTruthy();
    expect(screen.getByRole("cell", { name: /55\s*%/ })).toBeTruthy();
  });
});
