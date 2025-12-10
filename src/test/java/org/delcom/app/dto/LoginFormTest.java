package org.delcom.app.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginFormTest {

    @Test
    void testGettersAndSetters() {
        LoginForm form = new LoginForm();
        
        // Test Setters
        form.setEmail("test@example.com");
        form.setPassword("password123");
        form.setRememberMe(true);

        // Test Getters
        assertEquals("test@example.com", form.getEmail());
        assertEquals("password123", form.getPassword());
        assertTrue(form.isRememberMe());
        
        // Test default constructor state
        LoginForm defaultForm = new LoginForm();
        assertNull(defaultForm.getEmail());
        assertFalse(defaultForm.isRememberMe());
    }
}