import { useNavigation } from "../navigation";
import { useEffect, useState } from "react";
import { View, Text, Switch } from "react-native";
import { categoryLabel, validPassword, isPremium, dateLabel } from "@idk/api";
import { api } from "../services/api";
import {
  biometricsAvailable,
  biometricsEnabled,
  confirmIdentity,
  setBiometricsEnabled,
} from "../services/biometrics";
import { askToSubscribe } from "../services/premium";
import { useAuth, notify } from "../contexts/AppContext";
import { useQuery } from "../hooks/useQuery";
import {
  Page,
  Heading,
  HeadlineCard,
  QueryState,
  Empty,
  Field,
  Button,
  styles,
  colors,
} from "../components/ui";
function PlanCard() {
  const { user, reload } = useAuth();
  const [busy, setBusy] = useState(false);
  const premium = isPremium(user);
  return (
    <View style={styles.card}>
      <Text style={styles.eyebrow}>TU PLAN</Text>
      <Text style={[styles.body, { marginVertical: 10 }]}>
        {premium
          ? "Premium" +
            (user?.premiumHasta
              ? " hasta el " + dateLabel(user.premiumHasta)
              : "")
          : "Gratuito: ves el número de cada noticia."}
      </Text>
      <Button
        title={premium ? "Cancelar Premium" : "Conocer Premium"}
        outline={premium}
        busy={busy}
        onPress={async () => {
          setBusy(true);
          try {
            if (premium) {
              await api.cancelSubscription();
              await reload();
              notify("Volviste al plan gratuito.");
            } else if (await askToSubscribe()) {
              await reload();
              notify("Premium activado por 30 días.");
            }
          } catch (e) {
            notify((e as Error).message);
          } finally {
            setBusy(false);
          }
        }}
      />
    </View>
  );
}
function BiometricsCard() {
  const [available, setAvailable] = useState(false);
  const [enabled, setEnabled] = useState(false);
  useEffect(() => {
    let active = true;
    Promise.all([biometricsAvailable(), biometricsEnabled()]).then(([a, e]) => {
      if (active) {
        setAvailable(a);
        setEnabled(e);
      }
    });
    return () => {
      active = false;
    };
  }, []);
  // sin sensor o sin huellas registradas no se ofrece: se sigue entrando con contraseña
  if (!available) return null;
  return (
    <View style={[styles.card, styles.row]}>
      <View style={{ flex: 1 }}>
        <Text style={styles.label}>Entrar con huella o rostro</Text>
        <Text style={styles.caption}>
          Al abrir la app se pide tu huella antes de usar la sesión guardada.
        </Text>
      </View>
      <Switch
        value={enabled}
        trackColor={{ true: colors.green }}
        accessibilityLabel="Entrar con huella o rostro"
        onValueChange={async (value) => {
          try {
            // se confirma una vez al activarlo, para comprobar que el sensor funciona
            if (
              value &&
              !(await confirmIdentity("Activa el acceso con huella"))
            )
              return;
            await setBiometricsEnabled(value);
            setEnabled(value);
          } catch {
            notify("No se pudo cambiar el acceso con huella.");
          }
        }}
      />
    </View>
  );
}
export function SavedScreen() {
  const query = useQuery(api.saved, "saved");
  return (
    <Page onRefresh={query.reload} refreshing={query.loading}>
      <Heading eyebrow="TU BIBLIOTECA" title="Para leer con calma" />
      <QueryState query={query}>
        {query.data?.length ? (
          query.data.map((s) => (
            <HeadlineCard
              key={s.id}
              headline={s.titular}
              saved
              onChange={query.reload}
            />
          ))
        ) : (
          <Empty
            title="Tus próximas lecturas"
            description="Guarda un titular desde la portada para encontrarlo aquí."
          />
        )}
      </QueryState>
    </Page>
  );
}
export function TopicsScreen() {
  const query = useQuery(api.categories, "topics");
  const { user, reload } = useAuth();
  const [busy, setBusy] = useState<number | null>(null);
  return (
    <Page>
      <Heading
        eyebrow="PERSONALIZA TU RADAR"
        title="Mis temas"
        description="Recibe un correo cuando una categoría que sigues cruce el 50 %."
      />
      <QueryState query={query}>
        {query.data?.map((c) => {
          const followed = user?.categoriasSeguidas.includes(c.nombre);
          return (
            <View style={styles.card} key={c.id}>
              <Text style={styles.subtitle}>{categoryLabel(c.nombre)}</Text>
              <Button
                outline={followed}
                busy={busy === c.id}
                disabled={busy !== null}
                title={followed ? "Dejar de seguir" : "+ Seguir tema"}
                onPress={async () => {
                  setBusy(c.id);
                  try {
                    if (followed) await api.unfollow(c.id);
                    else await api.follow(c.id);
                    await reload();
                    notify(
                      followed
                        ? "Dejaste de seguir el tema."
                        : "Tema añadido a tu radar.",
                    );
                  } catch (e) {
                    notify((e as Error).message);
                  } finally {
                    setBusy(null);
                  }
                }}
              />
            </View>
          );
        })}
        {query.data?.length === 0 && (
          <Empty
            title="Sin temas disponibles"
            description="Las categorías aparecerán aquí cuando estén configuradas."
          />
        )}
      </QueryState>
    </Page>
  );
}
export function ProfileScreen() {
  const { user, reload, logout } = useAuth();
  const nav = useNavigation();
  const [name, setName] = useState(user?.nombre || "");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  return (
    <Page>
      <Heading
        eyebrow="TU ESPACIO EN EL DIARIO"
        title="Mi perfil"
        description={user?.email}
      />
      <View style={styles.card}>
        <Field
          label="Nombre"
          value={name}
          onChangeText={setName}
          maxLength={100}
        />
        <Field
          label="Nueva contraseña (opcional)"
          value={password}
          secureTextEntry
          onChangeText={setPassword}
          autoCapitalize="none"
          error={
            password && !validPassword(password)
              ? "Usa 8 caracteres, mayúscula, minúscula y número."
              : undefined
          }
        />
        <Button
          title="Guardar cambios"
          busy={busy}
          disabled={!name.trim() || (!!password && !validPassword(password))}
          onPress={async () => {
            setBusy(true);
            try {
              await api.updateProfile({
                nombre: name.trim(),
                ...(password ? { password } : {}),
              });
              await reload();
              setPassword("");
              notify("Perfil actualizado.");
            } catch (e) {
              notify((e as Error).message);
            } finally {
              setBusy(false);
            }
          }}
        />
      </View>
      <PlanCard />
      <BiometricsCard />
      <Button
        title="Mis temas →"
        outline
        onPress={() => nav.go({ name: "topics" })}
      />
      <Button
        title="Mis guardados →"
        outline
        onPress={() => nav.go({ name: "saved" })}
      />
      <Button
        title="Cerrar sesión"
        outline
        onPress={async () => {
          try {
            await logout();
            nav.tab("home");
          } catch (e) {
            notify((e as Error).message);
            nav.tab("home");
          }
        }}
      />
    </Page>
  );
}
export function AuthScreen({ register = false }: { register?: boolean }) {
  const { login } = useAuth();
  const nav = useNavigation();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  const valid =
    emailValid &&
    password.length > 0 &&
    (!register ||
      (name.trim() && validPassword(password) && confirm === password));
  return (
    <Page>
      <Heading
        eyebrow="BIENVENIDO AL DIARIO"
        title={register ? "Crea tu cuenta" : "Qué bueno verte de nuevo"}
        description="Guarda noticias y sigue los temas que te importan."
      />
      {register && (
        <Field
          label="Nombre"
          value={name}
          onChangeText={setName}
          maxLength={100}
          autoComplete="name"
        />
      )}
      <Field
        label="Correo electrónico"
        value={email}
        onChangeText={setEmail}
        keyboardType="email-address"
        autoCapitalize="none"
        autoComplete="email"
        error={email && !emailValid ? "Escribe un correo válido." : undefined}
      />
      <Field
        label="Contraseña"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
        autoCapitalize="none"
        autoComplete={register ? "new-password" : "current-password"}
        error={
          register && password && !validPassword(password)
            ? "Al menos 8 caracteres, mayúscula, minúscula y número."
            : undefined
        }
      />
      {register && (
        <Field
          label="Confirmar contraseña"
          value={confirm}
          onChangeText={setConfirm}
          secureTextEntry
          autoCapitalize="none"
          error={
            confirm && confirm !== password
              ? "Las contraseñas no coinciden."
              : undefined
          }
        />
      )}
      {error && (
        <Text accessibilityRole="alert" style={styles.body}>
          {error}
        </Text>
      )}
      <Button
        title={register ? "Crear cuenta" : "Iniciar sesión"}
        busy={busy}
        disabled={!valid}
        onPress={async () => {
          setBusy(true);
          setError("");
          try {
            await login(
              {
                email: email.trim(),
                password,
                ...(register ? { nombre: name.trim() } : {}),
              },
              register,
            );
            nav.tab("profile");
          } catch (e) {
            setError((e as Error).message);
          } finally {
            setBusy(false);
          }
        }}
      />
      <Button
        outline
        title={register ? "Ya tengo cuenta" : "Crear una cuenta"}
        onPress={() => nav.go({ name: register ? "login" : "register" })}
      />
    </Page>
  );
}
