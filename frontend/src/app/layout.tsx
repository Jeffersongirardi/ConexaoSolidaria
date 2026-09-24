import type { Metadata, Viewport } from "next";
import { Geist } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/lib/auth";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import MobileBottomNav from "@/components/MobileBottomNav";
import OfflineBanner from "@/components/OfflineBanner";
import SwRegister from "@/components/SwRegister";

const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] });

export const metadata: Metadata = {
  title: {
    default: "Conexões Solidárias",
    template: "%s · Conexões Solidárias",
  },
  description:
    "Conecte-se a quem transforma doações em impacto real — campanhas de instituições validadas, com acompanhamento até a entrega.",
  manifest: "/manifest.webmanifest",
  appleWebApp: { capable: true, statusBarStyle: "default", title: "Conexões" },
};

export const viewport: Viewport = {
  themeColor: "#1a4d3e",
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
        <SwRegister />
      </body>
    </html>
  );
}
