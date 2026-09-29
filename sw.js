// Service worker: guarda la app y TODOS los audios para uso sin internet
const VERSION = "neuroaudio-v3";
const CORE = [
  "./", "./index.html", "./manifest.webmanifest",
  "./audio/alfa10_binaural.flac",
  "./audio/calidad.json",
  "./audio/delta2_binaural_noche.flac",
  "./audio/gamma40_am.flac",
  "./audio/gamma40_binaural.flac",
  "./audio/smr13_binaural.flac",
  "./audio/theta4_binaural_presueno.flac",
  "./audio/gamma40_genus.flac",
  "./audio/ondas_lentas_noche.flac",
  "./audio/theta6_binaural.flac",
  "./audio/tono528.flac",
  "./icons/icon-192.png",
  "./icons/icon-512-maskable.png",
  "./icons/icon-512.png",
];
self.addEventListener("install", e => {
  e.waitUntil(caches.open(VERSION).then(c => c.addAll(CORE)).then(() => self.skipWaiting()));
});
self.addEventListener("activate", e => {
  e.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(k => k !== VERSION).map(k => caches.delete(k)))).then(() => self.clients.claim()));
});
self.addEventListener("fetch", e => {
  const req = e.request;
  if (req.method !== "GET") return;
  // Los audios se sirven completos desde caché (incluye peticiones con Range para <audio>)
  if (req.url.includes("/audio/")) {
    e.respondWith((async () => {
      const cache = await caches.open(VERSION);
      let res = await cache.match(req.url, {ignoreSearch: true});
      if (!res) { res = await fetch(req.url); if (res.ok) cache.put(req.url, res.clone()); }
      const range = req.headers.get("range");
      if (!range || res.status !== 200) return res;
      const buf = await res.arrayBuffer();
      const m = /bytes=(\d*)-(\d*)/.exec(range);
      const start = m[1] ? parseInt(m[1]) : 0, end = m[2] ? parseInt(m[2]) : buf.byteLength - 1;
      return new Response(buf.slice(start, end + 1), {status: 206, headers: {
        "Content-Type": res.headers.get("Content-Type") || "audio/flac",
        "Content-Range": "bytes " + start + "-" + end + "/" + buf.byteLength,
        "Content-Length": String(end - start + 1), "Accept-Ranges": "bytes"}});
    })());
    return;
  }
  e.respondWith(caches.match(req, {ignoreSearch: true}).then(hit => hit || fetch(req).then(res => {
    if (res.ok && (req.url.startsWith(self.location.origin) || req.url.includes("fonts.g"))) {
      const copy = res.clone(); caches.open(VERSION).then(c => c.put(req, copy));
    }
    return res;
  }).catch(() => caches.match("./index.html"))));
});
