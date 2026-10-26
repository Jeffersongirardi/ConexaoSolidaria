package com.conexoessolidarias.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Profile("prod")
public class R2Config {
    private final URI endpoint;
    private final StaticCredentialsProvider credentials;

    public R2Config(@Value("${app.storage.r2.endpoint}") String endpoint,
                    @Value("${app.storage.r2.access-key}") String accessKey,
                    @Value("${app.storage.r2.secret-key}") String secretKey) {
        URI uri = URI.create(endpoint);
        if (!"https".equals(uri.getScheme()) || uri.getHost() == null
                || !uri.getHost().endsWith(".r2.cloudflarestorage.com")
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                || uri.getPort() != -1 || !(uri.getPath().isEmpty() || "/".equals(uri.getPath()))) {
            throw new IllegalArgumentException("R2_ENDPOINT deve ser o endpoint S3 HTTPS da Cloudflare");
        }
        if (accessKey.isBlank() || secretKey.isBlank()) {
            throw new IllegalArgumentException("Credenciais R2 obrigatórias em prod");
        }
        this.endpoint = uri;
        this.credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    private S3Configuration s3Configuration() {
        return S3Configuration.builder().pathStyleAccessEnabled(true)
                .chunkedEncodingEnabled(false).build();
    }

    @Bean(destroyMethod = "close")
    public S3Client r2Client() {
        return S3Client.builder().endpointOverride(endpoint).region(Region.of("auto"))
                .credentialsProvider(credentials).serviceConfiguration(s3Configuration())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(20)))
                .overrideConfiguration(c -> c.apiCallTimeout(Duration.ofSeconds(30)))
                .build();
    }

    @Bean(destroyMethod = "close")
    public S3Presigner r2Presigner() {
        return S3Presigner.builder().endpointOverride(endpoint).region(Region.of("auto"))
                .credentialsProvider(credentials).serviceConfiguration(s3Configuration()).build();
    }
}
