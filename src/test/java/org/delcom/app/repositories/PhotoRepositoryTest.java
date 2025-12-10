package org.delcom.app.repositories;

import org.delcom.app.entities.Photo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PhotoRepositoryTest {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final UUID userIdA = UUID.randomUUID();
    private final UUID userIdB = UUID.randomUUID();
    
    // Data dummy yang akan digunakan di setiap test
    private Photo photoA1;
    private Photo photoA2;
    private Photo photoB1;

    @BeforeEach
    void setup() {
        // Clear database sebelum setiap test
        photoRepository.deleteAll();
        
        // Setup data Photo untuk User A (kategori Landscape dan Portrait)
        photoA1 = new Photo(userIdA, "Mountain", "Landscape", "Desc A1", new BigDecimal("100.00"));
        photoA1.setCreatedAt(LocalDateTime.now().minusDays(2)); // Lebih lama
        
        photoA2 = new Photo(userIdA, "Person", "Portrait", "Desc A2", new BigDecimal("200.00"));
        photoA2.setCreatedAt(LocalDateTime.now().minusDays(1)); // Lebih baru
        
        // Setup data Photo untuk User B (kategori Landscape)
        photoB1 = new Photo(userIdB, "Ocean", "Landscape", "Desc B1", new BigDecimal("300.00"));
        photoB1.setCreatedAt(LocalDateTime.now().minusDays(3));
        
        // Persist data
        entityManager.persistAndFlush(photoA1);
        entityManager.persistAndFlush(photoA2);
        entityManager.persistAndFlush(photoB1);
    }

    // --- 1. Testing standard JpaRepository methods ---

    @Test
    void testFindById() {
        // Pastikan ID sudah diset setelah persist
        UUID id = photoA1.getId();
        assertNotNull(id);
        
        Optional<Photo> found = photoRepository.findById(id);
        assertTrue(found.isPresent());
        assertEquals("Mountain", found.get().getTitle());
    }

    // --- 2. Testing custom derived query: findByUserIdOrderByCreatedAtDesc ---

    @Test
    void testFindByUserIdOrderByCreatedAtDesc_shouldReturnPhotosInOrder() {
        List<Photo> photos = photoRepository.findByUserIdOrderByCreatedAtDesc(userIdA);

        assertNotNull(photos);
        assertEquals(2, photos.size());

        // Verifikasi urutan: yang paling baru (photoA2) harus di posisi pertama
        assertEquals(photoA2.getTitle(), photos.get(0).getTitle(), "Photo terbaru harus di posisi 0");
        assertEquals(photoA1.getTitle(), photos.get(1).getTitle(), "Photo lama harus di posisi 1");
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc_noPhotosFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        List<Photo> photos = photoRepository.findByUserIdOrderByCreatedAtDesc(nonExistentUserId);

        assertNotNull(photos);
        assertTrue(photos.isEmpty(), "Harusnya mengembalikan list kosong jika user tidak memiliki foto");
    }
    
    // --- 3. Testing custom JPQL query: countPhotosByCategory ---

    @Test
    void testCountPhotosByCategory_shouldGroupAndCountCorrectly() {
        // Data setup:
        // User A: 1 Landscape, 1 Portrait.
        
        List<Object[]> chartData = photoRepository.countPhotosByCategory(userIdA);
        
        assertNotNull(chartData);
        assertEquals(2, chartData.size(), "Harusnya ada 2 grup kategori (Landscape, Portrait)");
        
        // Mapping hasil query ke Map untuk memudahkan verifikasi
        var dataMap = chartData.stream()
                .collect(java.util.stream.Collectors.toMap(
                        arr -> (String) arr[0], 
                        arr -> ((Number) arr[1]).longValue()));

        assertTrue(dataMap.containsKey("Landscape"));
        assertTrue(dataMap.containsKey("Portrait"));
        
        assertEquals(1L, dataMap.get("Landscape"), "Jumlah foto Landscape harus 1");
        assertEquals(1L, dataMap.get("Portrait"), "Jumlah foto Portrait harus 1");
    }

    @Test
    void testCountPhotosByCategory_singleCategory() {
        // Data setup:
        // User B: 1 Landscape.
        
        List<Object[]> chartData = photoRepository.countPhotosByCategory(userIdB);
        
        assertEquals(1, chartData.size(), "Harusnya hanya ada 1 grup kategori untuk User B");
        
        assertEquals("Landscape", chartData.get(0)[0]);
        assertEquals(1L, ((Number) chartData.get(0)[1]).longValue());
    }

    @Test
    void testCountPhotosByCategory_noPhotos() {
        UUID nonExistentUserId = UUID.randomUUID();
        List<Object[]> chartData = photoRepository.countPhotosByCategory(nonExistentUserId);
        
        assertNotNull(chartData);
        assertTrue(chartData.isEmpty(), "Harusnya mengembalikan list kosong jika user tidak punya foto");
    }
}