package mn.foreman.googelchatbot.config;

import mn.foreman.googelchatbot.commands.*;
import mn.foreman.googelchatbot.session.GoogleStorageRepository;
import mn.foreman.googelchatbot.session.SessionRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/** The configuration for the Foreman Google Chat *TM* bot */
@Configuration
public class BotConfig {

    @Bean
    public Bucket bucket(
            @Value("${google.bucket}") final String bucketName,
            final Storage storage) {
        return storage.get(bucketName);
    }

    @Bean
    public Map<String, CommandHandler> commandHandlers(
            @Value("${foreman.baseUrl}") final String dashboardUrl,
            @Value("${foreman.apiUrl}") final String apiUrl,
            final SessionRepository sessionRepository) {
        return ImmutableMap.of(
                "/start",
                new StartHandler(
                        dashboardUrl),
                "/register",
                new RegisterHandler(
                        apiUrl,
                        dashboardUrl,
                        sessionRepository),
                "/test",
                new TestHandler(
                        apiUrl,
                        sessionRepository),
                "/forget",
                new ForgetHandler(
                        sessionRepository),
                "/help",
                new HelpHandler());
    }

    @Bean
    public CommandRouter commandRouter(final Map<String, CommandHandler> commandMap) {
        return new CommandRouter(commandMap);
    }

    @Bean
    public GoogleCredentials googleCredentials(
            @Value("${google.credentialOauthScope}") final String credentials,
            @Value("${google.resourceStream}") final String resourceStream)
            throws IOException {

        File serviceAccount = new File(resourceStream).getAbsoluteFile();

        return GoogleCredentials
                .fromStream(new FileInputStream(serviceAccount))
                .createScoped(credentials);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .registerModule(new JavaTimeModule());
    }

    @Bean
    public HttpRequestInitializer requestInitializer(
            @Value("${google.chatScope}") final String chatScope,
            @Value("${google.resourceStream}") final String resources)
            throws IOException {

        File serviceAccount = new File(resources);
        GoogleCredentials credentials =
                GoogleCredentials
                        .fromStream(new FileInputStream(serviceAccount))
                        .createScoped(chatScope);
        return new HttpCredentialsAdapter(credentials);
    }

    @Bean
    public SessionRepository sessionRepository(
            final Bucket bucket,
            final ObjectMapper objectMapper) {
        return new GoogleStorageRepository(
                objectMapper,
                bucket);
    }

    @Bean
    public Instant startTime() {
        return Instant.now();
    }

    @Bean
    public Storage storage(final GoogleCredentials googleCredentials) {
        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise, credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        return StorageOptions
                .newBuilder()
                .setCredentials(googleCredentials)
                .build()
                .getService();
    }
}
