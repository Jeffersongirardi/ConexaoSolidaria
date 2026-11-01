package com.conexoessolidarias.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class EmailServiceTest {
    private JavaMailSender smtp;
    private MockRestServiceServer server;
    private RestClient client;

    @BeforeEach
    void preparar() {
        smtp = mock(JavaMailSender.class);
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.resend.com");
        server = MockRestServiceServer.bindTo(builder).build();
        client = builder.build();
    }

    private EmailService service(String key, String provider) {
        EmailService service = new EmailService(smtp, client, key, provider);
        ReflectionTestUtils.setField(service, "from", "onboarding@resend.dev");
        return service;
    }

    @Test
    void enviaJsonAutenticadoComAcentosEHtmlSemUsarSmtp() {
        server.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer chave-ficticia"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.from").value("onboarding@resend.dev"))
                .andExpect(jsonPath("$.to[0]").value("destino@example.org"))
                .andExpect(jsonPath("$.subject").value("Doação confirmada"))
                .andExpect(jsonPath("$.html").value("<p>Olá \"André\"!</p>"))
                .andRespond(withSuccess("{\"id\":\"email-teste\"}", MediaType.APPLICATION_JSON));
        service("chave-ficticia", "resend").enviarEmail("destino@example.org", "Doação confirmada", "<p>Olá \"André\"!</p>");
        server.verify();
        verifyNoInteractions(smtp);
    }

    @Test
    void rejeicaoDoResendNaoTentaSmtpNemInterrompeFluxo() {
        server.expect(requestTo("https://api.resend.com/emails"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));
        assertDoesNotThrow(() -> service("chave-ficticia", "resend")
                .enviarEmail("destino@example.org", "Teste", "<p>Teste</p>"));
        server.verify();
        verifyNoInteractions(smtp);
    }

    @Test
    void semChaveNaoFazRequisicaoNemTentaSmtpEmProd() {
        assertDoesNotThrow(() -> service("", "resend")
                .enviarEmail("destino@example.org", "Teste", "<p>Teste</p>"));
        server.verify();
        verifyNoInteractions(smtp);
    }

    @Test
    void mantemSmtpNoDesenvolvimento() {
        when(smtp.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        service("", "smtp").enviarEmail("destino@example.org", "Teste", "<p>Teste</p>");
        verify(smtp).send(any(MimeMessage.class));
        server.verify();
    }
    @Test
    void eventoSoEnviaDepoisDoCommit() {
        var db = new EmbeddedDatabaseBuilder().generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2).build();
        try {
            var requests = new AtomicInteger();
            server.expect(requestTo("https://api.resend.com/emails"))
                    .andExpect(jsonPath("$.to[0]").value("instituicao@example.org"))
                    .andExpect(jsonPath("$.subject").value("Novo Pix iniciado"))
                    .andRespond(request -> {
                        requests.incrementAndGet();
                        return withSuccess("{}", MediaType.APPLICATION_JSON).createResponse(request);
                    });
            var tx = new TransactionTemplate(new DataSourceTransactionManager(db));
            tx.executeWithoutResult(status -> {
                service("chave-ficticia", "resend").evento(
                        "instituicao@example.org", "Novo Pix iniciado", "<p>Teste</p>");
                assertEquals(0, requests.get(), "Não enviar antes do commit");
            });
            assertEquals(1, requests.get());
            server.verify();
            verifyNoInteractions(smtp);
        } finally {
            db.shutdown();
        }
    }

    @Test
    void rollbackDescartaEmailDoEvento() {
        var db = new EmbeddedDatabaseBuilder().generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2).build();
        try {
            var tx = new TransactionTemplate(new DataSourceTransactionManager(db));
            tx.executeWithoutResult(status -> {
                service("chave-ficticia", "resend").evento(
                        "instituicao@example.org", "Novo Pix iniciado", "<p>Teste</p>");
                status.setRollbackOnly();
            });
            // Sem expectativas: qualquer chamada HTTP faz o teste falhar.
            server.verify();
            verifyNoInteractions(smtp);
        } finally {
            db.shutdown();
        }
    }

}
