import {
  Component,
  useEffect,
  useRef,
  useId,
  type ReactNode,
  type InputHTMLAttributes,
  type ErrorInfo,
} from "react";
import { Link } from "react-router-dom";
import {
  ArrowRight,
  RefreshCw,
  Inbox,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import { percent, type Page } from "@idk/api";
export function Button({
  children,
  busy = false,
  variant = "",
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & {
  busy?: boolean;
  variant?: string;
}) {
  return (
    <button
      {...props}
      disabled={busy || props.disabled}
      className={"button " + variant + " " + (props.className || "")}
    >
      {busy ? <RefreshCw size={16} className="spin" /> : null}
      {children}
    </button>
  );
}
export function Field({
  label,
  error,
  hint,
  ...props
}: InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
  hint?: string;
}) {
  const auto = useId();
  const id = props.id || auto;
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <input
        {...props}
        id={id}
        aria-invalid={!!error}
        aria-describedby={error || hint ? id + "-hint" : undefined}
      />
      {(error || hint) && (
        <small id={id + "-hint"} className={error ? "field-error" : ""}>
          {error || hint}
        </small>
      )}
    </div>
  );
}
export function PageHeading({
  eyebrow,
  title,
  description,
  children,
  level = 1,
}: {
  level?: 1 | 2;
  eyebrow?: string;
  title: string;
  description?: string;
  children?: ReactNode;
}) {
  const Heading = level === 1 ? "h1" : "h2";
  return (
    <header className="page-heading">
      <div>
        {eyebrow && <p className="eyebrow">{eyebrow}</p>}
        <Heading>{title}</Heading>
        {description && <p className="muted">{description}</p>}
      </div>
      {children}
    </header>
  );
}
export function Probability({
  value,
  large = false,
}: {
  value: number;
  large?: boolean;
}) {
  return (
    <div className={"probability " + (large ? "large" : "")}>
      <strong>{percent(value)}</strong>
      <span>de probabilidad</span>
      <div className="probability-track">
        <i style={{ width: Math.min(100, Math.max(0, value * 100)) + "%" }} />
      </div>
    </div>
  );
}
export function ChangeBadge({ value }: { value: number }) {
  return (
    <span className={"change " + (value < 0 ? "down" : "")}>
      {value > 0 ? "↗" : value < 0 ? "↘" : "→"} {value > 0 ? "+" : ""}
      {(value * 100).toLocaleString("es-PE", { maximumFractionDigits: 1 })} pp
      <span className="sr-only"> frente a ayer</span>
    </span>
  );
}
export function EmptyState({
  title,
  description,
  action,
  to,
}: {
  title: string;
  description: string;
  action?: string;
  to?: string;
}) {
  return (
    <div className="empty">
      <Inbox size={32} />
      <h2>{title}</h2>
      <p>{description}</p>
      {action && to && (
        <Link className="button" to={to}>
          {action}
          <ArrowRight size={16} />
        </Link>
      )}
    </div>
  );
}
export function LoadingState() {
  return (
    <div aria-busy="true" aria-label="Cargando contenido" className="skeletons">
      {[1, 2, 3].map((i) => (
        <div className="skeleton" key={i} />
      ))}
      <span className="sr-only">
        Cargando, el servidor puede tardar un momento en iniciarse.
      </span>
    </div>
  );
}
export function ErrorState({
  message,
  onRetry,
}: {
  message: string;
  onRetry: () => void;
}) {
  return (
    <div className="empty" role="alert">
      <h2>No pudimos cargar esta sección</h2>
      <p>{message}</p>
      <Button onClick={onRetry}>
        <RefreshCw size={16} />
        Volver a intentar
      </Button>
    </div>
  );
}
export function QueryState({
  query,
  children,
}: {
  query: { loading: boolean; error: Error | null; reload: () => void };
  children: ReactNode;
}) {
  return query.loading ? (
    <LoadingState />
  ) : query.error ? (
    <ErrorState message={query.error.message} onRetry={query.reload} />
  ) : (
    <>{children}</>
  );
}
export function Pagination({
  data,
  onChange,
}: {
  data: Page<unknown>;
  onChange: (page: number, size: number) => void;
}) {
  const start = data.totalElements === 0 ? 0 : data.number * data.size + 1;
  const end = Math.min((data.number + 1) * data.size, data.totalElements);
  const pages = Array.from(
    { length: Math.min(5, data.totalPages) },
    (_, i) => Math.max(0, Math.min(data.number - 2, data.totalPages - 5)) + i,
  );
  return (
    <nav className="pagination" aria-label="Paginación">
      <span>
        Mostrando {start}–{end} de {data.totalElements} resultados
      </span>
      <label>
        Por página{" "}
        <select
          value={data.size}
          onChange={(e) => onChange(0, Number(e.target.value))}
        >
          {[10, 25, 50, 100].map((n) => (
            <option key={n}>{n}</option>
          ))}
        </select>
      </label>
      <div className="pager">
        <Button
          aria-label="Página anterior"
          disabled={data.number === 0}
          onClick={() => onChange(data.number - 1, data.size)}
        >
          <ChevronLeft size={16} />
        </Button>
        {pages.map((n) => (
          <Button
            key={n}
            aria-current={n === data.number ? "page" : undefined}
            variant={n === data.number ? "selected" : "quiet"}
            onClick={() => onChange(n, data.size)}
          >
            {n + 1}
          </Button>
        ))}
        <Button
          aria-label="Página siguiente"
          disabled={data.number + 1 >= data.totalPages}
          onClick={() => onChange(data.number + 1, data.size)}
        >
          <ChevronRight size={16} />
        </Button>
      </div>
    </nav>
  );
}
export function Dialog({
  title,
  children,
  onClose,
}: {
  title: string;
  children: ReactNode;
  onClose: () => void;
}) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const dialog = ref.current;
    const active = document.activeElement as HTMLElement;
    dialog?.showModal();
    return () => {
      dialog?.close();
      active?.focus();
    };
  }, []);
  return (
    <dialog
      ref={ref}
      onCancel={(e) => {
        e.preventDefault();
        onClose();
      }}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="dialog-heading">
        <h2>{title}</h2>
        <Button variant="quiet" aria-label="Cerrar diálogo" onClick={onClose}>
          ×
        </Button>
      </div>
      {children}
    </dialog>
  );
}
export class ErrorBoundary extends Component<
  { children: ReactNode },
  { failed: boolean }
> {
  state = { failed: false };
  static getDerivedStateFromError() {
    return { failed: true };
  }
  componentDidCatch(_error: Error, _info: ErrorInfo) {}
  render() {
    return this.state.failed ? (
      <main>
        <EmptyState
          title="Esta página tuvo un problema"
          description="Vuelve a abrir el diario para continuar."
        />
        <a className="button" href="/">
          Volver a la portada
        </a>
      </main>
    ) : (
      this.props.children
    );
  }
}
