export interface Category {
  id: number;
  nombre: string;
}
export interface Snapshot {
  fecha: string;
  probabilidad: number;
}
export interface Market {
  id: number;
  preguntaOriginal: string;
  probabilidadActual: number;
  resuelto: boolean;
  categorias: string[];
}
export interface MarketDetail extends Market {
  polymarketId?: string;
  fechaResolucionEstimada: string | null;
  historial: Snapshot[];
}
export interface Headline {
  id: number;
  textoEspanol: string;
  probabilidadHoy: number;
  cambioDesdeAyer: number;
  mercadoId: number;
}
export interface HeadlineDetail {
  id: number;
  mercadoId: number;
  textoEspanol: string;
  probabilidadHoy: number;
  cambioDesdeAyer: number;
  fechaResolucionEstimada: string | null;
  historial: Snapshot[];
}
export interface Edition {
  id: number;
  fecha: string;
  titulares: Headline[];
}
export interface EditionSummary {
  id: number;
  fecha: string;
  cantidadTitulares: number;
}
export interface SavedHeadline {
  id: number;
  titular: Headline;
  fechaGuardado: string;
}
export interface Profile {
  id: number;
  nombre: string;
  email: string;
  role: "USER" | "ADMIN";
  fechaRegistro: string;
  categoriasSeguidas: string[];
  /** Opcional para seguir funcionando con un backend anterior a Premium. */
  plan?: "FREE" | "PREMIUM";
  premiumHasta?: string | null;
}
export interface ExplanationSource {
  tipo: "NOTICIA" | "COMENTARIO";
  titulo: string;
  url: string | null;
  medio: string | null;
  fecha: string | null;
}
/** "¿Por qué este número?". Con bloqueado = true, resumen es null y fuentes viene vacía. */
export interface Explanation {
  titularId: number;
  disponible: boolean;
  bloqueado: boolean;
  cantidadFuentes: number;
  tiposFuente: ("NOTICIA" | "COMENTARIO")[];
  resumen: string | null;
  fuentes: ExplanationSource[];
  generadaEn: string | null;
}
export interface Session {
  token: string;
  refreshToken?: string;
  nombre: string;
  email: string;
  role: "USER" | "ADMIN";
}
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
export interface PageQuery {
  page?: number;
  size?: number;
  sort?: string;
  categoria?: string;
  q?: string;
}
export interface Credentials {
  email: string;
  password: string;
}
export interface Registration extends Credentials {
  nombre: string;
}
