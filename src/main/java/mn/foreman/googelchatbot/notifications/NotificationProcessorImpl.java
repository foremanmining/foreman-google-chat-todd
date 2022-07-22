package mn.foreman.googelchatbot.notifications;

import mn.foreman.api.ForemanApi;
import mn.foreman.api.ForemanApiImpl;
import mn.foreman.api.JdkWebUtil;
import mn.foreman.api.endpoints.notifications.Notifications;
import mn.foreman.googelchatbot.session.Session;
import mn.foreman.googelchatbot.session.SessionRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.chat.v1.HangoutsChat;
import com.google.api.services.chat.v1.model.Message;
import com.google.common.collect.Iterables;
import com.vdurmont.emoji.EmojiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link NotificationsProcessor} implementation that sends
 * markdown-formatted messages to the provided chat based on the session that's
 * to be notified.
 */
@Component
public class NotificationProcessorImpl
        implements NotificationsProcessor {

    /** The logger for this class. */
    private static final Logger LOG =
            LoggerFactory.getLogger(NotificationProcessorImpl.class);

    /** Base URl for Foreman. */
    private final String foremanDashboardUrl;

    /** The max notifications to send at once. */
    private final int maxNotifications;

    /** The mapper. */
    private final ObjectMapper objectMapper;

    /** Http request initializer for the class. */
    private final HttpRequestInitializer requestInitializer;

    /** The bot start time. */
    private final Instant startTime;

    public NotificationProcessorImpl(
            @Value("${foreman.baseUrl}") String foremanDashboardUrl,
            @Value("${notifications.max}") int maxNotifications,
            final ObjectMapper objectMapper,
            final Instant startTime,
            final HttpRequestInitializer requestInitializer) {
        this.foremanDashboardUrl = foremanDashboardUrl;
        this.maxNotifications = maxNotifications;
        this.objectMapper = objectMapper;
        this.startTime = startTime;
        this.requestInitializer = requestInitializer;
    }

    @Override
    public void process(
            Session session,
            SessionRepository sessionRepository)
            throws IOException {

        final String spaceId = session.getSpaceId();

        final ForemanApi foremanApi =
                makeApi(
                        session);

        final Notifications notificationsApi =
                foremanApi.notifications();

        final Instant registered = session.getDateRegistered();

        //Check the notification time against the time the user registered.
        final List<Notifications.Notification> notifications =
                notificationsApi.googleChat(
                        session.getLastNotificationId(),
                        registered.isAfter(this.startTime)
                                ? registered
                                : this.startTime);

        LOG.info("Session {} has {} pending notifications",
                session,
                notifications);
        if (!notifications.isEmpty()) {
            sendExistingNotifications(
                    sessionRepository,
                    session,
                    spaceId,
                    notifications);
        }
    }

    /**
     * Adds the miners information and link to the notification sent.
     *
     * @param failingMiner  this is the individual miner that failed for the
     *                      user on foreman.
     * @param stringBuilder java string builder.
     */
    private void appendMiner(
            final Notifications.Notification.FailingMiner failingMiner,
            final StringBuilder stringBuilder) {
        stringBuilder
                .append(
                        String.format(
                                // If there is an issue with the output,
                                // this is where it would most likely be
                                "<%s/dashboard/miners/%d/details/|%s>",
                                this.foremanDashboardUrl,
                                failingMiner.minerId,
                                failingMiner.miner))
                .append("\n");
        failingMiner
                .diagnosis
                .forEach(
                        diag ->
                                stringBuilder
                                        .append(diag)
                                        .append("\n"));
        stringBuilder
                .append("\n");
    }

    /**
     * Creates the foreman Api Url This is the same as lines 95-104 in foreman
     * discord NotificationsProcessorImpl just done in a separate method.
     *
     * @return A new {@link ForemanApi} authenticated with the data from the
     *         provided {@link Session}.
     */
    private ForemanApi makeApi(final Session session) {
        // Initializing the client id.
        final String clientId = Integer.toString(session.getClientId());

        // Returning the desired ForemanApiImpl object.
        return new ForemanApiImpl(
                clientId,
                "",
                this.objectMapper,
                new JdkWebUtil(
                        this.foremanDashboardUrl,
                        session.getApiKey(),
                        5,
                        TimeUnit.SECONDS));
    }

    /**
     * This method builds a string of notifications and sends it to the user's
     * Google chat space.
     *
     * @param sessionRepository The repository.
     * @param session           The current {@link Session} for the user.
     * @param spaceId           The space ID.
     * @param notifications     The actual notification that is being sent to
     *                          the user.
     *
     * @throws IOException on failure.
     */
    private void sendExistingNotifications(
            final SessionRepository sessionRepository,
            final Session session,
            final String spaceId,
            final List<Notifications.Notification> notifications)
            throws IOException {

        LOG.info("Building notification message for {}", session);
        notifications
                .stream()
                .map(this::toNotificationMessage)
                .forEach(message -> {
                    try {
                        sendMessage(
                                spaceId,
                                message);
                    } catch (final Exception e) {
                        LOG.warn("Exception occurred while notifying", e);
                    }
                });

        final Notifications.Notification lastNotification =
                Iterables.getLast(notifications);
        session.setLastNotificationId(lastNotification.id);
        sessionRepository.save(
                spaceId,
                session);
    }

    /**
     * Method to create and return messages to the User.
     *
     * @param spaceId The space where the bot is communicating with the User.
     * @param content The content of the notification.
     */
    private void sendMessage(
            final String spaceId,
            final String content) {

        try {
            final Message message = new Message();
            message.setText(content);
            // Gets the name of the space (or name of person who's privately
            // chatting with the bot.

            HangoutsChat chatService = new HangoutsChat.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    this.requestInitializer)
                    .setApplicationName("basic-async-bot-java")
                    .build();
            chatService
                    .spaces()
                    .messages()
                    .create(spaceId, message)
                    .execute();
        } catch (final Exception e) {
            LOG.error("Something really bad happened", e);
        }
    }

    /**
     * Converts the provided notification to a message to be sent.
     *
     * @param notification The notification to process.
     *
     * @return The message sent through the emoji parser. Necessary to get
     *         properly formatted emojis since Google doesn't do it
     *         automatically.
     */
    private String toNotificationMessage(
            final Notifications.Notification notification) {
        final StringBuilder messageBuilder =
                new StringBuilder();

        //write the subject
        messageBuilder.append(
                String.format(
                        "%s *%s*",
                        !notification.failingMiners.isEmpty()
                                ? ":x:"
                                : ":white_check_mark:",
                        notification.subject));

        final List<Notifications.Notification.FailingMiner> failingMiners =
                notification.failingMiners;

        if (!failingMiners.isEmpty()) {
            // Write the failing miners out as lists
            messageBuilder.append("\n\n");
            failingMiners
                    .stream()
                    .limit(this.maxNotifications)
                    .forEach(
                            miner ->
                                    appendMiner(
                                            miner,
                                            messageBuilder));

            if (failingMiners.size() > this.maxNotifications) {
                // Too many miners were failing if we get here
                messageBuilder
                        .append("\n\n")
                        .append(
                                String.format(
                                        "*...and %d more",
                                        failingMiners.size() - this.maxNotifications))

                        .append(
                                String.format(
                                        "Head to [your dashboard](%s/dashboard/) to see the rest",
                                        this.foremanDashboardUrl));
            }
        }
        return EmojiParser.parseToUnicode(messageBuilder.toString());
    }
}
