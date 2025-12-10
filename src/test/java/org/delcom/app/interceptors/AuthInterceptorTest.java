package org.delcom.app.interceptors;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils; // Untuk set/get private methods/fields

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// Gunakan MockitoExtension untuk mengaktifkan anotasi Mock
@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private AuthContext authContext;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private final UUID mockUserId = UUID.randomUUID();
    private final String validToken = "valid.jwt.token";
    private final User mockUser = new User("Test", "test@user.com", "pass");
    private final AuthToken mockAuthToken = new AuthToken(mockUserId, validToken);

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        
        // Atur mockUser ID karena digunakan oleh AuthToken
        mockUser.setId(mockUserId);
    }
    
    // Helper untuk memverifikasi respon error (sendErrorResponse)
    private void verifyErrorResponse(int status, String message) throws Exception {
        assertEquals(status, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("\"status\":\"fail\""));
        assertTrue(response.getContentAsString().contains(message));
    }

    // --- 1. SUCCESS PATH ---
    @Test
    void preHandle_validTokenAndUser_shouldReturnTrueAndSetAuthContext() throws Exception {
        // Setup Header Request
        request.addHeader("Authorization", "Bearer " + validToken);
        
        // Mock static method JwtUtil
        try (MockedStatic<JwtUtil> mockedJwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            // 1. Validasi token: TRUE
            mockedJwtUtil.when(() -> JwtUtil.validateToken(eq(validToken), anyBoolean())).thenReturn(true);
            // 2. Ekstrak User ID: mockUserId
            mockedJwtUtil.when(() -> JwtUtil.extractUserId(eq(validToken))).thenReturn(mockUserId);

            // 3. Cari token di DB: Ditemukan
            when(authTokenService.findUserToken(mockUserId, validToken)).thenReturn(mockAuthToken);
            // 4. Ambil data User: Ditemukan
            when(userService.getUserById(mockUserId)).thenReturn(mockUser);

            // Eksekusi
            boolean result = authInterceptor.preHandle(request, response, new Object());

            // Verifikasi
            assertTrue(result, "Harus mengembalikan true untuk akses sukses");
            verify(authContext, times(1)).setAuthUser(eq(mockUser));
            verify(response, never()).setStatus(anyInt());
        }
    }

    // --- 2. BRANCH: isPublicEndpoint (Skipping Auth) ---
    @Test
    void preHandle_publicEndpoint_shouldReturnTrueAndSkipAuth() throws Exception {
        // Case 1: /api/auth path
        request.setRequestURI("/api/auth/login");
        
        boolean result = authInterceptor.preHandle(request, response, new Object());
        
        assertTrue(result, "Harus mengembalikan true untuk endpoint public");
        // Verifikasi bahwa tidak ada logika autentikasi yang dijalankan
        verify(authContext, never()).setAuthUser(any());
        verify(authTokenService, never()).findUserToken(any(), any());
        
        // Case 2: /error path
        request.setRequestURI("/error");
        result = authInterceptor.preHandle(request, response, new Object());
        assertTrue(result, "Harus mengembalikan true untuk endpoint /error");
    }

    // --- 3. BRANCH: Token is null or empty (Token not found) ---
    @Test
    void preHandle_missingAuthHeader_shouldReturnFalseAndSend401() throws Exception {
        // Case 1: Header tidak ada
        // request.removeHeader("Authorization"); // Default is null
        
        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verifyErrorResponse(401, "Token autentikasi tidak ditemukan");
    }
    
    @Test
    void preHandle_invalidTokenFormat_shouldReturnFalseAndSend401() throws Exception {
        // Case 2: Header ada, tapi tanpa "Bearer "
        request.addHeader("Authorization", validToken); 
        
        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verifyErrorResponse(401, "Token autentikasi tidak ditemukan");
    }

    // --- 4. BRANCH: Invalid JWT format (`JwtUtil.validateToken` returns false) ---
    @Test
    void preHandle_jwtValidationFailed_shouldReturnFalseAndSend401() throws Exception {
        request.addHeader("Authorization", "Bearer invalid.jwt.token");
        
        try (MockedStatic<JwtUtil> mockedJwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            // Validasi token: FALSE
            mockedJwtUtil.when(() -> JwtUtil.validateToken(any(), anyBoolean())).thenReturn(false);

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertFalse(result);
            verifyErrorResponse(401, "Token autentikasi tidak valid");
            verify(authContext, never()).setAuthUser(any());
        }
    }
    
    // --- 5. BRANCH: Missing UserId in Token (`JwtUtil.extractUserId` returns null) ---
    @Test
    void preHandle_userIdExtractionFailed_shouldReturnFalseAndSend401() throws Exception {
        request.addHeader("Authorization", "Bearer valid.token.without.userid");
        
        try (MockedStatic<JwtUtil> mockedJwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            // Validasi token: TRUE
            mockedJwtUtil.when(() -> JwtUtil.validateToken(any(), anyBoolean())).thenReturn(true);
            // Ekstrak User ID: NULL
            mockedJwtUtil.when(() -> JwtUtil.extractUserId(any())).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertFalse(result);
            verifyErrorResponse(401, "Format token autentikasi tidak valid");
        }
    }
    
    // --- 6. BRANCH: Token not found in DB (`authTokenService.findUserToken` returns null) ---
    @Test
    void preHandle_tokenExpiredOrRevoked_shouldReturnFalseAndSend401() throws Exception {
        request.addHeader("Authorization", "Bearer " + validToken);

        try (MockedStatic<JwtUtil> mockedJwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwtUtil.when(() -> JwtUtil.validateToken(eq(validToken), anyBoolean())).thenReturn(true);
            mockedJwtUtil.when(() -> JwtUtil.extractUserId(eq(validToken))).thenReturn(mockUserId);
            
            // Cari token di DB: TIDAK DITEMUKAN (null)
            when(authTokenService.findUserToken(mockUserId, validToken)).thenReturn(null); 

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertFalse(result);
            verifyErrorResponse(401, "Token autentikasi sudah expired");
            verify(userService, never()).getUserById(any());
        }
    }

    // --- 7. BRANCH: User not found in DB (`userService.getUserById` returns null) ---
    @Test
    void preHandle_userNotFound_shouldReturnFalseAndSend404() throws Exception {
        request.addHeader("Authorization", "Bearer " + validToken);

        try (MockedStatic<JwtUtil> mockedJwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwtUtil.when(() -> JwtUtil.validateToken(eq(validToken), anyBoolean())).thenReturn(true);
            mockedJwtUtil.when(() -> JwtUtil.extractUserId(eq(validToken))).thenReturn(mockUserId);
            
            when(authTokenService.findUserToken(mockUserId, validToken)).thenReturn(mockAuthToken);
            // Ambil data User: TIDAK DITEMUKAN (null)
            when(userService.getUserById(mockUserId)).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertFalse(result);
            // Menguji cabang 404
            verifyErrorResponse(404, "User tidak ditemukan"); 
            verify(authContext, never()).setAuthUser(any());
        }
    }
}