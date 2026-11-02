import { Link } from "react-router-dom";
import {
  Landmark,
  BarChart3,
  Trophy,
  Cpu,
  Hash,
  ArrowUpRight,
} from "lucide-react";
import { categoryLabel } from "@idk/api";
import { api } from "../services/api";
import { useQuery } from "../hooks/useQuery";
const icons = {
  POLITICA: Landmark,
  ECONOMIA: BarChart3,
  DEPORTES: Trophy,
  TECNOLOGIA: Cpu,
};
export default function TopicLinks() {
  const query = useQuery(api.categories, "home-topics");
  if (!query.data?.length) return null;
  return (
    <nav className="topic-links" aria-label="Explorar por tema">
      <span>En tu radar</span>
      {query.data.map((c) => {
        const Icon = icons[c.nombre as keyof typeof icons] || Hash;
        return (
          <Link
            key={c.id}
            to={"/mercados?categoria=" + encodeURIComponent(c.nombre)}
          >
            <Icon size={15} />
            {categoryLabel(c.nombre)}
            <ArrowUpRight size={13} />
          </Link>
        );
      })}
    </nav>
  );
}
