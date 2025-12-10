package org.delcom.app.services;

import org.delcom.app.entities.Photo;
import org.delcom.app.repositories.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @InjectMocks
    private PhotoService photoService;

    // Data dummy
    private final UUID mockUserId = UUID.randomUUID();
    private final UUID mockPhotoId = UUID.randomUUID();
    private final Photo mockPhoto = new Photo(
            mockUserId, 
            "Original Title", 
            "Landscape", 
            "Original Desc", 
            new BigDecimal("100.00")
    );
    private final List<Photo> mockPhotoList = Arrays.asList(mockPhoto);

    // --- 1. Test getAllPhotos ---
    @Test
    void getAllPhotos_shouldCallRepositoryAndReturnList() {
        // Setup: Ketika repository dipanggil, kembalikan mockPhotoList
        when(photoRepository.findByUserIdOrderByCreatedAtDesc(mockUserId)).thenReturn(mockPhotoList);

        // Eksekusi
        List<Photo> result = photoService.getAllPhotos(mockUserId);

        // Verifikasi
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockPhotoList, result);
        verify(photoRepository, times(1)).findByUserIdOrderByCreatedAtDesc(mockUserId);
    }

    // --- 2. Test getPhotoById ---
    @Test
    void getPhotoById_found_shouldReturnPhoto() {
        // Setup: Ketika repository dipanggil, kembalikan Optional.of(mockPhoto)
        when(photoRepository.findById(mockPhotoId)).thenReturn(Optional.of(mockPhoto));

        // Eksekusi
        Photo result = photoService.getPhotoById(mockPhotoId);

        // Verifikasi
        assertNotNull(result);
        assertEquals(mockPhoto, result);
        verify(photoRepository, times(1)).findById(mockPhotoId);
    }

    @Test
    void getPhotoById_notFound_shouldReturnNull() {
        // Setup: Ketika repository dipanggil, kembalikan Optional.empty()
        when(photoRepository.findById(mockPhotoId)).thenReturn(Optional.empty());

        // Eksekusi
        Photo result = photoService.getPhotoById(mockPhotoId);

        // Verifikasi
        assertNull(result); // Memastikan orElse(null) bekerja
    }

    // --- 3. Test createPhoto ---
    @Test
    void createPhoto_shouldCallRepositorySave() {
        // Setup: Ketika repository.save dipanggil, kembalikan objek yang sama
        when(photoRepository.save(mockPhoto)).thenReturn(mockPhoto);

        // Eksekusi
        Photo result = photoService.createPhoto(mockPhoto);

        // Verifikasi
        assertNotNull(result);
        assertEquals(mockPhoto, result);
        verify(photoRepository, times(1)).save(mockPhoto);
    }
    
    // --- 4. Test updatePhotoData ---
    @Test
    void updatePhotoData_found_shouldUpdateFieldsAndSave() {
        // Data update
        String newTitle = "New Title";
        String newCategory = "Portrait";
        String newDesc = "Updated Desc";
        BigDecimal newPrice = new BigDecimal("150.75");

        // Setup: getPhotoById dipanggil di awal updatePhotoData
        when(photoRepository.findById(mockPhotoId)).thenReturn(Optional.of(mockPhoto));
        // Setup: photoRepository.save mengembalikan objek yang sudah diupdate
        when(photoRepository.save(mockPhoto)).thenReturn(mockPhoto);

        // Eksekusi
        Photo updatedPhoto = photoService.updatePhotoData(mockPhotoId, newTitle, newCategory, newDesc, newPrice);

        // Verifikasi
        assertNotNull(updatedPhoto);
        assertEquals(newTitle, updatedPhoto.getTitle());
        assertEquals(newCategory, updatedPhoto.getCategory());
        assertEquals(newDesc, updatedPhoto.getDescription());
        assertEquals(newPrice, updatedPhoto.getPrice());
        
        // Verifikasi bahwa save dipanggil sekali
        verify(photoRepository, times(1)).save(mockPhoto);
    }

    @Test
    void updatePhotoData_notFound_shouldReturnNull() {
        BigDecimal dummyPrice = new BigDecimal("10.00");
        // Setup: Photo tidak ditemukan
        when(photoRepository.findById(mockPhotoId)).thenReturn(Optional.empty());

        // Eksekusi
        Photo result = photoService.updatePhotoData(mockPhotoId, "Title", "Cat", "Desc", dummyPrice);

        // Verifikasi
        assertNull(result);
        // Verifikasi bahwa save tidak dipanggil
        verify(photoRepository, never()).save(any(Photo.class));
    }

    // --- 5. Test updatePhotoFile ---
    @Test
    void updatePhotoFile_found_shouldUpdateFilenameAndSave() {
        String newFilename = "new_image.png";
        
        // Setup: getPhotoById dipanggil di awal updatePhotoFile
        when(photoRepository.findById(mockPhotoId)).thenReturn(Optional.of(mockPhoto));
        
        // Eksekusi
        photoService.updatePhotoFile(mockPhotoId, newFilename);

        // Verifikasi
        assertEquals(newFilename, mockPhoto.getFilename());
        // Verifikasi bahwa save dipanggil sekali
        verify(photoRepository, times(1)).save(mockPhoto);
    }

    @Test
    void updatePhotoFile_notFound_shouldDoNothing() {
        String newFilename = "new_image.png";
        // Setup: Photo tidak ditemukan
        when(photoRepository.findById(mockPhotoId)).thenReturn(Optional.empty());

        // Eksekusi
        photoService.updatePhotoFile(mockPhotoId, newFilename);

        // Verifikasi
        // Verifikasi bahwa save tidak dipanggil
        verify(photoRepository, never()).save(any(Photo.class));
    }

    // --- 6. Test deletePhoto ---
    @Test
    void deletePhoto_shouldCallRepositoryDeleteById() {
        // Setup: deleteById adalah void, jadi hanya perlu verifikasi panggilan
        doNothing().when(photoRepository).deleteById(mockPhotoId);

        // Eksekusi
        photoService.deletePhoto(mockPhotoId);

        // Verifikasi
        verify(photoRepository, times(1)).deleteById(mockPhotoId);
    }
    
    // --- 7. Test getChartData ---
    @Test
    void getChartData_shouldCallRepositoryAndReturnChartData() {
        // Data chart dummy: Category A (2), Category B (1)
        List<Object[]> chartData = Arrays.asList(
            new Object[]{"Category A", 2L},
            new Object[]{"Category B", 1L}
        );
        
        // Setup
        when(photoRepository.countPhotosByCategory(mockUserId)).thenReturn(chartData);

        // Eksekusi
        List<Object[]> result = photoService.getChartData(mockUserId);

        // Verifikasi
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(chartData, result);
        verify(photoRepository, times(1)).countPhotosByCategory(mockUserId);
    }
}