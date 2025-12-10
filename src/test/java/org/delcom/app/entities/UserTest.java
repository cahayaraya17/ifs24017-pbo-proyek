package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private final UUID mockUserId = UUID.randomUUID();
    private final String mockName = "Test User";
    private final String mockEmail = "test@example.com";
    private final String mockPassword = "hashedpassword";

    // Test Default Constructor
    @Test
    void testDefaultConstructor() {
        User user = new User();
        assertNotNull(user);
        assertNull(user.getName());
    }

    // Test Constructor (email, password)
    @Test
    void testConstructor_emailPassword() {
        User user = new User(mockEmail, mockPassword);
        
        // Name diset ke "" (string kosong)
        assertEquals("", user.getName()); 
        assertEquals(mockEmail, user.getEmail());
        assertEquals(mockPassword, user.getPassword());
    }
    
    // Test Constructor (name, email, password)
    @Test
    void testConstructor_fullName() {
        User user = new User(mockName, mockEmail, mockPassword);
        
        assertEquals(mockName, user.getName());
        assertEquals(mockEmail, user.getEmail());
        assertEquals(mockPassword, user.getPassword());
    }

    // Test Getters and Setters
    @Test
    void testGettersAndSetters() {
        User user = new User();
        
        // Set values
        user.setId(mockUserId);
        user.setName(mockName);
        user.setEmail(mockEmail);
        user.setPassword(mockPassword);

        // Assert values
        assertEquals(mockUserId, user.getId());
        assertEquals(mockName, user.getName());
        assertEquals(mockEmail, user.getEmail());
        assertEquals(mockPassword, user.getPassword());
        
        // SerialVersionUID ada dan statis
        assertEquals(1L, User.serialVersionUID);
    }
    
    // Test JPA PrePersist Callback
    @Test
    void testOnCreateCallback() {
        User user = new User();
        
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
        
        // Panggil callback
        user.onCreate();
        
        assertNotNull(user.getCreatedAt(), "createdAt harus diset");
        assertNotNull(user.getUpdatedAt(), "updatedAt harus diset");
        // Dalam onCreate, createdAt dan updatedAt harus sama
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }
    
    // Test JPA PreUpdate Callback
    @Test
    void testOnUpdateCallback() throws InterruptedException {
        User user = new User();
        user.onCreate(); // Simulasikan persistensi awal
        
        LocalDateTime initialUpdatedAt = user.getUpdatedAt();
        
        // Tunggu sebentar untuk memastikan ada perbedaan waktu
        Thread.sleep(100); 
        
        // Panggil callback
        user.onUpdate();
        
        // Verifikasi updatedAt lebih baru
        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(initialUpdatedAt));
        // Verifikasi createdAt tidak berubah
        assertEquals(user.getCreatedAt(), initialUpdatedAt); 
    }
}