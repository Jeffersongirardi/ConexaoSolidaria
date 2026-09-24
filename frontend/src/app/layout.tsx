import type { Metadata, Viewport } from "next";
import { Geist } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/lib/auth";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import MobileBottomNav from "@/components/MobileBottomNav";
import OfflineBanner from "@/components/OfflineBanner";
import SwRegister from "@/components/SwRegister";
import { Toaster } from "sonner";

const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] });

const siteUrl = process.env.NEXT_PUBLIC_SITE_URL ?? "https://conexoessolidarias.org";

export const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
  title: {
    default: "Conexões Solidárias",
    template: "%s · Conexões Solidárias",
  },
  description:
    "Conecte-se a quem transforma doações em impacto real — campanhas de instituições validadas, com acompanhamento até a entrega.",
  manifest: "/manifest.webmanifest",
  appleWebApp: { capable: true, statusBarStyle: "default", title: "Conexões" },
  openGraph: {
    title: "Conexões Solidárias",
    description: "Campanhas de instituições validadas, com acompanhamento até a entrega.",
    url: siteUrl,
    siteName: "Conexões Solidárias",
    locale: "pt_BR",
    type: "website",
  },
  twitter: { card: "summary_large_image" },
  alternates: { canonical: siteUrl },
  robots: { index: true, follow: true },
};

export const viewport: Viewport = {
  themeColor: "#1a4d3e",
  width: "device-width",
  initialScale: 1,
  colorScheme: "light",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="pt-BR" className={`${geistSans.variable} h-full`}>
      <body className="flex min-h-full flex-col bg-[var(--bg)] text-[var(--text)]">
        <a href="#conteudo" className="skip-link">
          Pular para o conteúdo
        </a>
        <AuthProvider>
          <OfflineBanner />
          <Header />
          <main id="conteudo" className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 pb-20 sm:pb-6">
            {children}
          </main>
          <Footer />
          <MobileBottomNav />
        </AuthProvider>
        <Toaster richColors position="top-center" />
        <SwRegister />
      </body>
    </html>
  );
}
