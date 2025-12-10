package org.delcom.app.repositories;

import org.delcom.app.entities.Photo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PhotoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PhotoRepository photoRepository;

    @Test
    void testFindById_Found() {
        // Given
        UUID userId = UUID.randomUUID();
        Photo photo = new Photo(userId, "Test Photo", "Landscape", "Description", new BigDecimal("100.00"));
        Photo persistedPhoto = entityManager.persistAndFlush(photo);

        // When
        Optional<Photo> found = photoRepository.findById(persistedPhoto.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(persistedPhoto.getId(), found.get().getId());
        assertEquals("Test Photo", found.get().getTitle());
    }

    @Test
    void testFindById_NotFound() {
        // When
        Optional<Photo> found = photoRepository.findById(UUID.randomUUID());

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        // Given
        UUID userId = UUID.randomUUID();
        Photo photo1 = new Photo(userId, "Photo 1", "Portrait", "Desc 1", new BigDecimal("50.00"));
        Photo photo2 = new Photo(userId, "Photo 2", "Abstract", "Desc 2", new BigDecimal("75.00"));
        entityManager.persistAndFlush(photo1);
        entityManager.persistAndFlush(photo2);

        // When
        List<Photo> photos = photoRepository.findAll();

        // Then
        assertTrue(photos.size() >= 2);
        assertTrue(photos.stream().anyMatch(p -> p.getTitle().equals("Photo 1")));
        assertTrue(photos.stream().anyMatch(p -> p.getTitle().equals("Photo 2")));
    }

    @Test
    void testSave() {
        // Given
        UUID userId = UUID.randomUUID();
        Photo photo = new Photo(userId, "New Photo", "Nature", "Beautiful nature", new BigDecimal("200.00"));

        // When
        Photo saved = photoRepository.save(photo);

        // Then
        assertNotNull(saved.getId());
        assertEquals("New Photo", saved.getTitle());
        assertEquals(userId, saved.getUserId());
    }

    @Test
    void testDeleteById() {
        // Given
        UUID userId = UUID.randomUUID();
        Photo photo = new Photo(userId, "Photo to Delete", "Test", "Test desc", new BigDecimal("10.00"));
        Photo persisted = entityManager.persistAndFlush(photo);

        // When
        photoRepository.deleteById(persisted.getId());

        // Then
        Optional<Photo> found = photoRepository.findById(persisted.getId());
        assertFalse(found.isPresent());
    }
}
