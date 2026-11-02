import { Link, useNavigate } from "react-router-dom";
import { Bookmark, ArrowUpRight } from "lucide-react";
import { useState } from "react";
import type { Headline } from "@idk/api";
import { api } from "../services/api";
import { useAuth, useToast } from "../contexts/AppContext";
import { Probability, ChangeBadge, Button } from "./ui";
export default function HeadlineCard({
  headline,
  lead = false,
  saved = false,
  onChange,
}: {
  headline: Headline;
  lead?: boolean;
  saved?: boolean;
  onChange?: () => void;
}) {
  const { user, savedIds, updateSaved } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const [busy, setBusy] = useState(false);
  const stored = saved || savedIds.has(headline.id);
  async function toggle() {
    if (!user) {
      navigate("/login", { state: { from: "/titulares/" + headline.id } });
      return;
    }
    setBusy(true);
    try {
      if (stored) await api.unsave(headline.id);
      else await api.save(headline.id);
      updateSaved(headline.id, !stored);
      toast(
        stored
          ? "Titular eliminado de guardados."
          : "Titular guardado para después.",
      );
      onChange?.();
    } catch (e) {
      toast((e as Error).message, true);
    } finally {
      setBusy(false);
    }
  }
  return (
    <article className={"headline-card " + (lead ? "lead" : "")}>
      <div className="card-top">
        <span className="eyebrow">
          {lead ? "La noticia en perspectiva" : "En el radar"}
        </span>
        <ChangeBadge value={headline.cambioDesdeAyer} />
      </div>
      <Link className="headline-link" to={"/titulares/" + headline.id}>
        <h2>{headline.textoEspanol}</h2>
      </Link>
      <Probability value={headline.probabilidadHoy} large={lead} />
      <div className="card-footer">
        <Link to={"/mercados/" + headline.mercadoId}>
          Explorar el mercado <ArrowUpRight size={15} />
        </Link>
        <Button
          variant="icon quiet"
          busy={busy}
          title={stored ? "Quitar de guardados" : "Guardar titular"}
          aria-label={stored ? "Quitar de guardados" : "Guardar titular"}
          aria-pressed={stored}
          onClick={toggle}
        >
          <Bookmark size={18} fill={stored ? "currentColor" : "none"} />
        </Button>
      </div>
    </article>
  );
}
