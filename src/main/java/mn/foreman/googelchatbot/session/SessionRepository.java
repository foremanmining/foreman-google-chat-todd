package mn.foreman.googelchatbot.session;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** The storage repository for the {@link Session}. */
public interface SessionRepository {

    /**
     * Deletes a blob from the bucket.
     *
     * @param spaceId the space that the user is communicating from
     */
    void delete(String spaceId);

    /**
     * Finds all sessions
     *
     * @return returns a list of all (@link Session}s
     *
     * @throws IOException on failure.
     */
    List<Session> findAll() throws IOException;

    /**
     * Finds the Session by using the space id as a key.
     *
     * @param spaceId the space where the user is interacting with the bot.
     *
     * @return returns an optional of a session.
     */
    Optional<Session> findBySpaceId(String spaceId);

    /**
     * Makes a new blob and the corresponding session to be stored there then
     * stores the blob in the bucket
     *
     * @param spaceId        the space from which the user registered.
     * @param clientId       the client id obtained from foreman.
     * @param apiKey         the apiKey obtained from foreman.
     * @param dateRegistered the date the user registered.
     *
     * @throws IOException throws an IO exception
     */
    void make(
            String spaceId,
            int clientId,
            String apiKey,
            Instant dateRegistered) throws IOException;

    /**
     * Saves an updated session with an updated last notification id. the blob
     * has to be deleted then recreated because the update method from Google
     * doesn't work correctly
     *
     * @param spaceId the space where the user bot is added.
     * @param session the {@link Session}.
     *
     * @throws IOException throws an IO exception.
     */
    void save(
            String spaceId,
            Session session) throws IOException;

}
