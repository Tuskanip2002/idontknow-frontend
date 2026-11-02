import { beforeAll, afterAll, describe, it, expect } from "vitest";
import http from "node:http";
import { spawn, type ChildProcess } from "node:child_process";
import { once } from "node:events";
let child: ChildProcess;
let upstream: http.Server;
let base = "";
beforeAll(async () => {
  upstream = http.createServer(async (req, res) => {
    let body = "";
    for await (const chunk of req) body += chunk;
    res.setHeader("Content-Type", "application/json");
    res.setHeader(
      "Set-Cookie",
      "idk_refresh=test; Path=/api/v1/auth; HttpOnly; SameSite=Lax",
    );
    res.end(
      JSON.stringify({
        method: req.method,
        path: req.url,
        body,
        client: req.headers["x-client-type"],
      }),
    );
  });
  upstream.listen(0, "127.0.0.1");
  await once(upstream, "listening");
  const address = upstream.address() as { port: number };
  child = spawn(process.execPath, ["server.mjs"], {
    cwd: process.cwd(),
    env: {
      ...process.env,
      PORT: "0",
      BACKEND_ORIGIN: "http://127.0.0.1:" + address.port,
    },
    stdio: ["ignore", "pipe", "pipe"],
  });
  base = await new Promise<string>((resolve, reject) => {
    const timer = setTimeout(
      () => reject(new Error("El servidor no inició.")),
      15000,
    );
    child.stdout!.on("data", (chunk) => {
      const match = String(chunk).match(/http:\/\/localhost:(\d+)/);
      if (match) {
        clearTimeout(timer);
        resolve("http://127.0.0.1:" + match[1]);
      }
    });
    child.once("error", reject);
    child.once("exit", (code) => {
      if (code) reject(new Error("El servidor terminó con código " + code));
    });
  });
}, 20000);
afterAll(async () => {
  child?.kill();
  if (upstream)
    await new Promise<void>((resolve) => upstream.close(() => resolve()));
});
describe("production web server", () => {
  it("serves the index and client routes with security headers", async () => {
    for (const path of ["/", "/mercados/1", "/archivo"]) {
      const response = await fetch(base + path);
      expect(response.status).toBe(200);
      expect(response.headers.get("content-type")).toContain("text/html");
      expect(response.headers.get("content-security-policy")).toContain(
        "frame-ancestors 'none'",
      );
      expect(await response.text()).toContain('id="root"');
    }
  });
  it("serves bundled assets and rejects missing files or traversal", async () => {
    const html = await (await fetch(base)).text();
    const asset = html.match(/src="([^"]+\.js)"/)![1];
    expect((await fetch(base + asset)).status).toBe(200);
    expect((await fetch(base + "/assets/missing.js")).status).toBe(404);
    expect((await fetch(base + "/%2e%2e%2fpackage.json")).status).toBe(403);
  });
  it("forwards API methods, JSON, client headers and HttpOnly cookies", async () => {
    const response = await fetch(base + "/api/v1/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json", "X-Client-Type": "web" },
      body: '{"test":true}',
    });
    expect(response.status).toBe(200);
    expect(response.headers.get("set-cookie")).toContain("HttpOnly");
    expect(await response.json()).toEqual({
      method: "POST",
      path: "/api/v1/auth/login",
      body: '{"test":true}',
      client: "web",
    });
  });
});
