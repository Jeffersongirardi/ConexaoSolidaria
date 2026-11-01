import {
  Apple, Baby, Droplets, Footprints, GraduationCap, Package, PawPrint,
  Shirt, Siren, Sofa, ToyBrick, type LucideIcon,
} from "lucide-react";

const ICONES: Record<string, LucideIcon> = {
  alimento: Apple,
  roupa: Shirt,
  calcado: Footprints,
  higiene: Droplets,
  fralda: Baby,
  material_escolar: GraduationCap,
  brinquedo: ToyBrick,
  movel: Sofa,
  racao_animal: PawPrint,
  emergencia: Siren,
  outro: Package,
};

export default function CategoryIcon({
  categoria,
  className,
}: {
  categoria?: string | null;
  className?: string;
}) {
  const Icon = ICONES[categoria ?? "outro"] ?? Package;
  return <Icon className={className} aria-hidden="true" />;
}
