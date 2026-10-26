package com.conexoessolidarias.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.net.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import com.conexoessolidarias.api.UploadController;
import com.conexoessolidarias.config.R2Config;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class R2StorageServiceTest {
    private S3Client client;
    private S3Presigner signer;
    private R2StorageService storage;
    private static final String NAME = "0123456789abcdef0123456789abcdef.png";

    @BeforeEach void setup() {
        client = mock(S3Client.class);
        signer = S3Presigner.builder().endpointOverride(URI.create("https://test.r2.cloudflarestorage.com"))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("fake-access", "fake-secret")))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).build();
        storage = new R2StorageService(client, signer, "staging-test");
    }
    @AfterEach void close() { signer.close(); }

    @Test void uploadsBytesAndPersistsOnlyStablePath() throws Exception {
        var file = new MockMultipartFile("foto", "original.PNG", "image/png", new byte[]{1, 2, 3});
        String path = storage.save(file, "comprovantes");
        assertTrue(path.matches("/uploads/comprovantes/[a-f0-9]{32}\\.png"));
        var request = ArgumentCaptor.forClass(PutObjectRequest.class);
        var body = ArgumentCaptor.forClass(RequestBody.class);
        verify(client).putObject(request.capture(), body.capture());
        assertEquals("staging-test", request.getValue().bucket());
        assertEquals(path.substring(9), request.getValue().key());
        assertEquals("image/png", request.getValue().contentType());
        assertArrayEquals(new byte[]{1, 2, 3}, body.getValue().contentStreamProvider().newStream().readAllBytes());
        assertFalse(path.contains("fake-access"));
    }

    @Test void rejectsInvalidFilesAndTraversalBeforeNetwork() {
        assertNull(storage.save(new MockMultipartFile("f", "a.html", "text/html", new byte[]{1}), "avatars"));
        assertNull(storage.save(new MockMultipartFile("f", "a.png", "image/jpeg", new byte[]{1}), "avatars"));
        assertNull(storage.save(new MockMultipartFile("f", "a.png", "image/png", new byte[]{1}), "../avatars"));
        assertNull(storage.save(new MockMultipartFile("f", "a.png", "image/png", new byte[5 * 1024 * 1024 + 1]), "avatars"));
        verifyNoInteractions(client);
    }

    @Test void providerFailureDoesNotReportSuccessfulUpload() {
        when(client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("test"));
        assertThrows(IllegalStateException.class, () -> storage.save(
                new MockMultipartFile("f", "a.png", "image/png", new byte[]{1}), "avatars"));
    }

    @Test void signedPrivateLinksExpireAndPublicPathsStayStable() {
        String path = "/uploads/comprovantes/" + NAME;
        String link = storage.resolveReadUrl(path);
        assertTrue(link.startsWith("https://test.r2.cloudflarestorage.com/staging-test/comprovantes/" + NAME));
        assertTrue(link.contains("X-Amz-Expires=900"));
        assertTrue(link.contains("X-Amz-Signature="));
        assertTrue(storage.resolveReadUrl("/uploads/updates/" + NAME).contains("X-Amz-Signature="));
        assertEquals("/uploads/avatars/" + NAME, storage.resolveReadUrl("/uploads/avatars/" + NAME));
    }

    @Test void anonymousRedirectCannotSignPrivateObjects() {
        var controller = new UploadController(storage);
        assertEquals(404, controller.image("comprovantes", NAME).getStatusCode().value());
        assertEquals(404, controller.image("updates", NAME).getStatusCode().value());
        assertEquals(404, controller.image("avatars", "../" + NAME).getStatusCode().value());
        var response = controller.image("campaigns", NAME);
        assertEquals(302, response.getStatusCode().value());
        assertTrue(response.getHeaders().getLocation().toString().contains("X-Amz-Signature="));
        assertEquals("no-store", response.getHeaders().getCacheControl());
    }

    @Test void deletingOnlyAcceptsOwnedGeneratedPaths() {
        storage.delete("https://example.com/a.png");
        storage.delete("/uploads/avatars/../comprovantes/" + NAME);
        verifyNoInteractions(client);
        storage.delete("/uploads/avatars/" + NAME);
        verify(client).deleteObject(argThat((DeleteObjectRequest r) ->
                r.bucket().equals("staging-test") && r.key().equals("avatars/" + NAME)));
    }

    @Test void invalidEndpointAndCredentialsFailConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new R2Config("http://example.com", "fake", "fake"));
        assertThrows(IllegalArgumentException.class, () -> new R2Config("https://test.r2.cloudflarestorage.com", "", "fake"));
    }
}
