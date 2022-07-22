package mn.foreman.googelchatbot.commands;

import mn.foreman.api.ForemanApi;
import mn.foreman.api.endpoints.ping.Ping;
import mn.foreman.googelchatbot.session.SessionRepository;
import mn.foreman.googelchatbot.utils.ForemanUtils;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;

/** Allows the user to register with the foreman Api. */
public class RegisterHandler
        implements CommandHandler {

    /** The logger for this the Register handler. */
    private static final Logger LOG = LoggerFactory.getLogger(RegisterHandler.class);

    /** URl for Foreman API. */
    private final String foremanApiUrl;

    /** URL for foreman dashboard. */
    private final String foremanDashboardUrl;

    /** The Google cloud storage repository. */
    private final SessionRepository sessionRepository;

    /**
     * The constructor for the register command
     *
     * @param foremanApiUrl       The Api Url for the User.
     * @param foremanDashboardUrl The User's foreman dashboard URL.
     * @param sessionRepository   The session repository.
     */
    public RegisterHandler(
            final String foremanApiUrl,
            final String foremanDashboardUrl,
            final SessionRepository sessionRepository) {
        this.foremanDashboardUrl = foremanDashboardUrl;
        this.foremanApiUrl = foremanApiUrl;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public String handle(
            final String rest,
            final String spaceId) {
        try {
            return buildResponse(
                    rest,
                    spaceId);
        } catch (final Exception e) {
            LOG.error("Something really bad happened with registering", e);
            return "Something didn't go right with registering";
        }
    }

    /**
     * This method checks that the users credentials were correct/correctly
     * input.
     *
     * @param spaceId   Information about the users chat space event payload.
     * @param splitArgs The user's client id and api key as an array.
     *
     * @return A string containing a verification if the input is valid. If not
     *         then the string says what went wrong.
     */
    private String applyValidArguments(
            final String spaceId,
            final String[] splitArgs)
            throws IOException {
        final String outPutArgs;

        final String clientIdCandidate = splitArgs[0];

        // Here we do a quick check to ensure that the client id is an int
        // before moving on.
        if (NumberUtils.isCreatable(clientIdCandidate)) {
            final int clientId = Integer.parseInt(clientIdCandidate);
            final String apiKey = splitArgs[1];
            final ForemanApi foremanApi = ForemanUtils.toApi(clientId, apiKey, this.foremanApiUrl);
            final Ping ping = foremanApi.ping();

            if (ping.pingClient()) {

                // Does the same thing as handle success in discord. Adds
                // state and sends the user a confirmation message. Builds
                // the session and google repository and adds the client id,
                // api key, and space id to the session/state.
                this.sessionRepository
                        .make(
                                spaceId,
                                clientId,
                                apiKey,
                                Instant.now()
                        );
                // Concatenate the confirmation text.
                outPutArgs =
                        "Those look correct! Setup complete :white_check_mark:\n" +
                                "\n" +
                                String.format(
                                        "You'll get notified based on your *alert* <%s/dashboard/triggers/|triggers>, so make sure you created some and set their destination to Slack.\n",
                                        this.foremanDashboardUrl) +
                                "\n" +
                                "If you've already done this, you should be good to go! :thumbsup:";
            } else {
                outPutArgs =
                        "I tried those, but they didn't work. Please " +
                                "re-input the register command followed by your client Id and api key to try again";
            }
        } else {
            // Log the invalid number and return a message to the user telling
            // them their input was wrong.
            LOG.warn("Number not provided: {}", clientIdCandidate);
            outPutArgs = "Sorry, client ID should have been a number. Please try again.";
        }
        return EmojiParser.parseToUnicode(outPutArgs);
    }

    /**
     * This method handles all registration matters.
     *
     * @param rest    Whatever follows the register command in Google Chat
     *                expected to be the client id followed by the Foreman Api
     *                key.
     * @param spaceId The space where the slash command was carried out.
     *
     * @return A String with the output.
     *
     * @throws IOException on failure.
     */
    private String buildResponse(
            final String rest,
            final String spaceId)
            throws IOException {
        String output = "";

        // Split on spaces, get first (client ID), get second (API key),
        // split the string on spaces and handle the < and > characters.
        // The >< handles the case where the client doesn't include a space
        // between their client id and the Api key.
        if (rest != null && !rest.isBlank()) {
            final String[] splitArgs =
                    rest
                            .replace("><", " ")
                            .replace("<", "")
                            .replace(">", " ")
                            .split(" ");

            // This deletes the old Session so they can re-register.
            if (this.sessionRepository.findBySpaceId(spaceId).isPresent()) {
                sessionRepository.delete(spaceId);
            }

            if (splitArgs.length >= 2) {
                output =
                        applyValidArguments(
                                spaceId,
                                splitArgs);
            } else {
                output = "Sorry something isn't right. You should input <clientId> followed by <API key>";
            }
        } else {
            output = "Sorry something isn't right. You should input <clientId> followed by <API key>";
        }
        return output;
    }
}
