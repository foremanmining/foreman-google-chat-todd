package mn.foreman.googelchatbot.commands;

import java.io.IOException;

/** The {@link  CommandHandler} handles various slash commands. */
public interface CommandHandler {

    /**
     * Handles incoming slash commands
     *
     * @param rest    Any input after the initial slash command. This is only
     *                really used for the register command.
     * @param spaceId The space where the json payload is coming from.
     *
     * @return Returns a string response to be sent to google chat.
     *
     * @throws IOException on failure.
     */
    String handle(
            String rest,
            String spaceId)
            throws IOException;
}
