import { View, Text, Dimensions } from "react-native";
import Svg, { Line, Polyline, Circle } from "react-native-svg";
import { percent, dateLabel, type Snapshot } from "@idk/api";
import { styles, colors } from "./ui";
export default function HistoryChart({ points }: { points: Snapshot[] }) {
  const data = [...points].sort((a, b) => a.fecha.localeCompare(b.fecha));
  if (!data.length)
    return (
      <Text style={styles.body}>
        El historial aparecerá después de la próxima sincronización.
      </Text>
    );
  const width = Math.min(Dimensions.get("window").width - 90, 500);
  const coords = data.map((p, i) => [
    20 +
      (data.length === 1
        ? (width - 40) / 2
        : (i / (data.length - 1)) * (width - 40)),
    165 - p.probabilidad * 145,
  ]);
  return (
    <View style={styles.card}>
      <Text style={styles.subtitle}>Cómo cambió</Text>
      <Svg
        width="100%"
        height={190}
        viewBox={"0 0 " + width + " 190"}
        accessibilityLabel={
          "Historial: de " +
          percent(data[0].probabilidad) +
          " a " +
          percent(data[data.length - 1].probabilidad)
        }
      >
        {[0, 0.5, 1].map((n) => (
          <Line
            key={n}
            x1={20}
            x2={width - 20}
            y1={165 - n * 145}
            y2={165 - n * 145}
            stroke={colors.line}
          />
        ))}
        <Polyline
          points={coords.map((p) => p.join(",")).join(" ")}
          fill="none"
          stroke={colors.green}
          strokeWidth={3}
        />
        {coords.map((p, i) => (
          <Circle key={i} cx={p[0]} cy={p[1]} r={3} fill={colors.green} />
        ))}
      </Svg>
      <Text style={styles.caption}>
        {dateLabel(data[0].fecha)} — {dateLabel(data[data.length - 1].fecha)}
      </Text>
      {data.map((p) => (
        <View key={p.fecha} style={[styles.row, { marginTop: 10 }]}>
          <Text style={styles.caption}>{dateLabel(p.fecha)}</Text>
          <Text style={styles.caption}>{percent(p.probabilidad)}</Text>
        </View>
      ))}
    </View>
  );
}
