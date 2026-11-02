import http from "node:http";
import https from "node:https";
import { readFile, stat } from "node:fs/promises";
import { resolve, extname, sep } from "node:path";
import { fileURLToPath } from "node:url";
const root = resolve(fileURLToPath(new URL("./dist/", import.meta.url)));
const port = Number(process.env.PORT || 3000);
const backend = new URL(process.env.BACKEND_ORIGIN || "http://localhost:8080");
if (!["http:", "https:"].includes(backend.protocol))
  throw new Error("BACKEND_ORIGIN debe usar HTTP o HTTPS.");
const mime = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".svg": "image/svg+xml",
  ".png": "image/png",
  ".ico": "image/x-icon",
};
const server = http.createServer(async (req, res) => {
  res.setHeader("X-Content-Type-Options", "nosniff");
  res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
  res.setHeader(
    "Permissions-Policy",
    "camera=(), microphone=(), geolocation=()",
  );
  res.setHeader(
    "Content-Security-Policy",
    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data:; connect-src 'self'; object-src 'none'; frame-ancestors 'none'; base-uri 'self'",
  );
  if (req.url?.startsWith("/api/")) {
    const upstream = (backend.protocol === "https:" ? https : http).request(
      new URL(req.url, backend),
      { method: req.method, headers: { ...req.headers, host: backend.host } },
      (response) => {
        res.writeHead(response.statusCode || 502, response.headers);
        response.pipe(res);
      },
    );
    upstream.setTimeout(70000, () => upstream.destroy());
    upstream.on("error", () => {
      if (!res.headersSent)
        res.writeHead(502, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ message: "El backend no está disponible." }));
    });
    req.on("aborted", () => upstream.destroy());
    req.pipe(upstream);
    return;
  }
  if (req.method !== "GET" && req.method !== "HEAD") {
    res.writeHead(405);
    res.end();
    return;
  }
  try {
    const pathname = decodeURIComponent(
      new URL(req.url, "http://localhost").pathname,
    );
    let file = resolve(root, "." + pathname);
    if (file !== root && !file.startsWith(root + sep)) {
      res.writeHead(403);
      res.end();
      return;
    }
    try {
      if (!(await stat(file)).isFile()) file = resolve(root, "index.html");
    } catch {
      if (extname(pathname)) {
        res.writeHead(404);
        res.end();
        return;
      }
      file = resolve(root, "index.html");
    }
    const content = await readFile(file);
    res.setHeader(
      "Content-Type",
      mime[extname(file)] || "application/octet-stream",
    );
    res.setHeader(
      "Cache-Control",
      file.endsWith("index.html")
        ? "no-cache"
        : "public, max-age=31536000, immutable",
    );
    res.writeHead(200);
    res.end(req.method === "HEAD" ? undefined : content);
  } catch {
    res.writeHead(500);
    res.end("No pudimos servir la página.");
  }
});
server.listen(port, "0.0.0.0", () =>
  console.log("Diario disponible en http://localhost:" + server.address().port),
);
