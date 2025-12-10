package org.delcom.app.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterFormTest {

    @Test
    void testGettersAndSetters() {
        RegisterForm form = new RegisterForm();
        
        // Test Setters
        form.setName("John Doe");
        form.setEmail("john@example.com");
        form.setPassword("securepass");

        // Test Getters
        assertEquals("John Doe", form.getName());
        assertEquals("john@example.com", form.getEmail());
        assertEquals("securepass", form.getPassword());
        
        // Test default constructor state
        RegisterForm defaultForm = new RegisterForm();
        assertNull(defaultForm.getName());
    }
}