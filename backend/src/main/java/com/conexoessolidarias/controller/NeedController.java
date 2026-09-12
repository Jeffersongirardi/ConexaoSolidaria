package com.conexoessolidarias.controller;

import com.conexoessolidarias.model.Need;
import com.conexoessolidarias.repository.NeedRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/necessidades")
public class NeedController {

    private final NeedRepository needRepository;

    public NeedController(NeedRepository needRepository) {
        this.needRepository = needRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String categoria,
                         @RequestParam(required = false) String urgencia,
                         @RequestParam(required = false) String busca,
                         @RequestParam(defaultValue = "1") int page,
                         Model model) {
        if ("todas".equals(categoria)) categoria = null;
        if ("todas".equals(urgencia)) urgencia = null;
        if (busca != null && busca.isBlank()) busca = null;

        Page<Need> pagination = needRepository.filtrar(categoria, urgencia, busca, PageRequest.of(page - 1, 12));

        model.addAttribute("pagination", pagination);
        model.addAttribute("categorias", new String[]{"alimento", "roupa", "higiene", "material_escolar", "outro"});
        model.addAttribute("filtros", java.util.Map.of(
            "categoria", categoria != null ? categoria : "",
            "urgencia", urgencia != null ? urgencia : "",
            "busca", busca != null ? busca : ""
        ));
        return "necessidades-listar";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Need need = needRepository.findById(id).orElse(null);
        if (need == null) return "error/404";
        model.addAttribute("need", need);
        model.addAttribute("institution", need.getInstitution());
        model.addAttribute("images", need.getImages());
        return "necessidades-detalhe";
    }
}
