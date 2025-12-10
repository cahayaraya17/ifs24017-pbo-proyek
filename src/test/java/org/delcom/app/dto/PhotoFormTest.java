package org.delcom.app.dto;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PhotoFormTest {

    @Test
    void testGettersAndSetters() {
        PhotoForm form = new PhotoForm();
        
        // Data
        BigDecimal price = new BigDecimal("99.99");
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());

        // Test Setters
        form.setTitle("My Test Photo");
        form.setCategory("Nature");
        form.setDescription("A beautiful landscape");
        form.setPrice(price);
        form.setFile(file);

        // Test Getters
        assertEquals("My Test Photo", form.getTitle());
        assertEquals("Nature", form.getCategory());
        assertEquals("A beautiful landscape", form.getDescription());
        assertEquals(price, form.getPrice());
        assertEquals(file, form.getFile());
        
        // Test default constructor state
        PhotoForm defaultForm = new PhotoForm();
        assertNull(defaultForm.getTitle());
        assertNull(defaultForm.getFile());
    }
}