package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testApiResponse_success() {
        String status = "success";
        String message = "Operation successful";
        Integer data = 123;

        ApiResponse<Integer> response = new ApiResponse<>(status, message, data);

        assertEquals(status, response.getStatus(), "Status harus sesuai");
        assertEquals(message, response.getMessage(), "Pesan harus sesuai");
        assertEquals(data, response.getData(), "Data harus sesuai");
    }
    
    @Test
    void testApiResponse_nullData() {
        String status = "fail";
        String message = "Validation failed";

        // Data null harus ditangani (sesuai @JsonInclude(JsonInclude.Include.NON_NULL))
        ApiResponse<Void> response = new ApiResponse<>(status, message, null); 

        assertEquals(status, response.getStatus(), "Status harus sesuai");
        assertEquals(message, response.getMessage(), "Pesan harus sesuai");
        assertNull(response.getData(), "Data harus null");
    }
}