export function CardSkeleton() {
  return <div className="h-64 animate-pulse rounded-xl bg-gray-100" aria-hidden="true" />;
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
