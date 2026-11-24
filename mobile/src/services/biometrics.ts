import * as LocalAuthentication from "expo-local-authentication";
import * as SecureStore from "expo-secure-store";

// preferencia del usuario: "1" si eligió entrar con huella o rostro
const KEY = "idk.biometria";

/** true si el teléfono tiene sensor biométrico y la persona registró al menos una huella o rostro. */
export async function biometricsAvailable() {
  try {
    const [hardware, enrolled] = await Promise.all([
      LocalAuthentication.hasHardwareAsync(),
      LocalAuthentication.isEnrolledAsync(),
    ]);
    return hardware && enrolled;
  } catch {
    return false;
  }
}

export async function biometricsEnabled() {
  try {
    return (await SecureStore.getItemAsync(KEY)) === "1";
  } catch {
    return false;
  }
}

export async function setBiometricsEnabled(value: boolean) {
  if (value) await SecureStore.setItemAsync(KEY, "1");
  else await SecureStore.deleteItemAsync(KEY);
}

/**
 * Pide la huella o el rostro. Si el sensor falla, Android ofrece el PIN o patrón del
 * teléfono como alternativa. Devuelve false si la persona cancela.
 */
export async function confirmIdentity(promptMessage: string) {
  try {
    const result = await LocalAuthentication.authenticateAsync({
      promptMessage,
      cancelLabel: "Cancelar",
    });
    return result.success;
  } catch {
    return false;
  }
}
