package org.delcom.app.services;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthTokenServiceTest {

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private AuthTokenService authTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindUserToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = "jwt.token";
        AuthToken authToken = new AuthToken(userId, token);
        when(authTokenRepository.findUserToken(userId, token)).thenReturn(authToken);

        // When
        AuthToken found = authTokenService.findUserToken(userId, token);

        // Then
        assertNotNull(found);
        assertEquals(userId, found.getUserId());
        assertEquals(token, found.getToken());
        verify(authTokenRepository, times(1)).findUserToken(userId, token);
    }

    @Test
    void testCreateAuthToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = "jwt.token";
        AuthToken authToken = new AuthToken(userId, token);
        when(authTokenRepository.save(authToken)).thenReturn(authToken);

        // When
        AuthToken created = authTokenService.createAuthToken(authToken);

        // Then
        assertNotNull(created);
        assertEquals(userId, created.getUserId());
        assertEquals(token, created.getToken());
        verify(authTokenRepository, times(1)).save(authToken);
    }

    @Test
    void testDeleteAuthToken() {
        // Given
        UUID userId = UUID.randomUUID();

        // When
        authTokenService.deleteAuthToken(userId);

        // Then
        verify(authTokenRepository, times(1)).deleteByUserId(userId);
    }
}
