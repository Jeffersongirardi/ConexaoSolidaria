package com.conexoessolidarias.controller;

import com.conexoessolidarias.model.ContactMessage;
import com.conexoessolidarias.repository.ContactMessageRepository;
import com.conexoessolidarias.repository.DonationRepository;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.repository.NeedRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {

    private final NeedRepository needRepository;
    private final DonationRepository donationRepository;
    private final InstitutionProfileRepository institutionProfileRepository;
    private final ContactMessageRepository contactMessageRepository;

    public MainController(NeedRepository needRepository,
                          DonationRepository donationRepository,
                          InstitutionProfileRepository institutionProfileRepository,
                          ContactMessageRepository contactMessageRepository) {
        this.needRepository = needRepository;
        this.donationRepository = donationRepository;
        this.institutionProfileRepository = institutionProfileRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("needs", needRepository.findTop6ByAtivoTrueOrderByDataCriacaoDesc());
        model.addAttribute("stats", java.util.Map.of(
            "doacoes", donationRepository.countByStatus("recebido"),
            "instituicoes", institutionProfileRepository.countByAprovado(true)
        ));
        return "index";
    }

    @GetMapping("/sobre")
    public String sobre(Model model) {
        model.addAttribute("stats", java.util.Map.of(
            "doacoes", donationRepository.countByStatus("recebido"),
            "instituicoes", institutionProfileRepository.countByAprovado(true)
        ));
        return "sobre";
    }

    @GetMapping("/contato")
    public String contato() {
        return "contato";
    }

    @PostMapping("/contato")
    public String enviarContato(@RequestParam String nome,
                                @RequestParam String email,
                                @RequestParam(required = false) String assunto,
                                @RequestParam String mensagem,
                                RedirectAttributes redirect) {
        ContactMessage msg = new ContactMessage();
        msg.setNome(nome);
        msg.setEmail(email);
        msg.setAssunto(assunto);
        msg.setMensagem(mensagem);
        contactMessageRepository.save(msg);
        redirect.addFlashAttribute("success", "Mensagem enviada com sucesso!");
        return "redirect:/contato";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/privacidade")
    public String privacidade() {
        return "privacidade";
    }
}
