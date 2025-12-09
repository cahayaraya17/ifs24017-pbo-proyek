package org.delcom.app.repositories;

import org.delcom.app.entities.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, UUID> {
    // Menampilkan foto milik user tertentu
    List<Photo> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    // Query untuk Chart: Menghitung jumlah foto per kategori milik user
    @Query("SELECT p.category, COUNT(p) FROM Photo p WHERE p.userId = ?1 GROUP BY p.category")
    List<Object[]> countPhotosByCategory(UUID userId);
}