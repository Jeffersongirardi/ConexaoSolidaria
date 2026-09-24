/* Service worker minimalista (cache + offline limitado).
 * Estratégia: navegações usam network-first com fallback ao cache;
 * imagens de /uploads usam cache-first; demais GET usam stale-while-revalidate.
 */
const VERSION = "cs-v1";
const STATIC_CACHE = `static-${VERSION}`;
const PAGES_CACHE = `pages-${VERSION}`;
const IMAGES_CACHE = `images-${VERSION}`;
const APP_SHELL = ["/", "/campanhas", "/manifest.webmanifest", "/offline"];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(PAGES_CACHE).then((cache) => cache.addAll(APP_SHELL)).then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) =>
        Promise.all(
          keys
            .filter((k) => ![STATIC_CACHE, PAGES_CACHE, IMAGES_CACHE].includes(k))
            .map((k) => caches.delete(k))
        )
      )
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (event) => {
  const { request } = event;
  if (request.method !== "GET") return;
  const url = new URL(request.url);

  // Navegações: tenta rede, cai para cache, por fim página offline
  if (request.mode === "navigate") {
    event.respondWith(
      fetch(request)
        .then((res) => {
          const copy = res.clone();
          caches.open(PAGES_CACHE).then((cache) => cache.put(request, copy));
          return res;
        })
        .catch(() =>
          caches.match(request).then((hit) => hit ?? caches.match("/offline"))
        )
    );
    return;
  }

  // Imagens servidas pelo backend (/uploads): cache-first
  if (url.pathname.startsWith("/uploads")) {
    event.respondWith(
      caches.match(request).then(
        (hit) =>
          hit ??
          fetch(request).then((res) => {
            const copy = res.clone();
            caches.open(IMAGES_CACHE).then((cache) => cache.put(request, copy));
            return res;
          })
      )
    );
    return;
  }

  // Demais GET mesma origem: stale-while-revalidate
  if (url.origin === self.location.origin) {
    event.respondWith(
      caches.match(request).then((hit) => {
        const network = fetch(request).then((res) => {
          const copy = res.clone();
          caches.open(STATIC_CACHE).then((cache) => cache.put(request, copy));
          return res;
        });
        return hit ?? network;
      })
    );
  }
});
