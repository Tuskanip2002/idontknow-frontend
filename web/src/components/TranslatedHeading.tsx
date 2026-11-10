import { useEffect, useRef, useState } from "react";
import { api } from "../services/api";
import { Button, PageHeading } from "./ui";

export default function TranslatedHeading({
  marketId,
  title,
  eyebrow,
}: {
  marketId: number;
  title: string;
  eyebrow: string;
}) {
  const [translation, setTranslation] = useState<string>();
  const [sourceOriginal, setSourceOriginal] = useState(title);
  const [original, setOriginal] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const controller = useRef<AbortController | null>(null);
  useEffect(() => () => controller.current?.abort(), []);

  async function translate() {
    if (translation) {
      setOriginal(!original);
      return;
    }
    controller.current = new AbortController();
    setBusy(true);
    setError("");
    try {
      const result = await api.translate(marketId, controller.current.signal);
      setTranslation(result.espanol);
      setSourceOriginal(result.original);
    } catch (e) {
      if (!controller.current.signal.aborted) setError((e as Error).message);
    } finally {
      if (!controller.current.signal.aborted) setBusy(false);
    }
  }

  return (
    <>
      <PageHeading
        eyebrow={eyebrow}
        title={translation ? (original ? sourceOriginal : translation) : title}
      />
      <Button variant="outline" busy={busy} onClick={translate}>
        {translation && !original ? "Ver texto original" : "Leer en español"}
      </Button>
      {translation && !original && (
        <p className="muted">
          Versión en español generada automáticamente. Puedes consultar el texto
          original.
        </p>
      )}
      {error && <p role="alert">{error}</p>}
    </>
  );
}
