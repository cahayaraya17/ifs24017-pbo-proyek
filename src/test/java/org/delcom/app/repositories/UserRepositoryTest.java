package org.delcom.app.repositories;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindFirstByEmail_Found() {
        // Setup data
        User user = new User("Test Repo", "test.repo@example.com", "password");
        
        // Simpan ke DB in-memory. Ini otomatis memicu @PrePersist (onCreate)
        entityManager.persistAndFlush(user); 

        // Eksekusi method repository
        Optional<User> found = userRepository.findFirstByEmail("test.repo@example.com");

        // Verifikasi
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test.repo@example.com");
    }

    @Test
    void testFindFirstByEmail_NotFound() {
        Optional<User> found = userRepository.findFirstByEmail("nonexistent@example.com");
        assertThat(found).isNotPresent();
    }
    
    @Test
    void testFindById_Found() {
        User user = new User("Test ID", "test.id@example.com", "password");
        User persistedUser = entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findById(persistedUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(persistedUser.getId());
    }
}