package mn.foreman.googelchatbot.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The Google storage repository for the bot. */
public class GoogleStorageRepository
        implements SessionRepository {

    /** The Google Cloud Bucket */
    private final Bucket bucket;

    /** The object mapper for the Bot */
    private final ObjectMapper objectMapper;

    /**
     * The constructor for the Google storage repository.
     *
     * @param objectMapper The object mapper for the bot.
     * @param bucket       The bucket where the session info will be stored.
     */
    public GoogleStorageRepository(
            final ObjectMapper objectMapper,
            final Bucket bucket) {
        this.objectMapper = objectMapper;
        this.bucket = bucket;
    }

    @Override
    public void delete(String spaceId) {
        String blobName = formatBlobName(spaceId);
        if (this.bucket.get(blobName) != null) {
            this.bucket
                    .get(blobName)
                    .delete();
        }
    }

    @Override
    public List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();

        for (Blob blob : this.bucket.list().iterateAll()) {
            Optional<Session> oneSessionOptional = getSessionFromBlob(blob);

            if (oneSessionOptional.isPresent()) {
                Session oneSession = oneSessionOptional.get();
                sessions.add(oneSession);
            }
        }
        return sessions;
    }

    @Override
    public Optional<Session> findBySpaceId(
            final String spaceId) {
        Session session = null;

        String blobName = formatBlobName(spaceId);
        Blob current = this.bucket.get(blobName);

        Optional<Session> sessionOpt = getSessionFromBlob(current);
        if (sessionOpt.isPresent()) {
            session = sessionOpt.get();
        }
        return Optional.ofNullable(session);
    }

    @Override
    public void make(
            final String spaceId,
            final int clientId,
            final String apiKey,
            final Instant dateRegistered)
            throws IOException {

        String blobName = formatBlobName(spaceId);

        // We need to store the Session information here, so we create and
        // store it.
        Session session =
                Session
                        .builder()
                        .apiKey(apiKey)
                        .dateRegistered(dateRegistered)
                        .clientId(clientId)
                        .spaceId(spaceId)
                        .build();
        // This creates and stores the blob.
        this.bucket.create(
                blobName,
                this.objectMapper.writeValueAsBytes(session),
                ContentType.APPLICATION_JSON.getMimeType());
    }

    @Override
    public void save(
            String spaceId,
            Session session)
            throws IOException {
        String blobName = formatBlobName(spaceId);
        if (this.bucket.get(blobName) != null) {
            delete(spaceId);
            this.bucket.create(
                    blobName,
                    this.objectMapper.writeValueAsBytes(session));
        }
    }

    /**
     * Method used to create the blob name using the space id it gets rid of the
     * spaces/ part and adds .json to the end.
     *
     * @param spaceId the space where the user added the bot.
     *
     * @return returns the formatted {@link Blob} name as a string.
     */
    private String formatBlobName(final String spaceId) {
        // All spaces look like the above space/space_name
        final String[] preBlobName = spaceId
                .replace('/', ' ')
                .split(" ");

        String bucketName = preBlobName[1]
                .toLowerCase();

        return bucketName + ".json";
    }

    /**
     * This method is for getting the session back from blob content.
     *
     * @param blob a blob containing session data.
     *
     * @return a {@link Session}.
     */
    private Optional<Session> getSessionFromBlob(Blob blob) {
        try {
            return Optional.ofNullable(this.objectMapper.readValue(
                    blob.getContent(),
                    Session.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
