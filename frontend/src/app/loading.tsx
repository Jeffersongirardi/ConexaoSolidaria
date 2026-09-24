export default function Loading() {
  return (
    <div className="py-10" aria-busy="true" aria-live="polite">
      <div className="mx-auto max-w-6xl space-y-4">
        <div className="h-6 w-48 animate-pulse rounded bg-gray-200" />
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-64 animate-pulse rounded-xl bg-gray-100" />
          ))}
        </div>
      </div>
      <p className="sr-only">Carregando...</p>
    </div>
  );
}
