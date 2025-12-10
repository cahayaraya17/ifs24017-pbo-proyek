package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthContextTest {

    @Test
    void testAuthContext() {
        AuthContext authContext = new AuthContext();
        
        // 1. Initial state (unauthenticated)
        assertFalse(authContext.isAuthenticated(), "Harusnya tidak terautentikasi saat inisialisasi");
        assertNull(authContext.getAuthUser(), "AuthUser harus null saat inisialisasi");

        // 2. Set an authenticated user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        
        authContext.setAuthUser(mockUser);

        // 3. State after setting user (authenticated)
        assertTrue(authContext.isAuthenticated(), "Harusnya terautentikasi setelah setting user");
        assertEquals(mockUser, authContext.getAuthUser(), "Harusnya mengembalikan user yang telah diset");

        // 4. Set authUser to null (unauthenticated)
        authContext.setAuthUser(null);
        assertFalse(authContext.isAuthenticated(), "Harusnya tidak terautentikasi setelah set null");
    }
}