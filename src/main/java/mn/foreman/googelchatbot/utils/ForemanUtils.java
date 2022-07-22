package mn.foreman.googelchatbot.utils;

import mn.foreman.api.ForemanApi;
import mn.foreman.api.ForemanApiImpl;
import mn.foreman.api.JdkWebUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

/** Utilities for interacting with the Foreman API. */
public class ForemanUtils {

    /**
     * Returns a new {@link ForemanApi} handler.
     *
     * @param clientId       the client ID.
     * @param apiKey         the client API key.
     * @param foremanBaseUrl the Foreman base URL.
     *
     * @return the new API handler.
     */
    public static ForemanApi toApi(
            final int clientId,
            final String apiKey,
            final String foremanBaseUrl) {
        return new ForemanApiImpl(
                Integer.toString(clientId),
                "",
                new ObjectMapper(),
                new JdkWebUtil(
                        foremanBaseUrl,
                        apiKey,
                        5,
                        TimeUnit.SECONDS));
    }
}
