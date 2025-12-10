package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit; // Import penting
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
        
        // Panggil onCreate secara manual
        photo.onCreate();

        assertNotNull(photo.getCreatedAt());
        assertNotNull(photo.getUpdatedAt());
        
        // 1. Pastikan created dan updated identik saat pembuatan awal
        assertEquals(photo.getCreatedAt(), photo.getUpdatedAt());

        // 2. [PERBAIKAN] Validasi Rentang Waktu
        // Memastikan waktu pembuatan adalah "barusan" (toleransi 1 detik)
        LocalDateTime now = LocalDateTime.now();
        long diff = ChronoUnit.SECONDS.between(photo.getCreatedAt(), now);

        assertTrue(Math.abs(diff) < 1, "CreatedAt harusnya waktu sekarang (toleransi 1 detik)");
    }

    @Test
    void testPreUpdate() throws InterruptedException {
        Photo photo = new Photo(USER_ID, TITLE, CATEGORY, DESCRIPTION, PRICE);
        photo.onCreate(); // Set waktu awal

        LocalDateTime initialCreatedAt = photo.getCreatedAt();
        LocalDateTime initialUpdatedAt = photo.getUpdatedAt();

        // Jeda waktu agar terlihat perbedaannya (50ms cukup)
        Thread.sleep(50);

        photo.onUpdate(); // Update waktu

        // Validasi
        assertEquals(initialCreatedAt, photo.getCreatedAt(), "CreatedAt tidak boleh berubah");
        assertNotEquals(initialUpdatedAt, photo.getUpdatedAt(), "UpdatedAt harus berubah");
        
        // Pastikan updated baru lebih 'tua' (after) dari updated lama
        assertTrue(photo.getUpdatedAt().isAfter(initialUpdatedAt), "UpdatedAt baru harus setelah yang lama");
    }
}