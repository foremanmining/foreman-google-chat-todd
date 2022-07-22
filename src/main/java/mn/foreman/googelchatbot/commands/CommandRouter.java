package mn.foreman.googelchatbot.commands;

import mn.foreman.googelchatbot.utils.EventUtils;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/** A class for responding to different events. */
public class CommandRouter {

    /** Map of all possible command handlers. */
    private final Map<String, CommandHandler> commandHandlers;

    /**
     * Constructor for the class.
     *
     * @param commandHandlers the map of all handlers.
     */
    public CommandRouter(
            final Map<String, CommandHandler> commandHandlers) {
        this.commandHandlers = commandHandlers;
    }

    /**
     * Method for handling slash commands.
     *
     * @param event is the Json payload.
     *
     * @throws IOException on failure.
     */
    public Optional<String> messageEvent(
            final JsonNode event)
            throws IOException {
        String response = "";
        String rest = "";
        String text = "";
        String spaceId = "";
        final Optional<String> textOpt = EventUtils.getAttribute(
                event,
                "/message/text");
        final Optional<String> spaceIdOpt = EventUtils.getAttribute(
                event,
                "/space/name");
        if (textOpt.isPresent()) {
            text = textOpt.get();
        }
        if (spaceIdOpt.isPresent()) {
            spaceId = spaceIdOpt.get();
        }

        if (text.startsWith("/")) {
            final String[] regions = text.split(" ", 2);
            final String command = regions[0];
            if (regions.length > 1) {
                rest = regions[1];
            }

            final CommandHandler handler =
                    this.commandHandlers
                            .getOrDefault(
                                    command,
                                    new NullHandler());
            response = handler
                    .handle(
                            rest,
                            spaceId);
        }
            return Optional.ofNullable(response);
    }
}
