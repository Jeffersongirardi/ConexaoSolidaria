// Poll only while visible. Focus/read events refresh without waiting 30 seconds.
export function pollVisible(task: () => Promise<void>, interval = 30_000) {
  let stopped = false;
  let busy = false;
  const run = async () => {
    if (stopped || busy || document.visibilityState === "hidden") return;
    busy = true;
    try { await task(); } finally { busy = false; }
  };
  void run();
  const timer = window.setInterval(() => void run(), interval);
  window.addEventListener("focus", run);
  window.addEventListener("notifications-changed", run);
  document.addEventListener("visibilitychange", run);
  return () => {
    stopped = true;
    window.clearInterval(timer);
    window.removeEventListener("focus", run);
    window.removeEventListener("notifications-changed", run);
    document.removeEventListener("visibilitychange", run);
  };
}
