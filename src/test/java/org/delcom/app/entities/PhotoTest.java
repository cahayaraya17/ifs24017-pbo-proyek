package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PhotoTest {

    private final UUID mockUserId = UUID.randomUUID();
    private final UUID mockPhotoId = UUID.randomUUID();
    private final BigDecimal mockPrice = new BigDecimal("150.50");

    // Test Default Constructor
    @Test
    void testDefaultConstructor() {
        Photo photo = new Photo();
        assertNotNull(photo);
        assertNull(photo.getTitle());
    }

    // Test Parameterized Constructor
    @Test
    void testParameterizedConstructor() {
        Photo photo = new Photo(
                mockUserId, 
                "Sunset View", 
                "Landscape", 
                "A vivid description", 
                mockPrice
        );
        
        assertEquals(mockUserId, photo.getUserId());
        assertEquals("Sunset View", photo.getTitle());
        assertEquals("Landscape", photo.getCategory());
        assertEquals("A vivid description", photo.getDescription());
        assertEquals(mockPrice, photo.getPrice());
        
        assertNull(photo.getFilename());
        assertNull(photo.getCreatedAt());
        assertNull(photo.getUpdatedAt());
    }

    // Test Getters and Setters
    @Test
    void testGettersAndSetters() {
        Photo photo = new Photo();
        
        // Set values
        photo.setId(mockPhotoId);
        photo.setUserId(mockUserId);
        photo.setTitle("New Title");
        photo.setCategory("Abstract");
        photo.setDescription("Short Desc");
        photo.setPrice(mockPrice);
        photo.setFilename("img_001.jpg");

        // Assert values
        assertEquals(mockPhotoId, photo.getId());
        assertEquals(mockUserId, photo.getUserId());
        assertEquals("New Title", photo.getTitle());
        assertEquals("Abstract", photo.getCategory());
        assertEquals("Short Desc", photo.getDescription());
        assertEquals(mockPrice, photo.getPrice());
        assertEquals("img_001.jpg", photo.getFilename());
    }

    // Test JPA PrePersist Callback
    @Test
    void testOnCreateCallback() throws InterruptedException {
        Photo photo = new Photo();
        
        assertNull(photo.getCreatedAt());
        assertNull(photo.getUpdatedAt());

        // Panggil callback
        photo.onCreate(); 
        
        assertNotNull(photo.getCreatedAt(), "createdAt harus diset");
        assertNotNull(photo.getUpdatedAt(), "updatedAt harus diset");
        // Dalam onCreate, createdAt dan updatedAt harus sama
        assertEquals(photo.getCreatedAt(), photo.getUpdatedAt());
    }
    
    // Test JPA PreUpdate Callback
    @Test
    void testOnUpdateCallback() throws InterruptedException {
        Photo photo = new Photo();
        photo.onCreate(); // Simulasikan persistensi awal
        
        LocalDateTime initialUpdatedAt = photo.getUpdatedAt();
        
        // Tunggu sebentar untuk memastikan ada perbedaan waktu
        Thread.sleep(100); 
        
        // Panggil callback
        photo.onUpdate();
        
        // Verifikasi updatedAt lebih baru
        assertNotNull(photo.getUpdatedAt());
        assertTrue(photo.getUpdatedAt().isAfter(initialUpdatedAt));
        // Verifikasi createdAt tidak berubah
        assertEquals(photo.getCreatedAt(), initialUpdatedAt); 
    }
}