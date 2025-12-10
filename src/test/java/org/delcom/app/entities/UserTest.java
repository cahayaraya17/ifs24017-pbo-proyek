package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private final String NAME = "John Doe";
    private final String EMAIL = "john.doe@example.com";
    private final String PASSWORD = "securepassword";

    @Test
    void testNoArgsConstructor() {
        User user = new User();
        assertNotNull(user);
        assertNull(user.getId());
    }

    @Test
    void testEmailPasswordConstructor() {
        User user = new User(EMAIL, PASSWORD);
        assertEquals("", user.getName());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(PASSWORD, user.getPassword());
    }

    @Test
    void testAllArgsConstructor() {
        User user = new User(NAME, EMAIL, PASSWORD);
        assertEquals(NAME, user.getName());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(PASSWORD, user.getPassword());
    }

    @Test
    void testGettersAndSetters() {
        User user = new User();
        UUID id = UUID.randomUUID();

        user.setId(id);
        user.setName(NAME);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);

        assertEquals(id, user.getId());
        assertEquals(NAME, user.getName());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(PASSWORD, user.getPassword());
    }

    @Test
    void testPrePersist() {
        User user = new User(NAME, EMAIL, PASSWORD);
        // Kita bisa panggil onCreate manual karena package-nya sama
        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    @Test
    void testPreUpdate() throws InterruptedException {
        User user = new User(NAME, EMAIL, PASSWORD);
        user.onCreate();

        LocalDateTime initialCreatedAt = user.getCreatedAt();
        LocalDateTime initialUpdatedAt = user.getUpdatedAt();

        // Jeda sedikit agar waktu berubah
        Thread.sleep(50); 

        user.onUpdate();

        assertEquals(initialCreatedAt, user.getCreatedAt()); // CreatedAt tidak boleh berubah
        assertNotEquals(initialUpdatedAt, user.getUpdatedAt()); // UpdatedAt harus berubah
        assertTrue(user.getUpdatedAt().isAfter(initialUpdatedAt));
    }
}