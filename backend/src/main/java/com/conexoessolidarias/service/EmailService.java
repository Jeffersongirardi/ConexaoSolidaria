package com.conexoessolidarias.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void enviarEmail(String para, String assunto, String corpoHtml) {
        log.info("=== EMAIL PARA: {} ===", para);
        log.info("ASSUNTO: {}", assunto);
        log.info("CORPO: {}...", corpoHtml.length() > 100 ? corpoHtml.substring(0, 100) + "..." : corpoHtml);
        log.info("=== FIM EMAIL ===");
    }

    public void notificarNovaDoacao(String emailDoador, String nomeDoador, String instituicao, String item) {
        String assunto = "Sua doação foi registrada!";
        String corpo = """
            <h2>Olá %s!</h2>
            <p>Sua intenção de doação para <strong>%s</strong> foi registrada com sucesso.</p>
            <p><strong>Item:</strong> %s</p>
            <p>Acompanhe o status no seu painel: <a href="https://localhost:8080/dashboard/doador">Meu Painel</a></p>
            <br/><p>Conexões Solidárias</p>
            """.formatted(nomeDoador, instituicao, item);
        enviarEmail(emailDoador, assunto, corpo);
    }

    public void notificarDoacaoConfirmada(String emailDoador, String nomeDoador, String instituicao, String item) {
        String assunto = "Sua doação foi confirmada!";
        String corpo = """
            <h2>Ótima notícia, %s!</h2>
            <p><strong>%s</strong> confirmou o recebimento da sua doação: <strong>%s</strong></p>
            <p>Obrigado por fazer a diferença!</p>
            <br/><p>Conexões Solidárias</p>
            """.formatted(nomeDoador, instituicao, item);
        enviarEmail(emailDoador, assunto, corpo);
    }

    public void enviarLinkRedefinicaoSenha(String email, String nome, String link) {
        String assunto = "Redefinição de senha - Conexões Solidárias";
        String corpo = """
            <h2>Olá %s!</h2>
            <p>Recebemos uma solicitação de redefinição de senha para sua conta.</p>
            <p><a href="%s" style="display:inline-block;padding:12px 24px;background:#007bff;color:#fff;text-decoration:none;border-radius:6px;">Redefinir Senha</a></p>
            <p>Este link expira em 1 hora.</p>
            <p>Se não foi você quem solicitou, ignore este e-mail.</p>
            <br/><p>Conexões Solidárias</p>
            """.formatted(nome, link);
        enviarEmail(email, assunto, corpo);
    }
}
