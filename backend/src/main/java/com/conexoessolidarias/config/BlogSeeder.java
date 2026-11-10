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
 * Publica o primeiro conteúdo editorial apenas quando o blog ainda está vazio.
 * A instituição/admin podem editar ou remover o post depois pelo painel.
 */
@Component
@Order(2)
public class BlogSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BlogSeeder.class);
    private static final String SLUG = "como-escolher-instituicao-de-caridade";

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;

    public BlogSeeder(BlogPostRepository blogPostRepository, UserRepository userRepository) {
        this.blogPostRepository = blogPostRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (blogPostRepository.count() > 0) return;

        List<User> admins = userRepository.findAllByTipoAndAtivoTrue("admin");
        if (admins.isEmpty()) return;

        var resource = new ClassPathResource("blog/como-escolher-instituicao-de-caridade.txt");
        if (!resource.exists()) {
            log.warn("Conteúdo inicial do blog não encontrado no classpath.");
            return;
        }

        BlogPost post = new BlogPost();
        post.setTitulo("Como escolher uma instituição de caridade para doar com segurança");
        post.setSlug(SLUG);
        post.setResumo("Antes de doar, confira CNPJ ativo, canais oficiais e sinais de transparência. Veja como a plataforma valida e o que você pode verificar por conta própria.");
        post.setConteudo(resource.getContentAsString(StandardCharsets.UTF_8));
        post.setCategoria("transparencia");
        post.setImagemUrl("/img/entrega-confirmada.jpg");
        post.setPublicado(true);
        post.setDataPublicacao(LocalDateTime.now());
        post.setAutor(admins.get(0));
        blogPostRepository.save(post);
        log.info("Post inicial do blog publicado: {}", SLUG);
    }
}
