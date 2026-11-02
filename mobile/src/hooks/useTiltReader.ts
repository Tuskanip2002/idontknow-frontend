import { useIsFocused } from "@react-navigation/native";
import { useEffect, useRef, useState } from "react";
import { Accelerometer } from "expo-sensors";
import { AppState } from "react-native";
export function useTiltReader(onStep: (direction: number) => void) {
  const focused = useIsFocused();
  const [enabled, setEnabled] = useState(false);
  const [message, setMessage] = useState(
    "Activa la lectura por inclinación para pasar titulares.",
  );
  const step = useRef(onStep);
  step.current = onStep;
  async function toggle() {
    if (enabled) {
      setEnabled(false);
      return;
    }
    try {
      if (!(await Accelerometer.isAvailableAsync())) {
        setMessage(
          "El sensor no está disponible. Usa los botones Anterior y Siguiente.",
        );
        return;
      }
      const permission = await Accelerometer.requestPermissionsAsync();
      if (!permission.granted) {
        setMessage(
          "Sin permiso de movimiento. Puedes seguir usando los botones.",
        );
        return;
      }
      setMessage(
        "Inclina a la derecha o izquierda y vuelve al centro para avanzar.",
      );
      setEnabled(true);
    } catch {
      setMessage(
        "No pudimos activar el sensor. Usa los botones para continuar.",
      );
    }
  }
  useEffect(() => {
    if (!enabled || !focused) return;
    let centered = true;
    let last = 0;
    Accelerometer.setUpdateInterval(150);
    const subscription = Accelerometer.addListener(({ x }) => {
      if (Math.abs(x) < 0.2) centered = true;
      if (centered && Math.abs(x) > 0.6 && Date.now() - last > 1000) {
        centered = false;
        last = Date.now();
        step.current(x > 0 ? 1 : -1);
      }
    });
    const state = AppState.addEventListener("change", (s) => {
      if (s !== "active") setEnabled(false);
    });
    return () => {
      subscription.remove();
      state.remove();
    };
  }, [enabled, focused]);
  return { enabled, message, toggle };
}
