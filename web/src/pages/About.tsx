import { projectTeam } from "@idk/api";
import { Link } from "react-router-dom";
import { PageHeading } from "../components/ui";
export default function About() {
  return (
    <article className="about">
      <PageHeading
        eyebrow="UNA MIRADA DISTINTA"
        title="Entender lo que viene"
        description="Un diario donde cada noticia es una probabilidad."
      />
      <h2>¿De dónde salen las probabilidades?</h2>
      <p>
        Polymarket reúne a participantes que expresan sus expectativas sobre
        eventos futuros. El precio de un resultado «Sí» sirve como una
        estimación de la probabilidad que le asigna ese mercado. Un 70 %
        significa que ese resultado se considera más probable que su
        alternativa, sin garantizar que ocurra.
      </p>
      <h2>¿Qué convierte un mercado en noticia?</h2>
      <p>
        Nuestro backend compara las probabilidades con las del día anterior. Los
        cambios grandes, los cruces del 50 % y las fechas de resolución cercanas
        ayudan a seleccionar los titulares de la portada. Las preguntas se
        presentan en español y el historial permite ver cómo evolucionaron.
      </p>
      <h2>Lee el cambio, además del número</h2>
      <p>
        Si un mercado pasa de 40 % a 55 %, aumentó 15 puntos porcentuales. Las
        flechas de cada titular indican este cambio frente al día anterior. El
        gráfico muestra las observaciones diarias disponibles.
      </p>
      <h2>Tu propio radar</h2>
      <p>
        Puedes guardar titulares y seguir categorías. Si un mercado de un tema
        que sigues cruza el 50 % al alza, el sistema puede enviarte una alerta
        por correo.
      </p>
      <p>
        Este proyecto informa sobre expectativas. No permite apostar ni ofrece
        recomendaciones de inversión.
      </p>
      <Link className="button" to="/mercados">
        Explorar mercados →
      </Link>
      <section className="team-section" aria-labelledby="team-title">
        <p className="eyebrow">El equipo del proyecto</p>
        <h2 id="team-title">Cuatro miradas, una perspectiva</h2>
        <p>
          Desarrollado por estudiantes de UTEC para el curso CS2031 Desarrollo
          Basado en Plataformas, 2026-2.
        </p>
        <ul className="team-grid">
          {projectTeam.map((member) => (
            <li className="team-member" key={member.name}>
              <span aria-hidden="true">{member.initials}</span>
              <div>
                <h3>{member.name}</h3>
                <p>Integrante del equipo</p>
              </div>
            </li>
          ))}
        </ul>
      </section>
    </article>
  );
}
