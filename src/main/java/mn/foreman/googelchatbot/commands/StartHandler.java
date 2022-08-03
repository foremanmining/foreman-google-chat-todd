package mn.foreman.googelchatbot.commands;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Value;

/**
 *  Give the user basic information for how to get notifications sent to a
 *  Google chat space.
 */
public class StartHandler
        implements CommandHandler {

    /** Foreman Dashboard Url. */
    private final String foremanDashboardUrl;

    /**
     * The constructor for the start command handler.
     *
     * @param foremanDashboardUrl The user's foreman dashboard URL.
     */
    public StartHandler(
            @Value("${foreman.baseUrl}") final String foremanDashboardUrl) {
        this.foremanDashboardUrl = foremanDashboardUrl;
    }

    @Override
    public String handle(
            final String rest,
            final String spaceId) {
        return buildResponse();
    }

    /**
     * Method for building the response for start that directs the user to their
     * foreman dashboard.
     *
     * @return String with the response.
     */
    private String buildResponse() {
        final String response =
                "Hello! I'm *Todd*, the Foreman Slack notification bot. :wave: \n" +
                        "\n" +
                        String.format(
                                "Based on <%s/dashboard/triggers/|triggers> you create on your dashboard, I'll send you notifications when things happen.\n",
                                this.foremanDashboardUrl) +
                        "\n" +
                        "Let's get introduced:\n" +
                        "\n" +
                        String.format(
                                "1. Go <%s/dashboard/profile/|here> get your *client id* and *API key*\n",
                                this.foremanDashboardUrl) +
                        "2. Once you have them, run: `/register <client_id> <api_key>`\n" +
                        "3. That's it! :beers: Then I'll send your notifications to this channel.\n" +
                        "\n" +
                        "If you want them to happen somewhere else, re-run " +
                        "the register above in the channel where you want to be notified.";
        return EmojiParser.parseToUnicode(response);
    }
}
