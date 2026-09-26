export function CardSkeleton() {
  return <div className="h-64 animate-pulse rounded-xl bg-gray-100" aria-hidden="true" />;
}
export function CampaignCardSkeleton() {
  return (
    <article className="animate-pulse flex flex-col overflow-hidden rounded-xl border bg-gray-100 shadow-sm">
      <div className="h-44 w-full rounded-t-xl bg-gray-200" />
      <div className="flex flex-1 flex-col gap-2 p-4">
        <div className="h-4 w-3/4 bg-gray-200 rounded" />
        <div className="h-3 w-1/2 bg-gray-200 rounded mt-2" />
        <div className="h-3 w-3/4 bg-gray-200 rounded mt-1" />
        <div className="h-2 w-full bg-gray-200 rounded mt-3" />
        <div className="h-8 w-1/3 bg-gray-200 rounded mt-auto" />
      </div>
    </article>
  );
}
export function ListSkeleton({ rows = 3 }: { rows?: number }) {
  return (
    <div className="space-y-2" aria-hidden="true">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="h-20 animate-pulse rounded-xl bg-gray-100" />
      ))}
    </div>
  );
}
