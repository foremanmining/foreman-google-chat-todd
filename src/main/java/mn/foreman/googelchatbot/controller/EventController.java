package mn.foreman.googelchatbot.controller;

import mn.foreman.googelchatbot.commands.CommandRouter;
import mn.foreman.googelchatbot.utils.EventUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.chat.v1.HangoutsChat;
import com.google.api.services.chat.v1.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

/*
 * Controller for the commands serves as the entry point for events to
 * interact with the bot.
 */
@RestController
public class EventController {

    /** The logger for this class. */
    private static final Logger LOG =
            LoggerFactory.getLogger(EventController.class);

    /** The Command Router for the class. */
    private final CommandRouter commandRouter;

    /** Http request initializer for the class. */
    private final HttpRequestInitializer requestInitializer;

    /**
     * Constructor for the command router.
     *
     * @param commandRouter      The command router for the bot.
     * @param requestInitializer Makes the HTTP request for the bot.
     */
    public EventController(
            final CommandRouter commandRouter,
            final HttpRequestInitializer requestInitializer) {
        this.commandRouter = commandRouter;
        this.requestInitializer = requestInitializer;
    }

    @PostMapping("/")
    public void onEvent(
            @RequestBody final JsonNode event)
            throws IOException {
        String output = "";
        String type = "";
        Optional<String> typeOpt = EventUtils.getAttribute(event, "/type");
        if (typeOpt.isPresent()) {
            type = typeOpt.get();
            switch (type) {
                case "ADDED_TO_SPACE":
                    output = addedToSpaceReply(event);
                    doReply(
                            event,
                            output);
                    break;
                case "MESSAGE":
                    Optional<String> validCommandOpt = this.commandRouter
                            .messageEvent(event);
                    if (validCommandOpt.isPresent()) {
                        String validCommand = validCommandOpt.get();
                        doReply(event, validCommand);
                    } else {
                        String invalidResponse = "Hi, please type a slash command. If " +
                                "you're not sure what commands are available, type " +
                                "*/help* for a description of what I can do.";
                        doReply(event, invalidResponse);
                    }
                    break;
                case "REMOVED_FROM_SPACE":
                    String name = event
                            .at("/space/name")
                            .asText();
                    LOG.info(String.format("Bot removed from %s", name));
                    break;
            }
        } else {
            doReply(event, "Sorry, I'm not familiar with that command" +
                    "type */help* to see what commands are available. ");
        }
    }

    /**
     * Method for responding to being added to a space or a direct message.
     *
     * @param event Is the Json payload.
     *
     * @return The reply message.
     */
    private String addedToSpaceReply(final JsonNode event) {
        String addedReply = "";
        String spaceType = "";
        Optional<String> spaceOpt = EventUtils.getAttribute(
                event,
                "/space/type");
        if (spaceOpt.isPresent()) {
            spaceType = spaceOpt.get();

            if ("ROOM".equals(spaceType)) {
                String displayName = event
                        .at("/space/displayName")
                        .asText();
                addedReply = String
                        .format("Thanks for adding me to %s", displayName);
            } else {
                String displayName = event
                        .at("/user/displayName").asText();
                addedReply = String
                        .format("Thanks for adding me to a DM, %s!", displayName);
            }
        } else {
            addedReply = "Invalid space type. I can only work in a room or DM.";
        }

        return addedReply;
    }

    /**
     * Method to create and return messages to the User.
     *
     * @param event   The json event payload.
     * @param content String of what needs to be made into a message.
     */
    private void doReply(
            final JsonNode event,
            final String content) {
        try {
            final Message message = new Message();
            message.setText(content);
            // Gets the name of the space (or name of person who's privately
            // chatting with the bot.
            String spaceName = event.at("/space/name").asText();

            HangoutsChat chatService = new HangoutsChat.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    this.requestInitializer)
                    .setApplicationName("Foreman-Bot")
                    .build();
            chatService
                    .spaces()
                    .messages()
                    .create(spaceName, message)
                    .execute();
        } catch (final Exception e) {
            LOG.error("Something really bad happened", e);
        }
    }
}