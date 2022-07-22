package mn.foreman.googelchatbot.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/** Utility that allows us to parse Json payloads to get strings */
public class EventUtils {

    /**
     * Utility function to obtain an attribute from the provided json.
     *
     * @param event the json payload.
     * @param path  the string indicating the information.
     *
     * @return an Optional<String> with the desired information.
     */
    public static Optional<String> getAttribute(
            final JsonNode event,
            final String path) {
        final JsonNode result = event.at(path);
        if (result != null) {
            return Optional.ofNullable(result.asText());
        }
        return Optional.empty();
    }
}
