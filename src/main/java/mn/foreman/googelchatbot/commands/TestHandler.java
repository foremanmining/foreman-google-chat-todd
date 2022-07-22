package mn.foreman.googelchatbot.commands;

import mn.foreman.api.ForemanApi;
import mn.foreman.api.endpoints.ping.Ping;
import mn.foreman.googelchatbot.session.Session;
import mn.foreman.googelchatbot.session.SessionRepository;
import mn.foreman.googelchatbot.utils.ForemanUtils;

import com.vdurmont.emoji.EmojiParser;

import java.util.Optional;

/**
 * The handler for the test command. Allows users to check connectivity to the
 * Foreman Api
 */
public class TestHandler
        implements CommandHandler {

    /** The Url for the Foreman Api */
    private final String foremanApiUrl;

    /** Where the {@link Session} is stored */
    private final SessionRepository sessionRepository;

    /**
     * The constructor for the slash command handler.
     *
     * @param foremanApiUrl     The user's foreman api url.
     * @param sessionRepository The repository for the current session.
     */
    public TestHandler(
            final String foremanApiUrl,
            final SessionRepository sessionRepository) {
        this.foremanApiUrl = foremanApiUrl;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public String handle(
            String rest,
            String spaceId) {

        final StringBuilder messageBuilder = new StringBuilder();

        final Optional<Session> sessionOpt =
                this.sessionRepository.findBySpaceId(spaceId);

        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();

            final ForemanApi foremanApi =
                    ForemanUtils
                            .toApi(session.getClientId(),
                                    session.getApiKey(),
                                    this.foremanApiUrl);
            final Ping ping = foremanApi.ping();
            if (ping.ping()) {
                messageBuilder
                        .append("*Connectivity to Foreman:* :white_check_mark:\n");
            } else {
                messageBuilder
                        .append("*Connectivity to Foreman:* :x:\n");
            }

            if (ping.pingClient()) {
                messageBuilder.append("*Authentication with your API credentials:* :white_check_mark:\n");
            } else {
                messageBuilder.append("*Authentication with your API credentials:* :x:\n");
            }
        } else {
            messageBuilder.append("We haven't met yet...");
        }
        return EmojiParser.parseToUnicode(messageBuilder.toString());
    }
}

