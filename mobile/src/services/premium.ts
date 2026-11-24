import { Alert } from "react-native";
import { api } from "./api";
import { biometricsAvailable, confirmIdentity } from "./biometrics";

async function activate() {
  // la cuenta ahora tiene algo de valor: si el teléfono tiene huella o rostro, se confirma con eso
  if (await biometricsAvailable()) {
    const ok = await confirmIdentity("Confirma tu suscripción Premium");
    if (!ok) return false;
  }
  await api.subscribe();
  return true;
}

/**
 * Explica la suscripción simulada y, si la persona acepta, la activa.
 * Resuelve true si quedó activada; rechaza si la API falla.
 */
export function askToSubscribe(): Promise<boolean> {
  return new Promise((resolve, reject) => {
    Alert.alert(
      "I Don’t Know Premium",
      "El número siempre es gratis. Con Premium ves por qué se movió: un resumen con las noticias y los comentarios del mercado que coinciden con el cambio, cada uno con su fuente.\n\nModo demostración: se activa por 30 días y no se realiza ningún cobro.",
      [
        { text: "Ahora no", style: "cancel", onPress: () => resolve(false) },
        { text: "Activar", onPress: () => activate().then(resolve, reject) },
      ],
      { cancelable: true, onDismiss: () => resolve(false) },
    );
  });
}
