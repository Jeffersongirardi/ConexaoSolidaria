package com.conexoessolidarias.controller;

import com.conexoessolidarias.model.BlogPost;
import com.conexoessolidarias.repository.BlogPostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/blog")
public class BlogController {

    private final BlogPostRepository blogPostRepository;

    public BlogController(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String categoria,
                         @RequestParam(defaultValue = "1") int page,
                         Model model) {
        var pageable = PageRequest.of(page - 1, 6);
        var pagination = categoria != null && !categoria.isEmpty()
            ? blogPostRepository.findByPublicadoTrueAndCategoriaOrderByDataPublicacaoDesc(categoria, pageable)
            : blogPostRepository.findByPublicadoTrueOrderByDataPublicacaoDesc(pageable);

        model.addAttribute("pagination", pagination);
        model.addAttribute("categorias", blogPostRepository.findCategoriasPublicadas());
        return "blog-publico";
    }

    @GetMapping("/{slug}")
    public String detalhe(@PathVariable String slug, Model model) {
        BlogPost post = blogPostRepository.findBySlugAndPublicadoTrue(slug).orElse(null);
        if (post == null) return "error/404";
        model.addAttribute("post", post);
        return "blog-post";
    }
}
