import { useNavigation } from "../navigation";
import { useState, type ReactNode, Component } from "react";
import {
  View,
  Text,
  Pressable,
  TextInput,
  ActivityIndicator,
  StyleSheet,
  ScrollView,
  RefreshControl,
  type TextInputProps,
} from "react-native";
import { percent, type Headline } from "@idk/api";
import { api } from "../services/api";
import { useAuth, notify } from "../contexts/AppContext";
export const colors = {
  paper: "#f7f6f0",
  surface: "#fffefa",
  ink: "#20312a",
  muted: "#6b756a",
  line: "#dcdfd4",
  green: "#286e50",
  mint: "#e7efdf",
  red: "#a3483e",
};
export function Button({
  title,
  onPress,
  busy = false,
  outline = false,
  disabled = false,
}: {
  title: string;
  onPress: () => void;
  busy?: boolean;
  outline?: boolean;
  disabled?: boolean;
}) {
  return (
    <Pressable
      accessibilityRole="button"
      disabled={busy || disabled}
      onPress={onPress}
      style={({ pressed }) => [
        styles.button,
        outline && styles.outline,
        (busy || disabled) && { opacity: 0.5 },
        pressed && { opacity: 0.75 },
      ]}
    >
      {busy ? (
        <ActivityIndicator color={outline ? colors.ink : colors.surface} />
      ) : (
        <Text style={[styles.buttonText, outline && { color: colors.ink }]}>
          {title}
        </Text>
      )}
    </Pressable>
  );
}
export function Field({
  label,
  error,
  ...props
}: TextInputProps & { label: string; error?: string }) {
  return (
    <View style={{ gap: 7, marginBottom: 17 }}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        {...props}
        accessibilityLabel={label}
        placeholderTextColor={colors.muted}
        style={[
          styles.input,
          error ? { borderColor: colors.red } : null,
          props.style,
        ]}
      />
      {error && (
        <Text style={{ color: colors.red, fontSize: 12 }}>{error}</Text>
      )}
    </View>
  );
}
export function Page({
  children,
  onRefresh,
  refreshing = false,
}: {
  children: ReactNode;
  onRefresh?: () => void;
  refreshing?: boolean;
}) {
  return (
    <ScrollView
      keyboardShouldPersistTaps="handled"
      contentContainerStyle={styles.page}
      refreshControl={
        onRefresh ? (
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor={colors.green}
          />
        ) : undefined
      }
    >
      {children}
    </ScrollView>
  );
}
export function Heading({
  title,
  eyebrow,
  description,
}: {
  title: string;
  eyebrow?: string;
  description?: string;
}) {
  return (
    <View style={{ marginBottom: 24, gap: 9 }}>
      {eyebrow && <Text style={styles.eyebrow}>{eyebrow}</Text>}
      <Text accessibilityRole="header" style={styles.title}>
        {title}
      </Text>
      {description && <Text style={styles.body}>{description}</Text>}
    </View>
  );
}
export function Empty({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <View style={styles.empty}>
      <Text style={styles.subtitle}>{title}</Text>
      <Text style={styles.body}>{description}</Text>
    </View>
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
    <View style={styles.empty}>
      <ActivityIndicator size="large" color={colors.green} />
      <Text style={styles.body}>
        Cargando el diario… La primera conexión puede tardar un momento.
      </Text>
    </View>
  ) : query.error ? (
    <View style={styles.empty}>
      <Text accessibilityRole="alert" style={styles.body}>
        {query.error.message}
      </Text>
      <Button title="Volver a intentar" onPress={query.reload} />
    </View>
  ) : (
    <>{children}</>
  );
}
export function Probability({ value }: { value: number }) {
  return (
    <View style={{ gap: 10, marginVertical: 15 }}>
      <View style={styles.row}>
        <Text style={styles.number}>{percent(value)}</Text>
        <Text style={styles.caption}>de probabilidad</Text>
      </View>
      <View
        style={{ height: 4, backgroundColor: colors.line, borderRadius: 3 }}
      >
        <View
          style={{
            height: 4,
            width: (Math.min(100, Math.max(0, value * 100)) +
              "%") as `${number}%`,
            backgroundColor: colors.green,
            borderRadius: 3,
          }}
        />
      </View>
    </View>
  );
}
export function HeadlineCard({
  headline,
  saved = false,
  onChange,
}: {
  headline: Headline;
  saved?: boolean;
  onChange?: () => void;
}) {
  const nav = useNavigation();
  const { user, savedIds, updateSaved } = useAuth();
  const [busy, setBusy] = useState(false);
  const stored = saved || savedIds.has(headline.id);
  return (
    <View style={styles.card}>
      <View style={styles.row}>
        <Text style={styles.eyebrow}>EN EL RADAR</Text>
        <Text
          style={{
            color: headline.cambioDesdeAyer < 0 ? colors.red : colors.green,
            fontSize: 12,
          }}
        >
          {headline.cambioDesdeAyer >= 0 ? "+" : ""}
          {(headline.cambioDesdeAyer * 100).toFixed(1)} pp
        </Text>
      </View>
      <Pressable
        accessibilityRole="link"
        onPress={() => nav.go({ name: "headline", id: headline.id })}
      >
        <Text style={styles.subtitle}>{headline.textoEspanol}</Text>
      </Pressable>
      <Probability value={headline.probabilidadHoy} />
      <View style={styles.row}>
        <Button
          title="Ver noticia ↗"
          outline
          onPress={() => nav.go({ name: "headline", id: headline.id })}
        />
        <Button
          title={stored ? "Quitar guardado" : "Guardar"}
          busy={busy}
          onPress={async () => {
            if (!user) {
              nav.go({ name: "login" });
              return;
            }
            setBusy(true);
            try {
              if (stored) await api.unsave(headline.id);
              else await api.save(headline.id);
              updateSaved(headline.id, !stored);
              onChange?.();
              notify(stored ? "Se quitó el titular." : "Titular guardado.");
            } catch (e) {
              notify((e as Error).message);
            } finally {
              setBusy(false);
            }
          }}
        />
      </View>
    </View>
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
  render() {
    return this.state.failed ? (
      <View style={styles.page}>
        <Empty
          title="La app tuvo un problema"
          description="Vuelve a abrir el diario para continuar."
        />
        <Button
          title="Intentar de nuevo"
          onPress={() => this.setState({ failed: false })}
        />
      </View>
    ) : (
      this.props.children
    );
  }
}
export const styles = StyleSheet.create({
  page: {
    padding: 22,
    paddingBottom: 45,
    gap: 16,
    flexGrow: 1,
    backgroundColor: colors.paper,
  },
  title: {
    fontFamily: "serif",
    fontSize: 38,
    color: colors.ink,
    lineHeight: 41,
  },
  subtitle: {
    fontFamily: "serif",
    fontSize: 26,
    lineHeight: 31,
    color: colors.ink,
    marginVertical: 14,
  },
  body: { fontSize: 14, color: colors.muted, lineHeight: 23 },
  eyebrow: {
    fontSize: 10,
    letterSpacing: 1.4,
    color: colors.green,
    fontWeight: "700",
  },
  caption: { fontSize: 12, color: colors.muted },
  number: {
    fontSize: 40,
    color: colors.ink,
    fontWeight: "500",
    letterSpacing: -1.5,
  },
  card: {
    padding: 23,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.surface,
    marginBottom: 2,
  },
  row: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: 10,
    flexWrap: "wrap",
  },
  button: {
    minHeight: 44,
    backgroundColor: colors.ink,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 5,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1,
    borderColor: colors.ink,
  },
  outline: { backgroundColor: "transparent", borderColor: colors.line },
  buttonText: { fontSize: 12, fontWeight: "600", color: colors.surface },
  input: {
    minHeight: 46,
    borderWidth: 1,
    borderColor: colors.line,
    borderRadius: 5,
    paddingHorizontal: 13,
    paddingVertical: 11,
    fontSize: 15,
    color: colors.ink,
    backgroundColor: colors.surface,
  },
  label: { fontSize: 13, fontWeight: "600", color: colors.ink },
  empty: {
    padding: 25,
    gap: 18,
    borderWidth: 1,
    borderColor: colors.line,
    borderStyle: "dashed",
    borderRadius: 7,
    alignItems: "stretch",
  },
  chip: {
    borderWidth: 1,
    borderColor: colors.line,
    borderRadius: 20,
    paddingHorizontal: 13,
    paddingVertical: 10,
    marginRight: 7,
    backgroundColor: colors.surface,
  },
  chipActive: { backgroundColor: colors.mint, borderColor: colors.green },
});
