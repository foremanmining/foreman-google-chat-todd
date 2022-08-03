package mn.foreman.googelchatbot.notifications;

import mn.foreman.googelchatbot.session.Session;
import mn.foreman.googelchatbot.session.SessionRepository;

import java.io.IOException;

public interface NotificationsProcessor {

    /**
     * Obtains notifications for the provided session and notifies the chat, as
     * necessary.
     *
     * @param session Provides the users credentials.
     * @param sessionRepository the repository for sessions.
     *
     * @throws IOException on failure.
     */
    void process(
            Session session,
            SessionRepository sessionRepository) throws IOException;
}
