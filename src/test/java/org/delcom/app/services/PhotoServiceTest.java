package org.delcom.app.services;

import org.delcom.app.entities.Photo;
import org.delcom.app.repositories.PhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @InjectMocks
    private PhotoService photoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllPhotos() {
        // Given
        UUID userId = UUID.randomUUID();
        Photo photo1 = new Photo(userId, "Photo 1", "Landscape", "Desc 1", new BigDecimal("100.00"));
        Photo photo2 = new Photo(userId, "Photo 2", "Portrait", "Desc 2", new BigDecimal("200.00"));
        List<Photo> photos = Arrays.asList(photo1, photo2);
        when(photoRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(photos);

        // When
        List<Photo> result = photoService.getAllPhotos(userId);

        // Then
        assertEquals(2, result.size());
        assertEquals("Photo 1", result.get(0).getTitle());
        assertEquals("Photo 2", result.get(1).getTitle());
        verify(photoRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void testGetPhotoById_Found() {
        // Given
        UUID photoId = UUID.randomUUID();
        Photo photo = new Photo(UUID.randomUUID(), "Test Photo", "Category", "Description", new BigDecimal("150.00"));
        when(photoRepository.findById(photoId)).thenReturn(Optional.of(photo));

        // When
        Photo found = photoService.getPhotoById(photoId);

        // Then
        assertNotNull(found);
        assertEquals(photo, found);
        verify(photoRepository, times(1)).findById(photoId);
    }

    @Test
    void testGetPhotoById_NotFound() {
        // Given
        UUID photoId = UUID.randomUUID();
        when(photoRepository.findById(photoId)).thenReturn(Optional.empty());

        // When
        Photo found = photoService.getPhotoById(photoId);

        // Then
        assertNull(found);
        verify(photoRepository, times(1)).findById(photoId);
    }

    @Test
    void testCreatePhoto() {
        // Given
        UUID userId = UUID.randomUUID();
        Photo photo = new Photo(userId, "New Photo", "Nature", "Beautiful", new BigDecimal("300.00"));
        when(photoRepository.save(photo)).thenReturn(photo);

        // When
        Photo saved = photoService.createPhoto(photo);

        // Then
        assertNotNull(saved);
        assertEquals("New Photo", saved.getTitle());
        verify(photoRepository, times(1)).save(photo);
    }

    @Test
    void testDeleteById() {
        // Given
        UUID photoId = UUID.randomUUID();

        // When
        photoService.deleteById(photoId);

        // Then
        verify(photoRepository, times(1)).deleteById(photoId);
    }
}
