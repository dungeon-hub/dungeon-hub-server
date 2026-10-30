package me.taubsie.dungeonhub.server.changes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.taubsie.dungeonhub.server.DungeonHubServerApplication;
import me.taubsie.dungeonhub.server.config.ConfigService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Connects real clients to the change stream and checks that changes made through the REST API reach exactly the
 * clients that are allowed to see them.
 */
@SpringBootTest(classes = DungeonHubServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:change-stream-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=test",
        "spring.datasource.password=test"
})
class ChangeStreamIntegrationTest {
    private static final Duration EVENT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration NO_EVENT_TIMEOUT = Duration.ofSeconds(2);

    private static final long SERVER_ID = 1001L;
    private static final long OTHER_SERVER_ID = 1002L;
    private static final long CLAIMER_ID = 2001L;
    private static final long NEW_CLAIMER_ID = 2002L;

    private static final Map<String, Jwt> TOKENS = Map.of(
            "bot", jwt("bot", Map.of("groups", List.of("bot"))),
            "manager", jwt("manager", Map.of("permissions", List.of("server_" + SERVER_ID))),
            "other-manager", jwt("other-manager", Map.of("permissions", List.of("server_" + OTHER_SERVER_ID))),
            "claimer", jwt("claimer", Map.of("discord-id", CLAIMER_ID)),
            "new-claimer", jwt("new-claimer", Map.of("discord-id", NEW_CLAIMER_ID))
    );

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final List<EventStream> streams = new ArrayList<>();

    @LocalServerPort
    private int port;

    @Autowired
    private ChangeStreamService changeStreamService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeAll
    static void createConfigFile() {
        // required by the application.properties of the server
        ConfigService.ensureConfigFile();
    }

    @BeforeEach
    void mockTokens() {
        when(jwtDecoder.decode(anyString())).thenAnswer(invocation -> Optional.ofNullable(TOKENS.get(invocation.<String>getArgument(0)))
                .orElseThrow(() -> new BadJwtException("Unknown token")));
    }

    @AfterEach
    void closeStreams() {
        streams.forEach(EventStream::close);
    }

    @Test
    void staticMessageChangesReachTheBotAndTheManagersOfTheServer() throws Exception {
        EventStream bot = connect("bot", "");
        EventStream manager = connect("manager", "");
        EventStream otherManager = connect("other-manager", "");
        EventStream ticketsOnly = connect("bot", "?types=Ticket");

        JsonObject staticMessage = request("POST", "/api/v1/server/" + SERVER_ID + "/static-message", """
                {"channelId": 5, "staticMessageType": "TicketPanel", "objectIds": [], "embedOverride": null}""");

        for (EventStream stream : List.of(bot, manager)) {
            Event event = stream.next();
            assertEquals("StaticMessage", event.name());
            assertEquals("StaticMessage", event.data().get("type").getAsString());
            assertEquals("Created", event.data().get("action").getAsString());
            assertEquals(SERVER_ID, event.data().get("serverId").getAsLong());
            assertEquals(staticMessage, event.data().getAsJsonObject("data"));
        }

        otherManager.assertNoEvent();
        ticketsOnly.assertNoEvent();

        request("PUT", "/api/v1/server/" + SERVER_ID + "/static-message/" + staticMessage.get("id").getAsLong(), """
                {"active": false}""");

        Event update = manager.next();
        assertEquals("Updated", update.data().get("action").getAsString());
        assertFalse(update.data().getAsJsonObject("data").get("active").getAsBoolean());
    }

    @Test
    void ticketChangesReachTheCurrentAndThePreviousClaimer() throws Exception {
        JsonObject ticketPanel = request("POST", "/api/v1/server/" + SERVER_ID + "/ticket-panel", """
                {"name": "carries", "closeable": true, "closeConfirmation": false, "claimable": true, "requiresLinking": false}""");
        String ticketPath = "/api/v1/server/" + SERVER_ID + "/ticket-panel/" + ticketPanel.get("id").getAsLong() + "/ticket";

        EventStream claimer = connect("claimer", "");
        EventStream newClaimer = connect("new-claimer", "");
        EventStream otherManager = connect("other-manager", "");

        JsonObject ticket = request("POST", ticketPath, """
                {"state": "Open", "user": 3001, "claimer": %d}""".formatted(CLAIMER_ID));

        Event created = claimer.next();
        assertEquals("Ticket", created.name());
        assertEquals("Created", created.data().get("action").getAsString());
        assertEquals(ticket.get("id"), created.data().getAsJsonObject("data").get("id"));
        newClaimer.assertNoEvent();

        request("PUT", ticketPath + "/" + ticket.get("id").getAsLong(), """
                {"claimer": %d}""".formatted(NEW_CLAIMER_ID));

        for (EventStream stream : List.of(claimer, newClaimer)) {
            Event event = stream.next();
            assertEquals("Updated", event.data().get("action").getAsString());
            assertEquals(NEW_CLAIMER_ID, event.data().getAsJsonObject("data").getAsJsonObject("claimer").get("id").getAsLong());
        }

        otherManager.assertNoEvent();
    }

    @Test
    void unauthenticatedClientsCantConnect() throws Exception {
        HttpResponse<Void> response = httpClient.send(HttpRequest.newBuilder(uri("/api/v1/changes")).build(), HttpResponse.BodyHandlers.discarding());

        assertEquals(401, response.statusCode());
    }

    @Test
    void theStreamIsClosedOnceTheTokenExpires() throws Exception {
        Jwt expiringToken = jwt("expiring", Map.of("groups", List.of("bot")), Instant.now().plusSeconds(2));
        // like the real decoder, expired tokens are rejected
        doAnswer(invocation -> {
            if (Instant.now().isAfter(expiringToken.getExpiresAt())) {
                throw new BadJwtException("Token expired");
            }
            return expiringToken;
        }).when(jwtDecoder).decode("expiring");

        EventStream stream = connect("expiring", "");

        assertTrue(stream.awaitClosed(EVENT_TIMEOUT), "The stream wasn't closed after the token expired.");
    }

    @Test
    void heartbeatsKeepTheConnectionAlive() throws Exception {
        EventStream stream = connect("bot", "");

        changeStreamService.sendHeartbeat();

        assertEquals(":heartbeat", stream.nextLine(line -> line.startsWith(":")));
    }

    private EventStream connect(String token, String query) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/v1/changes" + query))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "text/event-stream")
                .build();

        HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElseThrow().startsWith("text/event-stream"));

        EventStream stream = new EventStream(response.body());
        streams.add(stream);
        // the initial comment marks the point from which on all changes are received
        assertEquals(":connected", stream.nextLine(line -> line.startsWith(":")));
        return stream;
    }

    private JsonObject request(String method, String path, String body) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder(uri(path))
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .header("Authorization", "Bearer bot")
                .header("Content-Type", "application/json")
                .build(), HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), response.body());
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private static Jwt jwt(String token, Map<String, Object> claims) {
        return jwt(token, claims, Instant.now().plus(Duration.ofHours(1)));
    }

    private static Jwt jwt(String token, Map<String, Object> claims, Instant expiresAt) {
        return Jwt.withTokenValue(token)
                .header("alg", "none")
                .subject(token)
                .issuedAt(Instant.now())
                .expiresAt(expiresAt)
                .claims(existing -> existing.putAll(claims))
                .build();
    }

    private record Event(String name, JsonObject data) {
    }

    /**
     * Reads the lines of a server-sent event stream in the background.
     */
    private static final class EventStream {
        private static final String END_OF_STREAM = "\u0000end";

        private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();
        private final Stream<String> body;

        EventStream(Stream<String> body) {
            this.body = body;
            Thread reader = new Thread(() -> {
                try {
                    body.forEach(lines::add);
                } catch (RuntimeException ignored) {
                    // the stream was closed
                }
                lines.add(END_OF_STREAM);
            });
            reader.setDaemon(true);
            reader.start();
        }

        Event next() throws InterruptedException {
            String name = nextLine(line -> line.startsWith("event:")).substring("event:".length());
            String data = nextLine(line -> line.startsWith("data:")).substring("data:".length());

            return new Event(name, JsonParser.parseString(data).getAsJsonObject());
        }

        String nextLine(java.util.function.Predicate<String> filter) throws InterruptedException {
            long deadline = System.nanoTime() + EVENT_TIMEOUT.toNanos();

            while (System.nanoTime() < deadline) {
                String line = lines.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                if (line == null || line.equals(END_OF_STREAM)) {
                    break;
                }
                if (filter.test(line)) {
                    return line;
                }
            }

            return fail("Didn't receive the expected line.");
        }

        void assertNoEvent() throws InterruptedException {
            long deadline = System.nanoTime() + NO_EVENT_TIMEOUT.toNanos();

            while (System.nanoTime() < deadline) {
                String line = lines.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                if (line != null && line.startsWith("event:")) {
                    fail("Received an unexpected event: " + line);
                }
            }
        }

        boolean awaitClosed(Duration timeout) throws InterruptedException {
            long deadline = System.nanoTime() + timeout.toNanos();

            while (System.nanoTime() < deadline) {
                String line = lines.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                if (END_OF_STREAM.equals(line)) {
                    return true;
                }
            }

            return false;
        }

        void close() {
            body.close();
        }
    }
}
