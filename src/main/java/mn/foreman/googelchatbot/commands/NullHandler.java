package mn.foreman.googelchatbot.commands;

/** Handles null pointer exceptions when commands are input. */
public class NullHandler
        implements CommandHandler {

    @Override
    public String handle(
            final String rest,
            final String spaceId) {
        return "Invalid command";
    }

}
