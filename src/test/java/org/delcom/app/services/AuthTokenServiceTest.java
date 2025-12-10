package org.delcom.app.services;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    // Mock Repository
    @Mock
    private AuthTokenRepository authTokenRepository;

    // Inject the Mocks into the Service
    @InjectMocks
    private AuthTokenService authTokenService;
    
    // Data dummy
    private final UUID mockUserId = UUID.randomUUID();
    private final String mockTokenString = "mock-jwt-token";
    private final AuthToken mockAuthToken = new AuthToken(mockUserId, mockTokenString);

    // --- 1. Test findUserToken ---
    @Test
    void findUserToken_shouldCallRepositoryAndReturnToken() {
        // Setup: Ketika repository dipanggil, kembalikan mockAuthToken
        when(authTokenRepository.findUserToken(mockUserId, mockTokenString)).thenReturn(mockAuthToken);

        // Eksekusi
        AuthToken result = authTokenService.findUserToken(mockUserId, mockTokenString);

        // Verifikasi
        assertNotNull(result);
        assertEquals(mockAuthToken, result);
        
        // Verifikasi bahwa metode repository dipanggil sekali dengan parameter yang benar
        verify(authTokenRepository, times(1)).findUserToken(mockUserId, mockTokenString);
    }
    
    @Test
    void findUserToken_tokenNotFound_shouldReturnNull() {
        // Setup: Ketika repository dipanggil, kembalikan null
        when(authTokenRepository.findUserToken(any(UUID.class), anyString())).thenReturn(null);

        // Eksekusi
        AuthToken result = authTokenService.findUserToken(UUID.randomUUID(), "nonexistent-token");

        // Verifikasi
        assertNull(result);
    }

    // --- 2. Test createAuthToken ---
    @Test
    void createAuthToken_shouldCallRepositorySaveAndReturnSavedToken() {
        // Setup: Ketika repository.save dipanggil, kembalikan objek yang sama
        when(authTokenRepository.save(mockAuthToken)).thenReturn(mockAuthToken);

        // Eksekusi
        AuthToken result = authTokenService.createAuthToken(mockAuthToken);

        // Verifikasi
        assertNotNull(result);
        assertEquals(mockAuthToken, result);
        
        // Verifikasi bahwa metode repository.save dipanggil sekali dengan objek yang benar
        verify(authTokenRepository, times(1)).save(mockAuthToken);
    }

    // --- 3. Test deleteAuthToken ---
    @Test
    void deleteAuthToken_shouldCallRepositoryDeleteByUserId() {
        // Setup: Metode deleteByUserId adalah void, jadi tidak perlu `when`
        doNothing().when(authTokenRepository).deleteByUserId(mockUserId);
        
        // Eksekusi
        authTokenService.deleteAuthToken(mockUserId);

        // Verifikasi
        // Verifikasi bahwa metode repository.deleteByUserId dipanggil sekali dengan UUID yang benar
        verify(authTokenRepository, times(1)).deleteByUserId(mockUserId);
    }
}