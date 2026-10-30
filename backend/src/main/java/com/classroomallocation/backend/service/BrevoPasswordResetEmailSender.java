package com.classroomallocation.backend.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(
        name = "app.mail.provider",
        havingValue = "brevo"
)
public class BrevoPasswordResetEmailSender
        implements PasswordResetEmailSender {

    private final String apiKey;
    private final RestClient client;

    public BrevoPasswordResetEmailSender(
            @Value("${app.mail.brevo-api-key}") String apiKey
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Set BREVO_API_KEY for email delivery."
            );
        }

        this.apiKey = apiKey;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(httpClient);

        factory.setReadTimeout(Duration.ofSeconds(10));

        this.client = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Override
    public void send(SimpleMailMessage message) {
        String[] recipients = message.getTo();

        if (recipients == null
                || recipients.length != 1
                || message.getFrom() == null
                || message.getFrom().isBlank()
                || message.getSubject() == null
                || message.getText() == null) {
            throw new IllegalArgumentException(
                    "A complete password reset email is required."
            );
        }

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "email", message.getFrom(),
                        "name", "Classroom Allocator"
                ),
                "to", List.of(
                        Map.of("email", recipients[0])
                ),
                "subject", message.getSubject(),
                "textContent", message.getText()
        );

        client.post()
                .uri("https://api.brevo.com/v3/smtp/email")
                .header("api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            throw new IllegalStateException(
                                    "Email delivery provider rejected the request."
                            );
                        }
                )
                .toBodilessEntity();
    }
}