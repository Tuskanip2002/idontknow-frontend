import { useId, useState } from "react";
import { percent, dateLabel, type Snapshot } from "@idk/api";
export default function HistoryChart({ points }: { points: Snapshot[] }) {
  const id = useId();
  const [expanded, setExpanded] = useState(false);
  const data = [...points].sort((a, b) => a.fecha.localeCompare(b.fecha));
  if (!data.length)
    return (
      <p className="muted">
        Todavía no hay historial de este mercado. Aparecerá después de la
        próxima sincronización.
      </p>
    );
  const coords = data.map((p, i) => [
    50 + (data.length === 1 ? 270 : (i / (data.length - 1)) * 540),
    210 - p.probabilidad * 180,
  ]);
  return (
    <section className="chart">
      <div className="section-heading">
        <h2>Cómo cambió la probabilidad</h2>
        <span className="muted">Historial diario</span>
      </div>
      <svg viewBox="0 0 640 255" role="img" aria-labelledby={id}>
        <title id={id}>
          Probabilidad: de {percent(data[0].probabilidad)} a{" "}
          {percent(data.at(-1)!.probabilidad)}, {data.length} observaciones.
        </title>
        {[0, 0.25, 0.5, 0.75, 1].map((n) => (
          <g key={n}>
            <line
              x1="50"
              x2="590"
              y1={210 - n * 180}
              y2={210 - n * 180}
              stroke="currentColor"
              opacity=".12"
              strokeDasharray={n === 0.5 ? "5 5" : undefined}
            />
            <text x="4" y={214 - n * 180}>
              {percent(n)}
            </text>
          </g>
        ))}
        <polyline
          fill="none"
          stroke="var(--green)"
          strokeWidth="3"
          points={coords.map((c) => c.join(",")).join(" ")}
        />
        {coords.map((c, i) => (
          <circle
            key={data[i].fecha}
            cx={c[0]}
            cy={c[1]}
            r="4"
            fill="var(--green)"
          >
            <title>
              {dateLabel(data[i].fecha)}: {percent(data[i].probabilidad)}
            </title>
          </circle>
        ))}
        <text x="50" y="245">
          {dateLabel(data[0].fecha)}
        </text>
        <text x="590" y="245" textAnchor="end">
          {dateLabel(data.at(-1)!.fecha)}
        </text>
      </svg>
      <button
        className="text-button"
        aria-expanded={expanded}
        onClick={() => setExpanded((v) => !v)}
      >
        {expanded ? "Ocultar" : "Ver"} datos del gráfico
      </button>
      {expanded && (
        <table>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Probabilidad</th>
            </tr>
          </thead>
          <tbody>
            {data.map((p) => (
              <tr key={p.fecha}>
                <td>{dateLabel(p.fecha)}</td>
                <td>{percent(p.probabilidad)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
