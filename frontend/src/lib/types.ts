export interface User {
  id: number;
  nome: string;
  email: string;
  tipo: "doador" | "instituicao" | "admin";
  telefone?: string | null;
  whatsapp?: string | null;
  cep?: string | null;
  cidade?: string | null;
  estado?: string | null;
  avatarUrl?: string | null;
  ativo?: boolean;
  instituicaoId?: number | null;
  instituicaoAprovada?: boolean | null;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface InstitutionSummary {
  id: number;
  razaoSocial: string;
  nomeFantasia?: string | null;
  fotoUrl?: string | null;
  cidade?: string | null;
}

export interface CampaignImage {
  id: number;
  url: string;
  legenda?: string | null;
  ordem?: number | null;
}

export interface Campaign {
  id: number;
  titulo: string;
  descricao: string;
  categoria: string;
  quantidadeAlvo: string;
  urgencia: string;
  aceitaFinanceiro: boolean;
  progresso: number;
  ativo: boolean;
  dataCriacao: string;
  instituicao?: InstitutionSummary | null;
  imagens: CampaignImage[];
}

export interface DonationUpdate {
  id: number;
  mensagem: string;
  fotoUrl?: string | null;
  dataCriacao: string;
}

export interface Donation {
  id: number;
  item: string;
  quantidade: string;
  categoria: string;
  observacao?: string | null;
  status: "pendente" | "recebido" | "cancelado";
  dataIntencao: string;
  dataRecebimento?: string | null;
  doadorNome?: string | null;
  campaignId?: number | null;
  campaignTitulo?: string | null;
  instituicaoNome?: string | null;
  updates: DonationUpdate[];
}

export interface Payment {
  uuid: string;
  valor: number;
  metodo: string;
  status: string;
  comprovanteUrl?: string | null;
  transacaoId?: string | null;
  dataCriacao: string;
  dataConfirmacao?: string | null;
  instituicaoNome?: string | null;
  campaignId?: number | null;
  campaignTitulo?: string | null;
  pixKey?: string | null;
  qrcode?: string | null;
}

export interface Institution {
  id: number;
  razaoSocial: string;
  nomeFantasia?: string | null;
  cnpj?: string | null;
  endereco?: string | null;
  website?: string | null;
  fotoUrl?: string | null;
  descricao?: string | null;
  categoriaAtuacao?: string | null;
  whatsapp?: string | null;
  pixKey?: string | null;
  pixTitular?: string | null;
  aprovado?: boolean | null;
  motivoRecusa?: string | null;
  email?: string | null;
  telefone?: string | null;
  cidade?: string | null;
  estado?: string | null;
  campanhas: Campaign[];
}

export interface BlogPost {
  id: number;
  titulo: string;
  slug: string;
  conteudo: string;
  resumo?: string | null;
  categoria: string;
  imagemUrl?: string | null;
  publicado: boolean;
  dataPublicacao?: string | null;
  autorNome?: string | null;
}

export interface NotificationItem {
  id: number;
  tipo: string;
  mensagem: string;
  link?: string | null;
  lida: boolean;
  dataCriacao: string;
}

export interface ContactMessage {
  id: number;
  nome: string;
  email: string;
  assunto?: string | null;
  mensagem: string;
  lido: boolean;
  dataEnvio: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface Stats {
  doacoesRecebidas: number;
  instituicoes: number;
  campanhasAtivas: number;
}
