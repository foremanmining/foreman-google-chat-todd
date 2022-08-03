package mn.foreman.googelchatbot.session;

import lombok.*;

import java.time.Instant;

/** A {@link Session} represents a session for each registered chat Id */
@Data
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    /** The clients Foreman Api key. */
    private String apiKey;

    /** The users Foreman clientId. */
    private int clientId;

    /** The date that the client registered. */
    private Instant dateRegistered;

    /** The last notification id. */
    private int lastNotificationId;

    /** The id of the space that the user is using. */
    private String spaceId;
}
