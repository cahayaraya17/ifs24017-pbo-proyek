package org.delcom.app.repositories;

import org.delcom.app.entities.AuthToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
// Opsional: Gunakan profil yang sesuai jika Anda memiliki konfigurasi DB terpisah untuk pengujian
// @ActiveProfiles("test") 
class AuthTokenRepositoryTest {

    @Autowired
    private AuthTokenRepository authTokenRepository;
    
    @Autowired
    private TestEntityManager entityManager; // Digunakan untuk memastikan data bersih sebelum dan sesudah tes

    private final UUID userIdA = UUID.randomUUID();
    private final UUID userIdB = UUID.randomUUID();
    private final String tokenA = "token-for-user-A";
    private final String tokenB = "token-for-user-B";

    // --- Testing standard JpaRepository methods ---

    @Test
    void testSaveAndFindById() {
        AuthToken authToken = new AuthToken(userIdA, tokenA);
        
        // 1. Save
        AuthToken savedToken = authTokenRepository.save(authToken);
        entityManager.flush(); // Sinkronkan ke DB in-memory

        assertNotNull(savedToken.getId());

        // 2. FindById
        Optional<AuthToken> foundToken = authTokenRepository.findById(savedToken.getId());

        assertTrue(foundToken.isPresent());
        assertEquals(userIdA, foundToken.get().getUserId());
        assertEquals(tokenA, foundToken.get().getToken());
    }

    // --- Testing custom query: findUserToken ---

    @Test
    void testFindUserToken_success() {
        // Persist token untuk User A
        AuthToken authTokenA = new AuthToken(userIdA, tokenA);
        entityManager.persistAndFlush(authTokenA);

        // Cari dengan parameter yang benar
        AuthToken foundToken = authTokenRepository.findUserToken(userIdA, tokenA);

        assertNotNull(foundToken);
        assertEquals(tokenA, foundToken.getToken());
        assertEquals(userIdA, foundToken.getUserId());
    }

    @Test
    void testFindUserToken_tokenMismatch_shouldReturnNull() {
        // Persist tokenA
        AuthToken authTokenA = new AuthToken(userIdA, tokenA);
        entityManager.persistAndFlush(authTokenA);

        // Cari dengan token yang salah untuk userIdA
        AuthToken foundToken = authTokenRepository.findUserToken(userIdA, tokenB); // tokenB salah

        assertNull(foundToken);
    }
    
    @Test
    void testFindUserToken_userIdMismatch_shouldReturnNull() {
        // Persist tokenA
        AuthToken authTokenA = new AuthToken(userIdA, tokenA);
        entityManager.persistAndFlush(authTokenA);

        // Cari dengan userId yang salah
        AuthToken foundToken = authTokenRepository.findUserToken(userIdB, tokenA); // userIdB salah

        assertNull(foundToken);
    }
    
    // --- Testing custom modifying query: deleteByUserId ---

    @Test
    void testDeleteByUserId_shouldDeleteAllTokensForUser() {
        // Setup: 2 token untuk User A, 1 token untuk User B
        AuthToken tokenA1 = new AuthToken(userIdA, tokenA + "1");
        AuthToken tokenA2 = new AuthToken(userIdA, tokenA + "2");
        AuthToken tokenB1 = new AuthToken(userIdB, tokenB + "1");
        
        entityManager.persistAndFlush(tokenA1);
        entityManager.persistAndFlush(tokenA2);
        entityManager.persistAndFlush(tokenB1);

        // Verifikasi keberadaan awal
        assertEquals(3, authTokenRepository.count());

        // Hapus semua token milik User A
        authTokenRepository.deleteByUserId(userIdA);
        entityManager.flush(); // Commit penghapusan

        // Verifikasi setelah penghapusan
        assertEquals(1, authTokenRepository.count());

        // Verifikasi bahwa token milik User B tetap ada
        Optional<AuthToken> remainingToken = authTokenRepository.findById(tokenB1.getId());
        assertTrue(remainingToken.isPresent());
        assertEquals(userIdB, remainingToken.get().getUserId());

        // Verifikasi bahwa token milik User A sudah hilang
        assertFalse(authTokenRepository.findById(tokenA1.getId()).isPresent());
    }
    
    @Test
    void testDeleteByUserId_noTokensFound() {
        // Setup: Hanya token User B yang ada
        AuthToken tokenB1 = new AuthToken(userIdB, tokenB + "1");
        entityManager.persistAndFlush(tokenB1);
        
        long initialCount = authTokenRepository.count();

        // Coba hapus token untuk userIdA yang tidak ada
        authTokenRepository.deleteByUserId(userIdA);
        entityManager.flush(); 

        // Verifikasi bahwa tidak ada perubahan pada jumlah token
        assertEquals(initialCount, authTokenRepository.count());
    }
}