package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // Memuat MockMvc untuk menguji Controller
class CustomErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void handleError_shouldReturnNotFound_for404() throws Exception {
        // Simulasikan request yang menghasilkan 404 dan dialihkan ke /error
        mockMvc.perform(get("/error") 
                        // Atribut yang biasanya diset oleh DispatcherServlet saat terjadi 404
                        .requestAttr("jakarta.servlet.error.status_code", 404)
                        .requestAttr("jakarta.servlet.error.request_uri", "/nonexistent"))
                .andExpect(status().isNotFound()) // Harus mengembalikan status 404
                .andExpect(jsonPath("$.status", is("fail"))) // status: fail
                .andExpect(jsonPath("$.error", is("Not Found"))) // error default 404
                .andExpect(jsonPath("$.path", is("/nonexistent")))
                .andExpect(jsonPath("$.message", is("Endpoint tidak ditemukan atau terjadi error")))
                .andExpect(jsonPath("$.timestamp")).exists();
    }

    @Test
    void handleError_shouldReturnInternalServerError_for500() throws Exception {
        // Simulasikan request yang menghasilkan 500 dan dialihkan ke /error
        mockMvc.perform(get("/error")
                        .requestAttr("jakarta.servlet.error.status_code", 500)
                        .requestAttr("jakarta.servlet.error.request_uri", "/api/data/internal"))
                .andExpect(status().isInternalServerError()) // Harus mengembalikan status 500
                .andExpect(jsonPath("$.status", is("error"))) // status: error
                .andExpect(jsonPath("$.error", is("Internal Server Error"))) // error default 500
                .andExpect(jsonPath("$.path", is("/api/data/internal")))
                .andExpect(jsonPath("$.message", is("Endpoint tidak ditemukan atau terjadi error")));
    }
}