package com.conexoessolidarias.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@conexoessolidarias.org}")
    private String from;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmail(String para, String assunto, String corpoHtml) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);
            mailSender.send(msg);
            log.info("Email enviado para {}", para);
        } catch (Exception e) {
            log.warn("Falha ao enviar email para {}: {} — fallback log", para, e.getMessage());
            log.info("=== EMAIL PARA: {} ===", para);
            log.info("ASSUNTO: {}", assunto);
            log.info("CORPO: {}...", corpoHtml.length() > 100 ? corpoHtml.substring(0, 100) + "..." : corpoHtml);
            log.info("=== FIM EMAIL ===");
        }
    }

    public void notificarNovaDoacao(String emailDoador, String nomeDoador, String instituicao, String item) {
        String assunto = "Sua doação foi registrada!";
        String corpo = """
            <h2>Olá %s!</h2>
            <p>Sua intenção de doação para <strong>%s</strong> foi registrada com sucesso.</p>
            <p><strong>Item:</strong> %s</p>
            <p>Acompanhe o status no seu painel: <a href="%s/painel/doador">Meu Painel</a></p>
            <br/><p>Conexões Solidárias</p>
            """.formatted(nomeDoador, instituicao, item, frontendUrl);
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
            <p><a href="%s" style="display:inline-block;padding:12px 24px;background:#1a4d3e;color:#fff;text-decoration:none;border-radius:6px;">Redefinir Senha</a></p>
            <p>Este link expira em 1 hora.</p>
            <p>Se não foi você quem solicitou, ignore este e-mail.</p>
            <br/><p>Conexões Solidárias</p>
            """.formatted(nome, link);
        enviarEmail(email, assunto, corpo);
    }
}
