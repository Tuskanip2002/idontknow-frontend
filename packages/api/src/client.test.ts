import { describe, it, expect } from "vitest";
import { createClient, type SessionStore } from "./client";
import { pageQuery, parseNewsLink, validPassword } from "./utils";
import type { Session } from "./types";
import { AxiosError, type AxiosResponse } from "axios";
const session: Session = {
  token: "old",
  refreshToken: "refresh",
  nombre: "Reader",
  email: "reader@example.com",
  role: "USER",
};
function store() {
  let current: Session | null = { ...session };
  return {
    get: async () => current,
    set: async (s: Session | null) => {
      current = s;
    },
  } satisfies SessionStore;
}
describe("navigation and validation", () => {
  it("rejects malicious and unrelated QR links", () => {
    expect(
      parseNewsLink(
        "https://evil.example/titulares/1",
        "https://diario.example",
      ),
    ).toBeNull();
    expect(
      parseNewsLink("javascript:alert(1)", "https://diario.example"),
    ).toBeNull();
    expect(
      parseNewsLink(
        "https://diario.example/titulares/12",
        "https://diario.example",
      ),
    ).toEqual({ kind: "headline", id: 12 });
    expect(
      parseNewsLink("idontknow://mercados/8", "https://diario.example"),
    ).toEqual({ kind: "market", id: 8 });
  });
  it("normalizes invalid pagination and password policy", () => {
    expect(pageQuery(new URLSearchParams("page=-1&size=999"))).toEqual({
      page: 0,
      size: 10,
    });
    expect(validPassword("weak")).toBe(false);
    expect(validPassword("Strong123")).toBe(true);
  });
});
describe("API transport", () => {
  it("attaches the access token and preserves cancellation", async () => {
    const { client } = createClient("http://api", store(), "mobile");
    let header = "";
    client.defaults.adapter = async (config) => {
      header = String(config.headers.Authorization);
      return {
        data: { ok: true },
        status: 200,
        statusText: "OK",
        headers: {},
        config,
      };
    };
    await client.get("/mercados");
    expect(header).toBe("Bearer old");
    const controller = new AbortController();
    controller.abort();
    await expect(
      client.get("/mercados", { signal: controller.signal }),
    ).rejects.toMatchObject({ code: "ERR_CANCELED" });
  });
  it("does not retry mutations and translates conflicts", async () => {
    const { client } = createClient("http://api", store(), "web");
    let calls = 0;
    client.defaults.adapter = async (config) => {
      calls++;
      throw new AxiosError("conflict", "409", config, undefined, {
        status: 409,
        data: {},
        config,
        headers: {},
        statusText: "Conflict",
      } as AxiosResponse);
    };
    await expect(
      client.post("/guardados", { titularId: 1 }),
    ).rejects.toMatchObject({
      status: 409,
      message: "Este registro ya existe.",
    });
    expect(calls).toBe(1);
  });
  it("retries recoverable read errors only", async () => {
    const { client } = createClient("http://api", store(), "web");
    let calls = 0;
    client.defaults.adapter = async (config) => {
      calls++;
      if (calls === 1) throw new AxiosError("network", "ERR_NETWORK", config);
      return { data: [], status: 200, statusText: "OK", headers: {}, config };
    };
    await expect(client.get("/mercados")).resolves.toMatchObject({ data: [] });
    expect(calls).toBe(2);
  });
});
