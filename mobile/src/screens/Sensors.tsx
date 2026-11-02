import { useIsFocused } from "@react-navigation/native";
import { useNavigation } from "../navigation";
import { useRef, useState } from "react";
import { View, Text, Linking, AppState } from "react-native";
import { CameraView, useCameraPermissions } from "expo-camera";
import { parseNewsLink } from "@idk/api";
import { api, webOrigin } from "../services/api";
import { notify } from "../contexts/AppContext";
import { useQuery } from "../hooks/useQuery";
import { useTiltReader } from "../hooks/useTiltReader";
import {
  Page,
  Heading,
  HeadlineCard,
  QueryState,
  Empty,
  Button,
  Field,
  styles,
  colors,
} from "../components/ui";
import { useEffect } from "react";
export function ScanScreen() {
  const focused = useIsFocused();
  const [permission, requestPermission] = useCameraPermissions();
  const [manual, setManual] = useState("");
  const [active, setActive] = useState(AppState.currentState === "active");
  const locked = useRef(false);
  const [error, setError] = useState("");
  const nav = useNavigation();
  useEffect(() => {
    const s = AppState.addEventListener("change", (state) =>
      setActive(state === "active"),
    );
    return () => s.remove();
  }, []);
  function open(value: string) {
    const result = parseNewsLink(value, webOrigin);
    if (!result) {
      setError(
        "Este QR no pertenece al diario. Usa un enlace de titular o mercado de I Don’t Know.",
      );
      return;
    }
    if (locked.current) return;
    locked.current = true;
    nav.go({ name: result.kind, id: result.id });
  }
  return (
    <Page>
      <Heading
        eyebrow="CONTINÚA TU LECTURA"
        title="Escanea una noticia"
        description="Abre el QR desde el detalle de una noticia en la web."
      />{" "}
      {permission?.granted && active && focused ? (
        <View
          style={{
            height: 320,
            borderRadius: 10,
            overflow: "hidden",
            backgroundColor: colors.ink,
          }}
        >
          <CameraView
            style={{ flex: 1 }}
            facing="back"
            barcodeScannerSettings={{ barcodeTypes: ["qr"] }}
            onBarcodeScanned={({ data }) => open(data)}
            onMountError={() =>
              setError(
                "No pudimos iniciar la cámara. Pega el enlace para continuar.",
              )
            }
          />
        </View>
      ) : (
        <View style={styles.card}>
          <Text style={styles.body}>
            La cámara se usa solo para reconocer códigos QR de noticias. También
            puedes pegar el enlace abajo.
          </Text>
          <Button
            title={
              permission?.canAskAgain === false
                ? "Abrir ajustes de permisos"
                : "Permitir cámara"
            }
            onPress={() => {
              if (permission?.canAskAgain === false) {
                Linking.openSettings().catch(() =>
                  notify("Abre los ajustes de la app para permitir la cámara."),
                );
              } else
                requestPermission().catch(() =>
                  setError(
                    "No pudimos solicitar el permiso. Pega el enlace de la noticia.",
                  ),
                );
            }}
          />
        </View>
      )}
      {error && (
        <Text accessibilityRole="alert" style={{ color: colors.red }}>
          {error}
        </Text>
      )}
      <Field
        label="O pega un enlace del diario"
        value={manual}
        onChangeText={setManual}
        autoCapitalize="none"
        keyboardType="url"
        placeholder={webOrigin + "/titulares/1"}
      />
      <Button
        title="Abrir enlace"
        disabled={!manual.trim()}
        onPress={() => open(manual.trim())}
      />
    </Page>
  );
}
export function ReaderScreen() {
  const query = useQuery((s) => api.edition("hoy", s), "reader");
  const [index, setIndex] = useState(0);
  const headlines = query.data?.titulares || [];
  const tilt = useTiltReader((direction) =>
    setIndex((i) => Math.max(0, Math.min(headlines.length - 1, i + direction))),
  );
  return (
    <Page>
      <Heading
        eyebrow="LEE A TU RITMO"
        title="Un titular a la vez"
        description="Inclina suavemente el teléfono para recorrer la portada sin tocar la pantalla."
      />
      <Button
        title={tilt.enabled ? "Desactivar inclinación" : "Activar inclinación"}
        onPress={() => {
          tilt
            .toggle()
            .catch(() =>
              notify("No se pudo activar el sensor. Usa los botones."),
            );
        }}
      />
      <Text style={styles.body}>{tilt.message}</Text>
      <QueryState query={query}>
        {headlines[index] ? (
          <>
            <Text style={styles.caption}>
              Titular {index + 1} de {headlines.length}
            </Text>
            <HeadlineCard
              key={headlines[index].id}
              headline={headlines[index]}
            />
            <View style={styles.row}>
              <Button
                title="← Anterior"
                disabled={index === 0}
                outline
                onPress={() => setIndex((i) => i - 1)}
              />
              <Button
                title="Siguiente →"
                disabled={index >= headlines.length - 1}
                outline
                onPress={() => setIndex((i) => i + 1)}
              />
            </View>
          </>
        ) : (
          <Empty
            title="Sin titulares por ahora"
            description="La portada de hoy aún no está disponible."
          />
        )}
      </QueryState>
    </Page>
  );
}
