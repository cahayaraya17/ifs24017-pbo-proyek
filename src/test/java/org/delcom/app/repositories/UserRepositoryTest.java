package org.delcom.app.repositories;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User userA;
    private final String emailA = "alice@example.com";
    private final String emailB = "bob@example.com";

    @BeforeEach
    void setup() {
        // Clear database sebelum setiap test (Opsional, tapi praktik yang baik)
        userRepository.deleteAll();
        
        // Setup User A
        userA = new User("Alice", emailA, "pass123");
        
        // Persist User A
        entityManager.persistAndFlush(userA);
    }

    // --- 1. Testing standard JpaRepository methods (for coverage) ---

    @Test
    void testSaveAndFindById() {
        UUID id = userA.getId();
        assertNotNull(id);
        
        Optional<User> found = userRepository.findById(id);
        
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
        assertEquals(emailA, found.get().getEmail());
    }
    
    // --- 2. Testing custom derived query: findFirstByEmail ---

    @Test
    void testFindFirstByEmail_userExists() {
        // Cari User A menggunakan emailnya
        Optional<User> found = userRepository.findFirstByEmail(emailA);

        assertTrue(found.isPresent(), "Harusnya menemukan user berdasarkan email");
        assertEquals(userA.getId(), found.get().getId());
        assertEquals("Alice", found.get().getName());
    }

    @Test
    void testFindFirstByEmail_userDoesNotExist() {
        // Cari user dengan email yang tidak terdaftar
        Optional<User> found = userRepository.findFirstByEmail("nonexistent@example.com");

        assertFalse(found.isPresent(), "Harusnya tidak menemukan user untuk email yang tidak ada");
    }
    
    @Test
    void testFindFirstByEmail_caseSensitivity() {
        // Asumsi database in-memory H2 default adalah case-insensitive
        // Jika DB asli Anda case-sensitive, hasil ini bisa berbeda.
        
        // Simulasikan pencarian dengan huruf kapital
        Optional<User> found = userRepository.findFirstByEmail(emailA.toUpperCase());

        // Di H2 default (case-insensitive)
        assertTrue(found.isPresent(), "Default H2/JPA seringkali case-insensitive untuk string"); 
        
        // Jika Anda menggunakan DB yang case-sensitive (seperti PostgreSQL dengan pengaturan tertentu), 
        // Anda mungkin mengharapkan assertFalse di sini, dan perlu menggunakan query JPQL eksplisit.
    }
}