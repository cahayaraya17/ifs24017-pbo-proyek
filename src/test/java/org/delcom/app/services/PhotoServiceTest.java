package org.delcom.app.services;

import org.delcom.app.entities.Photo;
import org.delcom.app.repositories.PhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @InjectMocks
    private PhotoService photoService;

    private Photo samplePhoto;
    private UUID photoId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        photoId = UUID.randomUUID();
        userId = UUID.randomUUID();
        
        // Inisialisasi objek Photo standar untuk pengujian
        samplePhoto = new Photo();
        samplePhoto.setId(photoId);
        samplePhoto.setUserId(userId);
        samplePhoto.setTitle("Original Title");
        samplePhoto.setCategory("Nature");
        samplePhoto.setDescription("Original Desc");
        samplePhoto.setPrice(new BigDecimal("100.00"));
        samplePhoto.setFilename("original.jpg");
    }

    @Test
    void testGetAllPhotos() {
        // Given
        List<Photo> photoList = Collections.singletonList(samplePhoto);
        when(photoRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(photoList);

        // When
        List<Photo> result = photoService.getAllPhotos(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(samplePhoto.getTitle(), result.get(0).getTitle());
        verify(photoRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void testGetPhotoById_Found() {
        // Given
        when(photoRepository.findById(photoId)).thenReturn(Optional.of(samplePhoto));

        // When
        Photo result = photoService.getPhotoById(photoId);

        // Then
        assertNotNull(result);
        assertEquals(photoId, result.getId());
        verify(photoRepository, times(1)).findById(photoId);
    }

    @Test
    void testGetPhotoById_NotFound() {
        // Given
        when(photoRepository.findById(photoId)).thenReturn(Optional.empty());

        // When
        Photo result = photoService.getPhotoById(photoId);

        // Then
        assertNull(result);
        verify(photoRepository, times(1)).findById(photoId);
    }

    @Test
    void testCreatePhoto() {
        // Given
        when(photoRepository.save(samplePhoto)).thenReturn(samplePhoto);

        // When
        Photo result = photoService.createPhoto(samplePhoto);

        // Then
        assertNotNull(result);
        assertEquals("Original Title", result.getTitle());
        verify(photoRepository, times(1)).save(samplePhoto);
    }

    @Test
    void testUpdatePhotoData_Success() {
        // Given
        String newTitle = "Updated Title";
        String newCategory = "Abstract";
        String newDesc = "Updated Description";
        BigDecimal newPrice = new BigDecimal("250.00");

        when(photoRepository.findById(photoId)).thenReturn(Optional.of(samplePhoto));
        // Mock save untuk mengembalikan objek yang sama (yang sudah dimodifikasi di service)
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Photo updatedPhoto = photoService.updatePhotoData(photoId, newTitle, newCategory, newDesc, newPrice);

        // Then
        assertNotNull(updatedPhoto);
        assertEquals(newTitle, updatedPhoto.getTitle());
        assertEquals(newCategory, updatedPhoto.getCategory());
        assertEquals(newDesc, updatedPhoto.getDescription());
        assertEquals(newPrice, updatedPhoto.getPrice());
        
        // Verifikasi bahwa filename TIDAK berubah
        assertEquals("original.jpg", updatedPhoto.getFilename());
        
        verify(photoRepository, times(1)).findById(photoId);
        verify(photoRepository, times(1)).save(samplePhoto);
    }

    @Test
    void testUpdatePhotoData_NotFound() {
        // Given
        when(photoRepository.findById(photoId)).thenReturn(Optional.empty());

        // When
        Photo result = photoService.updatePhotoData(photoId, "Title", "Cat", "Desc", BigDecimal.TEN);

        // Then
        assertNull(result);
        verify(photoRepository, times(1)).findById(photoId);
        verify(photoRepository, never()).save(any(Photo.class));
    }

    @Test
    void testUpdatePhotoFile_Success() {
        // Given
        String newFilename = "new-image.png";
        when(photoRepository.findById(photoId)).thenReturn(Optional.of(samplePhoto));

        // When
        photoService.updatePhotoFile(photoId, newFilename);

        // Then
        assertEquals(newFilename, samplePhoto.getFilename());
        verify(photoRepository, times(1)).findById(photoId);
        verify(photoRepository, times(1)).save(samplePhoto);
    }

    @Test
    void testUpdatePhotoFile_NotFound() {
        // Given
        when(photoRepository.findById(photoId)).thenReturn(Optional.empty());

        // When
        photoService.updatePhotoFile(photoId, "new.jpg");

        // Then
        verify(photoRepository, times(1)).findById(photoId);
        verify(photoRepository, never()).save(any(Photo.class));
    }

    @Test
    void testDeletePhoto() {
        // When
        photoService.deletePhoto(photoId);

        // Then
        verify(photoRepository, times(1)).deleteById(photoId);
    }

    @Test
    void testGetChartData() {
        // Given
        List<Object[]> mockChartData = new ArrayList<>();
        mockChartData.add(new Object[]{"Nature", 5L});
        mockChartData.add(new Object[]{"Urban", 3L});
        
        when(photoRepository.countPhotosByCategory(userId)).thenReturn(mockChartData);

        // When
        List<Object[]> result = photoService.getChartData(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Nature", result.get(0)[0]);
        assertEquals(5L, result.get(0)[1]);
        
        verify(photoRepository, times(1)).countPhotosByCategory(userId);
    }
}