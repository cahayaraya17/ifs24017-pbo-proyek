package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SecurityConfig securityConfig; // Untuk menguji bean

    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;

    @Test
    void passwordEncoderBean_isLoaded() {
        // Memastikan bean PasswordEncoder terdaftar
        assertNotNull(passwordEncoder);
    }
    
    @Test
    void securityFilterChain_isConfigured() {
        // Memastikan SecurityFilterChain dapat dibuat
        assertNotNull(securityConfig.securityFilterChain(context.getBean(org.springframework.security.config.annotation.web.builders.HttpSecurity.class)));
    }
    
    @Test
    void unauthorizedAccess_toProtectedPath_shouldRedirect() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Path yang dilindungi (anyRequest().authenticated())
        mockMvc.perform(get("/home")) 
               .andExpect(status().isFound()) // Status 302 (Found)
               .andExpect(redirectedUrl("http://localhost/auth/login")); // Logika exceptionHandling
    }
    
    // Untuk 100% coverage, Anda harus menguji logika aset statis:
    @Test
    void unauthorizedAccess_toStaticAsset_shouldReturn404() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // Aset statis yang dilarang redirect (di exceptionHandling)
        mockMvc.perform(get("/css/style.css")) 
               .andExpect(status().isNotFound()); // Status 404
    }
}