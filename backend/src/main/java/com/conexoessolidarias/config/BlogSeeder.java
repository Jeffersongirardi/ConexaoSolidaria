package com.conexoessolidarias.config;

import com.conexoessolidarias.model.BlogPost;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.BlogPostRepository;
import com.conexoessolidarias.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Publica os conteúdos editoriais iniciais que ainda não existirem.
 * Cada post é identificado pelo slug, então novos posts podem ser adicionados
 * sem duplicar os já publicados.
 */
@Component
@Order(2)
public class BlogSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BlogSeeder.class);

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;

    public BlogSeeder(BlogPostRepository blogPostRepository, UserRepository userRepository) {
        this.blogPostRepository = blogPostRepository;
        this.userRepository = userRepository;
    }

    private record Seed(String slug, String titulo, String resumo, String categoria,
                        String imagem, String recurso) {
    }

    private static final List<Seed> SEEDS = List.of(
            new Seed(
                    "como-escolher-instituicao-de-caridade",
                    "Como escolher uma instituição de caridade para doar com segurança",
                    "Antes de doar, confira CNPJ ativo, canais oficiais e sinais de transparência. Veja como a plataforma valida e o que você pode verificar por conta própria.",
                    "transparencia",
                    "/img/entrega-confirmada.jpg",
                    "blog/como-escolher-instituicao-de-caridade.txt"
            ),
            new Seed(
                    "doacao-de-roupas-o-que-doar",
                    "Doação de roupas: o que doar, o que evitar e como preparar",
                    "Peças limpas, sem rasgos e prontas para uso fazem diferença. Veja o que doar, o que evitar e como preparar sua sacola.",
                    "geral",
                    "/img/categoria-roupa.jpg",
                    "blog/doacao-de-roupas-o-que-doar.txt"
            ),
            new Seed(
                    "doacao-de-alimentos-itens-que-ajudam",
                    "Doação de alimentos: itens que realmente ajudam",
                    "Alimentos não perecíveis, dentro da validade e bem embalados ajudam de verdade. Entenda o que priorizar.",
                    "geral",
                    "/img/categoria-alimento.jpg",
                    "blog/doacao-de-alimentos-itens-que-ajudam.txt"
            ),
            new Seed(
                    "pix-direto-para-instituicoes",
                    "Pix direto para instituições: como funciona e por que não cobramos taxa",
                    "No Conexões Solidárias, o Pix vai direto para a instituição. Sem custódia, sem taxa e com confirmação de recebimento.",
                    "transparencia",
                    "/img/pix-direto.jpg",
                    "blog/pix-direto-para-instituicoes.txt"
            ),
            new Seed(
                    "o-que-acontece-depois-da-doacao",
                    "O que acontece depois que você registra uma doação",
                    "Da intenção ao comprovante: entenda cada etapa e saiba onde consultar WhatsApp, endereço e instruções depois.",
                    "transparencia",
                    "/img/entrega-confirmada.jpg",
                    "blog/o-que-acontece-depois-da-doacao.txt"
            ),
            new Seed(
                    "como-instituicoes-participam",
                    "Como instituições podem participar da rede",
                    "Sua instituição tem CNPJ ativo e quer receber doações? Veja como entrar na rede e publicar campanhas.",
                    "parcerias",
                    "/img/depoimento-voluntario.jpg",
                    "blog/como-instituicoes-participam.txt"
            )
    );

    @Override
    public void run(String... args) throws Exception {
        List<User> admins = userRepository.findAllByTipoAndAtivoTrue("admin");
        if (admins.isEmpty()) return;

        for (Seed seed : SEEDS) {
            if (blogPostRepository.findBySlug(seed.slug()).isPresent()) continue;

            var resource = new ClassPathResource(seed.recurso());
            if (!resource.exists()) {
                log.warn("Conteúdo inicial do blog não encontrado: {}", seed.recurso());
                continue;
            }

            BlogPost post = new BlogPost();
            post.setTitulo(seed.titulo());
            post.setSlug(seed.slug());
            post.setResumo(seed.resumo());
            post.setConteudo(resource.getContentAsString(StandardCharsets.UTF_8));
            post.setCategoria(seed.categoria());
            post.setImagemUrl(seed.imagem());
            post.setPublicado(true);
            post.setDataPublicacao(LocalDateTime.now());
            post.setAutor(admins.get(0));
            blogPostRepository.save(post);
            log.info("Post inicial do blog publicado: {}", seed.slug());
        }
    }
}
