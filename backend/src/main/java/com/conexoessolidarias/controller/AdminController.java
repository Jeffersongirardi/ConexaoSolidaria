package com.conexoessolidarias.controller;

import com.conexoessolidarias.model.*;
import com.conexoessolidarias.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final InstitutionProfileRepository institutionProfileRepository;
    private final NeedRepository needRepository;
    private final DonationRepository donationRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final BlogPostRepository blogPostRepository;

    public AdminController(UserRepository userRepository,
                           InstitutionProfileRepository institutionProfileRepository,
                           NeedRepository needRepository,
                           DonationRepository donationRepository,
                           ContactMessageRepository contactMessageRepository,
                           BlogPostRepository blogPostRepository) {
        this.userRepository = userRepository;
        this.institutionProfileRepository = institutionProfileRepository;
        this.needRepository = needRepository;
        this.donationRepository = donationRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.blogPostRepository = blogPostRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", java.util.Map.of(
            "usuarios", userRepository.count(),
            "instituicoes", institutionProfileRepository.count(),
            "pendentes", institutionProfileRepository.countByAprovado(false),
            "doacoes", donationRepository.count(),
            "necessidades", needRepository.count(),
            "mensagens", contactMessageRepository.countByLidoFalse()
        ));
        return "admin/dashboard";
    }

    @GetMapping("/instituicoes")
    public String instituicoes(@RequestParam(defaultValue = "pendentes") String filtro, Model model) {
        java.util.List<InstitutionProfile> lista;
        if ("pendentes".equals(filtro)) {
            lista = institutionProfileRepository.findByAprovadoOrderByDataCadastroDesc(false);
        } else if ("aprovadas".equals(filtro)) {
            lista = institutionProfileRepository.findByAprovadoOrderByDataCadastroDesc(true);
        } else {
            lista = institutionProfileRepository.findAllByOrderByDataCadastroDesc();
        }
        model.addAttribute("instituicoes", lista);
        model.addAttribute("filtro", filtro);
        return "admin/instituicoes";
    }

    @PostMapping("/instituicoes/aprovar/{id}")
    public String aprovarInstituicao(@PathVariable Long id) {
        InstitutionProfile profile = institutionProfileRepository.findById(id).orElse(null);
        if (profile != null) {
            profile.setAprovado(true);
            profile.setMotivoRecusa(null);
            institutionProfileRepository.save(profile);
        }
        return "redirect:/admin/instituicoes";
    }

    @PostMapping("/instituicoes/recusar/{id}")
    public String recusarInstituicao(@PathVariable Long id,
                                      @RequestParam(defaultValue = "") String motivo) {
        InstitutionProfile profile = institutionProfileRepository.findById(id).orElse(null);
        if (profile != null) {
            profile.setMotivoRecusa(motivo);
            institutionProfileRepository.save(profile);
        }
        return "redirect:/admin/instituicoes";
    }

    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("usuarios", userRepository.findAll());
        return "admin/usuarios";
    }

    @GetMapping("/usuarios/toggle/{id}")
    public String toggleUsuario(@PathVariable Long id, RedirectAttributes redirect) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            if ("admin".equals(user.getTipo())) {
                redirect.addFlashAttribute("error", "Não é possível desativar o admin.");
                return "redirect:/admin/usuarios";
            }
            user.setAtivo(!user.getAtivo());
            userRepository.save(user);
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/mensagens")
    public String mensagens(Model model) {
        model.addAttribute("mensagens", contactMessageRepository.findAllByOrderByDataEnvioDesc());
        return "admin/mensagens";
    }

    @GetMapping("/mensagens/ler/{id}")
    public String lerMensagem(@PathVariable Long id) {
        ContactMessage msg = contactMessageRepository.findById(id).orElse(null);
        if (msg != null) {
            msg.setLido(true);
            contactMessageRepository.save(msg);
        }
        return "redirect:/admin/mensagens";
    }

    @GetMapping("/blog")
    public String blog(Model model) {
        model.addAttribute("posts", blogPostRepository.findAllByOrderByDataCriacaoDesc());
        return "admin/blog";
    }

    @GetMapping("/blog/criar")
    public String blogCriarForm(Model model) {
        model.addAttribute("post", new BlogPost());
        return "admin/blog-form";
    }

    @PostMapping("/blog/criar")
    public String blogCriar(@RequestParam String titulo,
                            @RequestParam String conteudo,
                            @RequestParam(required = false) String resumo,
                            @RequestParam(defaultValue = "geral") String categoria,
                            @RequestParam(required = false) String imagemUrl,
                            @RequestParam(defaultValue = "false") boolean publicado,
                            RedirectAttributes redirect) {
        BlogPost post = new BlogPost();
        post.setTitulo(titulo);
        post.setConteudo(conteudo);
        post.setResumo(resumo);
        post.setCategoria(categoria);
        post.setImagemUrl(imagemUrl);
        post.setPublicado(publicado);
        post.setSlug(slugify(titulo));
        if (publicado) post.setDataPublicacao(LocalDateTime.now());
        blogPostRepository.save(post);
        redirect.addFlashAttribute("success", "Post criado com sucesso!");
        return "redirect:/admin/blog";
    }

    @GetMapping("/blog/editar/{id}")
    public String blogEditarForm(@PathVariable Long id, Model model) {
        BlogPost post = blogPostRepository.findById(id).orElse(null);
        if (post == null) return "redirect:/admin/blog";
        model.addAttribute("post", post);
        return "admin/blog-form";
    }

    @PostMapping("/blog/editar/{id}")
    public String blogEditar(@PathVariable Long id,
                             @RequestParam String titulo,
                             @RequestParam String conteudo,
                             @RequestParam(required = false) String resumo,
                             @RequestParam(defaultValue = "geral") String categoria,
                             @RequestParam(required = false) String imagemUrl,
                             @RequestParam(defaultValue = "false") boolean publicado,
                             RedirectAttributes redirect) {
        BlogPost post = blogPostRepository.findById(id).orElse(null);
        if (post != null) {
            post.setTitulo(titulo);
            post.setConteudo(conteudo);
            post.setResumo(resumo);
            post.setCategoria(categoria);
            post.setImagemUrl(imagemUrl);
            post.setPublicado(publicado);
            if (publicado && post.getDataPublicacao() == null) {
                post.setDataPublicacao(LocalDateTime.now());
            }
            blogPostRepository.save(post);
            redirect.addFlashAttribute("success", "Post atualizado!");
        }
        return "redirect:/admin/blog";
    }

    @PostMapping("/blog/deletar/{id}")
    public String blogDeletar(@PathVariable Long id, RedirectAttributes redirect) {
        blogPostRepository.deleteById(id);
        redirect.addFlashAttribute("info", "Post removido.");
        return "redirect:/admin/blog";
    }

    private String slugify(String text) {
        String slug = text.toLowerCase().trim()
                .replaceAll("[^\\w\\s-]", "")
                .replaceAll("[\\s-]+", "-");
        return slug.length() > 80 ? slug.substring(0, 80) : slug;
    }
}
