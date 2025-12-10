package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AuthTokenTest {

    private final UUID mockUserId = UUID.randomUUID();
    private final UUID mockTokenId = UUID.randomUUID();
    private final String mockToken = "mock_auth_token_string";

    // Test Default Constructor
    @Test
    void testDefaultConstructor() {
        AuthToken token = new AuthToken();
        assertNotNull(token);
        assertNull(token.getId());
        assertNull(token.getToken());
        assertNull(token.getUserId());
        assertNull(token.getCreatedAt());
    }

    // Test Parameterized Constructor
    @Test
    void testParameterizedConstructor() {
        AuthToken token = new AuthToken(mockUserId, mockToken);
        
        assertEquals(mockUserId, token.getUserId());
        assertEquals(mockToken, token.getToken());
        assertNotNull(token.getCreatedAt(), "createdAt harus diset di constructor");
    }

    // Test Getters and Setters
    @Test
    void testGettersAndSetters() {
        AuthToken token = new AuthToken();
        
        // Set values
        token.setId(mockTokenId);
        token.setUserId(mockUserId);
        token.setToken(mockToken);

        // Assert values
        assertEquals(mockTokenId, token.getId());
        assertEquals(mockUserId, token.getUserId());
        assertEquals(mockToken, token.getToken());
    }
    
    // Test JPA PrePersist Callback
    @Test
    void testOnCreateCallback() throws InterruptedException {
        AuthToken token = new AuthToken();
        
        assertNull(token.getCreatedAt());

        // Tunggu sebentar untuk memastikan ada perbedaan waktu
        Thread.sleep(10); 
        LocalDateTime beforeCreate = LocalDateTime.now();
        
        // Panggil callback secara manual
        token.onCreate(); 
        
        assertNotNull(token.getCreatedAt());
        // Verifikasi bahwa createdAt diset setelah "sebelum" waktu pemanggilan
        assertTrue(token.getCreatedAt().isAfter(beforeCreate) || token.getCreatedAt().isEqual(beforeCreate));
    }
}