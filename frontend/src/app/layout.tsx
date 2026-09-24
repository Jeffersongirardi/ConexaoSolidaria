import type { Metadata, Viewport } from "next";
import { Geist } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/lib/auth";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import OfflineBanner from "@/components/OfflineBanner";
import SwRegister from "@/components/SwRegister";

const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] });

export const metadata: Metadata = {
  title: {
    default: "Conexões Solidárias",
    template: "%s · Conexões Solidárias",
  },
  description:
    "Plataforma que conecta doadores a instituições de caridade em Curitiba/PR.",
  manifest: "/manifest.webmanifest",
  appleWebApp: { capable: true, statusBarStyle: "default", title: "Conexões" },
};

export const viewport: Viewport = {
  themeColor: "#0d6efd",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="pt-BR" className={`${geistSans.variable} h-full`}>
      <body className="flex min-h-full flex-col bg-white text-gray-900">
        <a href="#conteudo" className="skip-link">
          Pular para o conteúdo
        </a>
        <AuthProvider>
          <OfflineBanner />
          <Header />
          <main id="conteudo" className="mx-auto w-full max-w-6xl flex-1 px-4 py-6">
            {children}
          </main>
          <Footer />
        </AuthProvider>
        <SwRegister />
      </body>
    </html>
  );
}
