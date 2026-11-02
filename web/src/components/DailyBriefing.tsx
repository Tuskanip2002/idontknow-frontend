import { Link } from "react-router-dom";
import { Activity, ArrowUpRight } from "lucide-react";
import { percent, type Headline } from "@idk/api";
import { ChangeBadge } from "./ui";
export default function DailyBriefing({
  headlines,
  loading,
}: {
  headlines: Headline[];
  loading: boolean;
}) {
  const highlighted = headlines.reduce<Headline | null>(
    (current, h) =>
      !current ||
      Math.abs(h.cambioDesdeAyer) > Math.abs(current.cambioDesdeAyer)
        ? h
        : current,
    null,
  );
  return (
    <aside className="daily-brief" aria-label="Resumen de la edición">
      <div className="brief-label">
        <Activity size={16} />
        <span>El cambio destacado</span>
      </div>
      {highlighted ? (
        <>
          <Link to={"/titulares/" + highlighted.id} className="brief-headline">
            {highlighted.textoEspanol}
            <ArrowUpRight size={19} />
          </Link>
          <div className="brief-numbers">
            <div>
              <strong>{percent(highlighted.probabilidadHoy)}</strong>
              <span>probabilidad actual</span>
            </div>
            <ChangeBadge value={highlighted.cambioDesdeAyer} />
          </div>
          <p>Mayor variación entre los titulares de esta edición.</p>
        </>
      ) : (
        <>
          <h2>
            {loading
              ? "Preparando tu perspectiva…"
              : "Cada cambio cuenta una historia"}
          </h2>
          <p>
            Compara la probabilidad de hoy con la de ayer para entender qué se
            está moviendo.
          </p>
        </>
      )}
    </aside>
  );
}
