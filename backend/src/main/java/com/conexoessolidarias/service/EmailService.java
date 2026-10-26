package com.conexoessolidarias.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final RestClient resendClient;
    private final String resendApiKey;
    private final String provider;

    @Value("${app.mail.from:noreply@conexoessolidarias.org}")
    private String from;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender, RestClient.Builder builder,
                        @Value("${app.mail.resend-api-key:}") String resendApiKey,
                        @Value("${app.mail.provider:smtp}") String provider) {
        this(mailSender, criarClienteResend(builder), resendApiKey, provider);
    }

    EmailService(JavaMailSender mailSender, RestClient resendClient, String resendApiKey, String provider) {
        this.mailSender = mailSender;
        this.resendClient = resendClient;
        this.resendApiKey = resendApiKey.trim();
        this.provider = provider;
    }

    private static RestClient criarClienteResend(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return builder.baseUrl("https://api.resend.com").requestFactory(factory).build();
    }

    public void enviarEmail(String para, String assunto, String corpoHtml) {
        if ("resend".equalsIgnoreCase(provider) && resendApiKey.isBlank()) {
            log.warn("Email não enviado: configure RESEND_API_KEY nas variáveis do backend.");
            return;
        }
        try {
            if ("resend".equalsIgnoreCase(provider)) {
                enviarViaResend(para, assunto, corpoHtml);
            } else if ("smtp".equalsIgnoreCase(provider)) {
                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
                helper.setFrom(from);
                helper.setTo(para);
                helper.setSubject(assunto);
                helper.setText(corpoHtml, true);
                mailSender.send(msg);
            } else {
                throw new IllegalStateException("Provedor de email inválido: use resend ou smtp");
            }
            log.info("Email aceito pelo provedor {}", provider);
        } catch (RestClientResponseException e) {
            // Não registrar corpo da resposta/requisição: pode conter dados pessoais ou links de reset.
            log.warn("Email não enviado pelo Resend: HTTP {}. Confira a chave, o remetente e o destinatário no painel Resend.",
                    e.getStatusCode().value());
        } catch (Exception e) {
            // Preserva o fluxo de doação/reset mesmo se o provedor estiver indisponível.
            log.warn("Email não enviado pelo provedor {} ({}). Confira a configuração do serviço.",
                    provider, e.getClass().getSimpleName());
        }
    }

    private void enviarViaResend(String para, String assunto, String corpoHtml) {
        resendClient.post().uri("/emails")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("from", from, "to", List.of(para), "subject", assunto, "html", corpoHtml))
                .retrieve().toBodilessEntity();
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
