package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenTest {

    private final UUID USER_ID = UUID.randomUUID();
    private final String TOKEN = "sample.jwt.token";

    @Test
    void testNoArgsConstructor() {
        AuthToken authToken = new AuthToken();
        assertNotNull(authToken);
        assertNull(authToken.getId());
    }

    @Test
    void testAllArgsConstructor() {
        AuthToken authToken = new AuthToken(USER_ID, TOKEN);
        assertEquals(USER_ID, authToken.getUserId());
        assertEquals(TOKEN, authToken.getToken());
        assertNotNull(authToken.getCreatedAt());
    }

    @Test
    void testGettersAndSetters() {
        AuthToken authToken = new AuthToken();
        UUID id = UUID.randomUUID();

        authToken.setId(id);
        authToken.setUserId(USER_ID);
        authToken.setToken(TOKEN);

        assertEquals(id, authToken.getId());
        assertEquals(USER_ID, authToken.getUserId());
        assertEquals(TOKEN, authToken.getToken());
    }

    @Test
    void testPrePersist() {
        AuthToken authToken = new AuthToken(USER_ID, TOKEN);
        // Call onCreate manually
        authToken.onCreate();

        assertNotNull(authToken.getCreatedAt());
    }
}
