import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  experimental: {
    useOffline: true,
    optimizePackageImports: ["lucide-react"],
  },
  poweredByHeader: false,
  compress: true,
  images: {
    remotePatterns: [{ protocol: "http", hostname: "localhost", port: "8080" }],
  },
  async headers() {
    return [
      {
        source: "/(.*)",
        headers: [
          { key: "X-Content-Type-Options", value: "nosniff" },
          { key: "X-Frame-Options", value: "DENY" },
          { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
          { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=()" },
          { key: "Strict-Transport-Security", value: "max-age=31536000; includeSubDomains" },
        ],
      },
    ];
  },
};

export default nextConfig;
