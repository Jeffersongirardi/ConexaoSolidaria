package com.conexoessolidarias.api;

import com.conexoessolidarias.api.dto.ContactDTO;
import com.conexoessolidarias.api.dto.MessageResponse;
import com.conexoessolidarias.model.ContactMessage;
import com.conexoessolidarias.repository.ContactMessageRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contact")
public class ContactApiController {

    private final ContactMessageRepository contactMessageRepository;

    public ContactApiController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> enviar(
            @Valid @RequestBody ContactDTO.ContactRequest req) {
        ContactMessage msg = new ContactMessage();
        msg.setNome(req.nome());
        msg.setEmail(req.email());
        msg.setAssunto(req.assunto());
        msg.setMensagem(req.mensagem());
        contactMessageRepository.save(msg);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Mensagem enviada com sucesso!"));
    }
}
