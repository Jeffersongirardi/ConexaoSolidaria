package com.conexoessolidarias.controller;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.User;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final InstitutionProfileRepository institutionProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository,
                          InstitutionProfileRepository institutionProfileRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.institutionProfileRepository = institutionProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/login-doador")
    public String loginDoador() {
        return "login-doador";
    }

    @GetMapping("/login-instituicao")
    public String loginInstituicao() {
        return "login-instituicao";
    }

    @GetMapping("/login-admin")
    public String loginAdmin() {
        return "login-admin";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String senha,
                        @RequestParam String tipo,
                        RedirectAttributes redirect) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, senha));
            SecurityContextHolder.getContext().setAuthentication(auth);

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null || !user.getTipo().equals(tipo)) {
                redirect.addFlashAttribute("error", "Acesso negado para este tipo de conta.");
                return "redirect:/auth/login-" + tipo;
            }

            return switch (tipo) {
                case "doador" -> "redirect:/dashboard/doador";
                case "instituicao" -> "redirect:/dashboard/instituicao";
                case "admin" -> "redirect:/admin";
                default -> "redirect:/";
            };
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "E-mail ou senha inválidos.");
            return "redirect:/auth/login-" + tipo;
        }
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/";
    }

    @PostMapping("/cadastro-doador")
    public String cadastroDoador(@RequestParam String nome,
                                 @RequestParam String email,
                                 @RequestParam String senha,
                                 @RequestParam(required = false) String telefone,
                                 @RequestParam(required = false) String cpf,
                                 @RequestParam(required = false) String dataNascimento,
                                 @RequestParam(required = false) String whatsapp,
                                 @RequestParam(required = false) String cep,
                                 @RequestParam(required = false) String cidade,
                                 @RequestParam(required = false) String estado,
                                 RedirectAttributes redirect) {
        if (userRepository.existsByEmail(email)) {
            redirect.addFlashAttribute("error", "E-mail já cadastrado.");
            return "redirect:/auth/login-doador";
        }
        User user = new User(nome, email, passwordEncoder.encode(senha), "doador");
        user.setTelefone(telefone);
        user.setCpf(cpf);
        user.setWhatsapp(whatsapp);
        user.setCep(cep);
        user.setCidade(cidade);
        user.setEstado(estado);
        if (dataNascimento != null && !dataNascimento.isEmpty()) {
            user.setDataNascimento(LocalDate.parse(dataNascimento));
        }
        userRepository.save(user);
        redirect.addFlashAttribute("success", "Conta criada com sucesso! Faça login.");
        return "redirect:/auth/login-doador";
    }

    @GetMapping("/cadastro-doador")
    public String formCadastroDoador() {
        return "cadastro-doador";
    }

    @PostMapping("/cadastro-instituicao")
    public String cadastroInstituicao(@RequestParam String nome,
                                      @RequestParam String email,
                                      @RequestParam String senha,
                                      @RequestParam String cnpj,
                                      @RequestParam String razaoSocial,
                                      @RequestParam(required = false) String nomeFantasia,
                                      @RequestParam(required = false) String telefone,
                                      @RequestParam(required = false) String whatsapp,
                                      @RequestParam(required = false) String endereco,
                                      @RequestParam(required = false) String descricao,
                                      @RequestParam(required = false) String categoriaAtuacao,
                                      @RequestParam(required = false) String pixKey,
                                      @RequestParam(required = false) String pixTitular,
                                      RedirectAttributes redirect) {
        if (userRepository.existsByEmail(email)) {
            redirect.addFlashAttribute("error", "E-mail já cadastrado.");
            return "redirect:/auth/login-instituicao";
        }
        User user = new User(nome, email, passwordEncoder.encode(senha), "instituicao");
        user.setTelefone(telefone);
        user.setWhatsapp(whatsapp);
        user = userRepository.save(user);

        InstitutionProfile profile = new InstitutionProfile();
        profile.setUser(user);
        profile.setCnpj(cnpj);
        profile.setRazaoSocial(razaoSocial);
        profile.setNomeFantasia(nomeFantasia);
        profile.setEndereco(endereco);
        profile.setDescricao(descricao);
        profile.setCategoriaAtuacao(categoriaAtuacao);
        profile.setWhatsapp(whatsapp);
        profile.setPixKey(pixKey);
        profile.setPixTitular(pixTitular);
        institutionProfileRepository.save(profile);

        redirect.addFlashAttribute("success", "Cadastro realizado! Aguarde aprovação.");
        return "redirect:/auth/login-instituicao";
    }

    @GetMapping("/cadastro-instituicao")
    public String formCadastroInstituicao() {
        return "cadastro-instituicao";
    }
}
