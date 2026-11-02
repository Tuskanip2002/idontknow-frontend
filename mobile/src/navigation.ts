import {
  CommonActions,
  useNavigation as useNativeNavigation,
  useRoute,
} from "@react-navigation/native";
export type ScreenName =
  | "home"
  | "markets"
  | "archive"
  | "edition"
  | "headline"
  | "market"
  | "saved"
  | "topics"
  | "profile"
  | "login"
  | "register"
  | "scan"
  | "reader"
  | "about";
export type Screen = { name: ScreenName; id?: number; date?: string };
export function useNavigation() {
  const navigation = useNativeNavigation();
  const route = useRoute();
  const tab = (name: ScreenName) =>
    navigation.dispatch(
      CommonActions.navigate({ name: "tabs", params: { screen: name } }),
    );
  return {
    screen: { name: route.name, ...route.params } as Screen,
    go: ({ name, id, date }: Screen) =>
      navigation.dispatch(
        CommonActions.navigate({ name, params: { id, date } }),
      ),
    back: () => {
      if (navigation.canGoBack()) navigation.goBack();
      else tab("home");
    },
    tab,
  };
}
