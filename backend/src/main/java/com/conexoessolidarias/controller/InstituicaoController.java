package com.conexoessolidarias.controller;

import com.conexoessolidarias.model.InstitutionProfile;
import com.conexoessolidarias.model.Need;
import com.conexoessolidarias.repository.InstitutionProfileRepository;
import com.conexoessolidarias.repository.NeedRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/instituicoes")
public class InstituicaoController {

    private final InstitutionProfileRepository institutionProfileRepository;
    private final NeedRepository needRepository;

    public InstituicaoController(InstitutionProfileRepository institutionProfileRepository,
                                  NeedRepository needRepository) {
        this.institutionProfileRepository = institutionProfileRepository;
        this.needRepository = needRepository;
    }

    @GetMapping
    public String listar(Model model) {
        List<InstitutionProfile> instituicoes = institutionProfileRepository.findByAprovadoOrderByDataCadastroDesc(true);
        model.addAttribute("instituicoes", instituicoes);
        return "instituicoes";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        InstitutionProfile profile = institutionProfileRepository.findById(id).orElse(null);
        if (profile == null || !Boolean.TRUE.equals(profile.getAprovado())) return "error/404";
        List<Need> needs = needRepository.findByInstitutionIdOrderByDataCriacaoDesc(profile.getId());
        model.addAttribute("profile", profile);
        model.addAttribute("needs", needs);
        return "instituicao-publica";
    }
}
