package mn.foreman.googelchatbot.commands;

/** Gives the user a description of the other available slash commands. */
public class HelpHandler
        implements CommandHandler {

    @Override
    public String handle(
            final String rest,
            final String spaceId) {
        return "Sure... here's what I can do for ya:\n\n" +
                "*/start:*\n" +
                "Begins the bot setup process.\n\n" +
                "*/register:*\n" +
                "Registers the bot with new API credentials. Notifications will be sent to the channel where the registration was performed.\n\n" +
                "*/forget:*\n" +
                "Stops the bot from notifying you.\n\n" +
                "*/test:*\n" +
                "Tests connectivity with the Foreman API.";
    }
}
