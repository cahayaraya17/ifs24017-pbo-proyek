package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit; // Tambahkan import ini
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
        assertEquals("", user.getName()); // Pastikan logic di entity set default empty string
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
        
        // Panggil method lifecycle callback manual
        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        
        // 1. Pastikan created dan updated sama persis saat pertama kali dibuat
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());

        // 2. [PERBAIKAN UTAMA] Validasi Rentang Waktu
        // Cek apakah createdAt benar-benar "baru saja" (selisih < 1 detik dari sekarang)
        LocalDateTime now = LocalDateTime.now();
        long diff = ChronoUnit.SECONDS.between(user.getCreatedAt(), now);
        
        // Menggunakan Math.abs untuk menangani kemungkinan millisecond drift
        assertTrue(Math.abs(diff) < 1, "CreatedAt harusnya waktu sekarang (toleransi 1 detik)");
    }

    @Test
    void testPreUpdate() throws InterruptedException {
        User user = new User(NAME, EMAIL, PASSWORD);
        user.onCreate(); // Set awal

        LocalDateTime initialCreatedAt = user.getCreatedAt();
        LocalDateTime initialUpdatedAt = user.getUpdatedAt();

        // Pause sebentar untuk memastikan ada beda waktu (50ms cukup aman)
        Thread.sleep(50); 

        user.onUpdate(); // Update waktu

        // Validasi
        assertEquals(initialCreatedAt, user.getCreatedAt(), "CreatedAt tidak boleh berubah saat update");
        assertNotEquals(initialUpdatedAt, user.getUpdatedAt(), "UpdatedAt harus berubah");
        
        // Pastikan updated yang baru lebih "tua" (after) dari yang lama
        assertTrue(user.getUpdatedAt().isAfter(initialUpdatedAt), "UpdatedAt baru harus setelah yang lama");
    }
}