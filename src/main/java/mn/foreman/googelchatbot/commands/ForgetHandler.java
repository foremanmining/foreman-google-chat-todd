package mn.foreman.googelchatbot.commands;

import mn.foreman.googelchatbot.session.Session;
import mn.foreman.googelchatbot.session.SessionRepository;

import java.util.Optional;

/**
 * Allows the user to de-register with the bot to stop getting notifications
 * sent to a channel. Only stops notifications to the channel in which this
 * command is used. Other channels still receive the notifications.
 */
public class ForgetHandler
        implements CommandHandler {

    /** The Google cloud storage repository for the {@link Session}. */
    private final SessionRepository sessionRepository;

    /**
     * Constructor for the forget command.
     *
     * @param sessionRepository repository for the {@link Session}.
     */
    public ForgetHandler(final SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public String handle(
            String rest,
            String spaceId) {
        try {
            return buildResponse(spaceId);
        } catch (Exception e) {
            return "That didn't work";
        }
    }

    /**
     * Handles the actual forgetting and makes a string indicating success or
     * stating that the user was never registered.
     *
     * @param spaceId The id of the space the user wants forgotten.
     *
     * @return A string response to be parsed to json and sent to the space.
     */
    private String buildResponse(String spaceId) {

        final Optional<Session> sessionOpt =
                this.sessionRepository.findBySpaceId(spaceId);
        if (sessionOpt.isPresent()) {
            this.sessionRepository.delete(spaceId);
            return "Got it - I won't send you notifications anymore";
        } else {
            // In this case they haven't done the register step yet.
            return "I don't think we've met...";
        }
    }
}
