package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PhotoTest {

    private final UUID USER_ID = UUID.randomUUID();
    private final String TITLE = "Test Photo";
    private final String CATEGORY = "Landscape";
    private final String DESCRIPTION = "A beautiful landscape";
    private final BigDecimal PRICE = new BigDecimal("100.00");

    @Test
    void testNoArgsConstructor() {
        Photo photo = new Photo();
        assertNotNull(photo);
        assertNull(photo.getId());
    }

    @Test
    void testAllArgsConstructor() {
        Photo photo = new Photo(USER_ID, TITLE, CATEGORY, DESCRIPTION, PRICE);
        assertEquals(USER_ID, photo.getUserId());
        assertEquals(TITLE, photo.getTitle());
        assertEquals(CATEGORY, photo.getCategory());
        assertEquals(DESCRIPTION, photo.getDescription());
        assertEquals(PRICE, photo.getPrice());
    }

    @Test
    void testGettersAndSetters() {
        Photo photo = new Photo();
        UUID id = UUID.randomUUID();

        photo.setId(id);
        photo.setUserId(USER_ID);
        photo.setTitle(TITLE);
        photo.setCategory(CATEGORY);
        photo.setDescription(DESCRIPTION);
        photo.setPrice(PRICE);
        photo.setFilename("test.jpg");

        assertEquals(id, photo.getId());
        assertEquals(USER_ID, photo.getUserId());
        assertEquals(TITLE, photo.getTitle());
        assertEquals(CATEGORY, photo.getCategory());
        assertEquals(DESCRIPTION, photo.getDescription());
        assertEquals(PRICE, photo.getPrice());
        assertEquals("test.jpg", photo.getFilename());
    }

    @Test
    void testPrePersist() {
        Photo photo = new Photo(USER_ID, TITLE, CATEGORY, DESCRIPTION, PRICE);
        // Call onCreate manually
        photo.onCreate();

        assertNotNull(photo.getCreatedAt());
        assertNotNull(photo.getUpdatedAt());
        assertEquals(photo.getCreatedAt(), photo.getUpdatedAt());
    }

    @Test
    void testPreUpdate() throws InterruptedException {
        Photo photo = new Photo(USER_ID, TITLE, CATEGORY, DESCRIPTION, PRICE);
        photo.onCreate();

        LocalDateTime initialCreatedAt = photo.getCreatedAt();
        LocalDateTime initialUpdatedAt = photo.getUpdatedAt();

        // Sleep to ensure time difference
        Thread.sleep(50);

        photo.onUpdate();

        assertEquals(initialCreatedAt, photo.getCreatedAt()); // CreatedAt should not change
        assertNotEquals(initialUpdatedAt, photo.getUpdatedAt()); // UpdatedAt should change
        assertTrue(photo.getUpdatedAt().isAfter(initialUpdatedAt));
    }
}
