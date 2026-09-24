package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.BlogDTO;
import com.conexoessolidarias.api.dto.MessageResponse;
import com.conexoessolidarias.model.BlogPost;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.BlogPostRepository;
import com.conexoessolidarias.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/blog")
public class BlogApiController {

    private final BlogPostRepository blogPostRepository;

    public BlogApiController(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Page<BlogDTO> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<BlogPost> result = (categoria != null && !categoria.isBlank())
                ? blogPostRepository.findByPublicadoTrueAndCategoriaOrderByDataPublicacaoDesc(
                        categoria, pageable)
                : blogPostRepository.findByPublicadoTrueOrderByDataPublicacaoDesc(pageable);
        return result.map(BlogDTO::from);
    }

    @GetMapping("/categorias")
    @Transactional(readOnly = true)
    public ResponseEntity<List<String>> categorias() {
        return ResponseEntity.ok(blogPostRepository.findCategoriasPublicadas());
    }

    @GetMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<BlogDTO> porId(@PathVariable Long id) {
        return ResponseEntity.ok(BlogDTO.from(blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post não encontrado"))));
    }

    @GetMapping("/{slug}")
    @Transactional(readOnly = true)
    public ResponseEntity<BlogDTO> detalhe(@PathVariable String slug) {
        return ResponseEntity.ok(BlogDTO.from(blogPostRepository.findBySlugAndPublicadoTrue(slug)
                .orElseThrow(() -> new EntityNotFoundException("Post não encontrado"))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<BlogDTO> criar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody BlogDTO.BlogRequest req) {
        BlogPost post = new BlogPost();
        apply(post, req);
        post.setAutor(principal.getUser());
        post.setSlug(slugify(req.titulo()));
        if (Boolean.TRUE.equals(post.getPublicado())) post.setDataPublicacao(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BlogDTO.from(blogPostRepository.save(post)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<BlogDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody BlogDTO.BlogRequest req) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post não encontrado"));
        apply(post, req);
        if (Boolean.TRUE.equals(post.getPublicado()) && post.getDataPublicacao() == null) {
            post.setDataPublicacao(LocalDateTime.now());
        }
        return ResponseEntity.ok(BlogDTO.from(blogPostRepository.save(post)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<MessageResponse> remover(@PathVariable Long id) {
        if (!blogPostRepository.existsById(id)) {
            throw new EntityNotFoundException("Post não encontrado");
        }
        blogPostRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Post removido"));
    }

    private void apply(BlogPost post, BlogDTO.BlogRequest req) {
        post.setTitulo(req.titulo());
        post.setConteudo(req.conteudo());
        post.setResumo(req.resumo());
        post.setCategoria(req.categoria() != null ? req.categoria() : "geral");
        post.setImagemUrl(req.imagemUrl());
        post.setPublicado(Boolean.TRUE.equals(req.publicado()));
    }

    private String slugify(String text) {
        String slug = text.toLowerCase().trim()
                .replaceAll("[^\\w\\s-]", "")
                .replaceAll("[\\s-]+", "-");
        if (slug.length() > 70) slug = slug.substring(0, 70);
        String base = slug;
        int i = 1;
        while (blogPostRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + (i++);
        }
        return slug;
    }
}
