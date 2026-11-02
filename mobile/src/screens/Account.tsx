import { useNavigation } from "../navigation";
import { useState } from "react";
import { View, Text } from "react-native";
import { categoryLabel, validPassword } from "@idk/api";
import { api } from "../services/api";
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
} from "../components/ui";
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
