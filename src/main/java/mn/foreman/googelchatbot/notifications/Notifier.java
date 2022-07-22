package mn.foreman.googelchatbot.notifications;

import mn.foreman.googelchatbot.session.Session;
import mn.foreman.googelchatbot.session.SessionRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpRequestInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/** This class works to send notifications to the user. */
@Component
public class Notifier {

    /** The logger for this class. */
    private static final Logger LOG =
            LoggerFactory.getLogger(Notifier.class);

    /**
     * This is the Api url for the user. It's important to note that this is
     * distinct from the foreman dashboard URL.
     */
    private final String foremanApiUrl;

    /** The max number of notifications the user will receive at once. */
    @Value("${notifications.max}")
    private final int maxNotifications;


    /** The mapper. */
    private final ObjectMapper objectMapper;

    /** This is the state repository used to help maintain state/session. */
    private final SessionRepository sessionRepository;

    /**
     * The time that the first notification is set off. Distinct from the time
     * that the user registers.
     */
    private final Instant startTime;

    /** Completes the oauth to send notifications to the Google space. */
    private final HttpRequestInitializer requestInitializer;

    /**
     * Constructor for the notifier. It calls {@link NotificationProcessorImpl}.
     *
     * @param foremanApiUrl     the foreman API url.
     * @param maxNotifications  the max number of notifications to send to
     *                          the user.
     * @param objectMapper      the object mapper.
     * @param startTime         the time that the user registered.
     * @param sessionRepository the session repository.
     */
    public Notifier(
            @Value("${foreman.apiUrl}") final String foremanApiUrl,
            @Value("${notifications.max}") final int maxNotifications,
            final ObjectMapper objectMapper,
            final Instant startTime,
            final SessionRepository sessionRepository,
            final HttpRequestInitializer requestInitializer) {
        this.foremanApiUrl = foremanApiUrl;
        this.maxNotifications = maxNotifications;
        this.objectMapper = objectMapper;
        this.startTime = startTime;
        this.sessionRepository = sessionRepository;
        this.requestInitializer = requestInitializer;
    }

    /**
     * Periodically sends notifications to the user.
     *
     * @throws IOException on failure.
     */
    @Scheduled(
            initialDelayString = "${bot.check.initialDelay}",
            fixedDelayString = "${bot.check.fixedDelay}")
    public void sendNotifications()
            throws IOException {
        final List<Session> sessions =
                sessionRepository.findAll();

        final NotificationsProcessor notificationProcessor =
                new NotificationProcessorImpl(
                        this.foremanApiUrl,
                        this.maxNotifications,
                        this.objectMapper,
                        this.startTime,
                        this.requestInitializer);
        LOG.info("Looking for notifications for {} sessions", sessions.size());

        // Makes sure the list of states is non-empty.
        if (!sessions.isEmpty()) {
            sessions
                    .parallelStream()
                    .forEach(
                            session ->
                            {
                                try {
                                    notificationProcessor.process(
                                            session,
                                            this.sessionRepository);
                                } catch (IOException e){
                                    LOG.error("Something really bad happened", e);
                                }
                    });
        }
    }
}
