import { useEffect, useState, type ReactNode } from "react";
import { Text, ActivityIndicator } from "react-native";
import { SafeAreaProvider, SafeAreaView } from "react-native-safe-area-context";
import { StatusBar } from "expo-status-bar";
import {
  NavigationContainer,
  DefaultTheme,
  createNavigationContainerRef,
  type NavigatorScreenParams,
} from "@react-navigation/native";
import {
  createNativeStackNavigator,
  type NativeStackScreenProps,
} from "@react-navigation/native-stack";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { parseNewsLink } from "@idk/api";
import { Linking } from "react-native";
import { api, webOrigin } from "./services/api";
import { AuthProvider, useAuth } from "./contexts/AppContext";
import { useNavigation } from "./navigation";

import { ErrorBoundary, colors, Page, Heading, Button } from "./components/ui";
import {
  HomeScreen,
  MarketsScreen,
  ArchiveScreen,
  EditionScreen,
  DetailScreen,
  AboutScreen,
} from "./screens/News";
import {
  SavedScreen,
  TopicsScreen,
  ProfileScreen,
  AuthScreen,
} from "./screens/Account";
import { ScanScreen, ReaderScreen } from "./screens/Sensors";
type Tabs = {
  home: undefined;
  markets: undefined;
  archive: undefined;
  saved: undefined;
  profile: undefined;
};
type Routes = {
  tabs: NavigatorScreenParams<Tabs> | undefined;
  edition: { date: string };
  headline: { id: number };
  market: { id: number };
  topics: undefined;
  login: undefined;
  register: undefined;
  scan: undefined;
  reader: undefined;
  about: undefined;
};
const Stack = createNativeStackNavigator<Routes>();
const Tab = createBottomTabNavigator<Tabs>();
const navigationRef = createNavigationContainerRef<Routes>();
function Guard({ children }: { children: ReactNode }) {
  const { user, ready } = useAuth();
  const nav = useNavigation();
  if (!ready)
    return (
      <ActivityIndicator
        style={{ flex: 1 }}
        size="large"
        color={colors.green}
      />
    );
  if (!user)
    return (
      <Page>
        <Heading
          title="Tu propio espacio"
          description="Inicia sesión para guardar titulares, seguir temas y personalizar tu perfil."
        />
        <Button
          title="Iniciar sesión"
          onPress={() => nav.go({ name: "login" })}
        />
        <Button
          title="Crear cuenta"
          outline
          onPress={() => nav.go({ name: "register" })}
        />
      </Page>
    );
  return <>{children}</>;
}
function Saved() {
  return (
    <Guard>
      <SavedScreen />
    </Guard>
  );
}
function Profile() {
  const { user } = useAuth();
  return (
    <Guard>
      <ProfileScreen key={user?.id} />
    </Guard>
  );
}
function Topics() {
  return (
    <Guard>
      <TopicsScreen />
    </Guard>
  );
}
function NewsDetail({
  route,
}: NativeStackScreenProps<Routes, "headline" | "market">) {
  return <DetailScreen kind={route.name} id={route.params.id} />;
}
function Edition({ route }: NativeStackScreenProps<Routes, "edition">) {
  return <EditionScreen date={route.params.date} />;
}
function Login() {
  return <AuthScreen />;
}
function Register() {
  return <AuthScreen register />;
}
function TabsNavigator() {
  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.green,
        tabBarInactiveTintColor: colors.muted,
        tabBarStyle: {
          backgroundColor: colors.surface,
          borderTopColor: colors.line,
        },
        tabBarLabelStyle: { fontSize: 10 },
        tabBarIcon: () => null,
      }}
    >
      <Tab.Screen
        name="home"
        component={HomeScreen}
        options={{ title: "Portada" }}
      />
      <Tab.Screen
        name="markets"
        component={MarketsScreen}
        options={{ title: "Mercados" }}
      />
      <Tab.Screen
        name="archive"
        component={ArchiveScreen}
        options={{ title: "Archivo" }}
      />
      <Tab.Screen
        name="saved"
        component={Saved}
        options={{ title: "Guardados" }}
      />
      <Tab.Screen
        name="profile"
        component={Profile}
        options={{ title: "Mi perfil" }}
      />
    </Tab.Navigator>
  );
}
function Navigator() {
  const [demo, setDemo] = useState(false);
  useEffect(() => {
    const c = new AbortController();
    api
      .status(c.signal)
      .then((s) => setDemo(s.demo))
      .catch(() => {});
    return () => c.abort();
  }, []);
  useEffect(() => {
    const open = (url: string) => {
      const result = parseNewsLink(url, webOrigin);
      if (result && navigationRef.isReady())
        navigationRef.navigate(result.kind, { id: result.id });
    };
    Linking.getInitialURL()
      .then((url) => {
        if (url) open(url);
      })
      .catch(() => {});
    const subscription = Linking.addEventListener("url", ({ url }) =>
      open(url),
    );
    return () => subscription.remove();
  }, []);
  return (
    <SafeAreaView
      style={{ flex: 1, backgroundColor: colors.paper }}
      edges={["left", "right"]}
    >
      <StatusBar style="dark" />
      {demo && (
        <SafeAreaView edges={["top"]} style={{ backgroundColor: colors.mint }}>
          <Text
            style={{
              fontSize: 10,
              textAlign: "center",
              padding: 8,
              color: colors.ink,
            }}
          >
            Demostración · Datos de ejemplo, no probabilidades reales
          </Text>
        </SafeAreaView>
      )}
      <Stack.Navigator
        screenOptions={{
          headerStyle: { backgroundColor: colors.paper },
          headerTintColor: colors.ink,
          headerTitleStyle: { fontFamily: "serif", fontSize: 25 },
          contentStyle: { backgroundColor: colors.paper },
        }}
      >
        <Stack.Screen
          name="tabs"
          component={TabsNavigator}
          options={{ title: "I Don’t Know." }}
        />
        <Stack.Screen
          name="headline"
          component={NewsDetail}
          options={{ title: "La noticia" }}
        />
        <Stack.Screen
          name="market"
          component={NewsDetail}
          options={{ title: "El mercado" }}
        />
        <Stack.Screen
          name="edition"
          component={Edition}
          options={{ title: "Edición del diario" }}
        />
        <Stack.Screen
          name="topics"
          component={Topics}
          options={{ title: "Mis temas" }}
        />
        <Stack.Screen
          name="login"
          component={Login}
          options={{ title: "Iniciar sesión" }}
        />
        <Stack.Screen
          name="register"
          component={Register}
          options={{ title: "Crear cuenta" }}
        />
        <Stack.Screen
          name="scan"
          component={ScanScreen}
          options={{ title: "Escanear QR" }}
        />
        <Stack.Screen
          name="reader"
          component={ReaderScreen}
          options={{ title: "Lectura por inclinación" }}
        />
        <Stack.Screen
          name="about"
          component={AboutScreen}
          options={{ title: "Cómo funciona" }}
        />
      </Stack.Navigator>
    </SafeAreaView>
  );
}
const theme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    background: colors.paper,
    card: colors.surface,
    text: colors.ink,
    primary: colors.green,
    border: colors.line,
  },
};
export default function App() {
  return (
    <SafeAreaProvider>
      <ErrorBoundary>
        <AuthProvider>
          <NavigationContainer ref={navigationRef} theme={theme}>
            <Navigator />
          </NavigationContainer>
        </AuthProvider>
      </ErrorBoundary>
    </SafeAreaProvider>
  );
}
