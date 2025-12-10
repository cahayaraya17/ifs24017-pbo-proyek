package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.util.ReflectionTestUtils; // Digunakan untuk menginjeksikan nilai @Value

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @InjectMocks
    private FileStorageService fileStorageService;

    @Mock
    private MultipartFile mockFile;
    
    // Mock Path objects yang akan dikembalikan oleh Paths.get/resolve
    @Mock
    private Path mockUploadPath;
    @Mock
    private Path mockFilePath;

    private final String mockUploadDir = "/tmp/test-uploads";
    private final UUID mockTodoId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        // Injeksi nilai @Value("${app.upload.dir:./uploads}") secara manual
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", mockUploadDir);
    }
    
    // --- Test storeFile ---

    @Test
    void storeFile_successWithExtension_shouldSaveAndReturnFilename() throws IOException {
        String originalFilename = "photo.jpg";
        String expectedFilename = "cover_" + mockTodoId.toString() + ".jpg";
        
        // Setup mock file input
        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        // Mock static calls ke Paths dan Files
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            
            // Mock Path resolution
            mockedPaths.when(() -> Paths.get(mockUploadDir)).thenReturn(mockUploadPath);
            when(mockUploadPath.resolve(expectedFilename)).thenReturn(mockFilePath);
            
            // Simulasikan direktori sudah ada (meliputi coverage if (!Files.exists))
            mockedFiles.when(() -> Files.exists(mockUploadPath)).thenReturn(true);
            
            // Mock Files.copy (pastikan dipanggil)
            mockedFiles.when(() -> Files.copy(any(InputStream.class), eq(mockFilePath), eq(StandardCopyOption.REPLACE_EXISTING)))
                       .thenReturn(0L); 

            // Eksekusi
            String actualFilename = fileStorageService.storeFile(mockFile, mockTodoId);

            // Verifikasi
            assertEquals(expectedFilename, actualFilename);
            mockedFiles.verify(() -> Files.copy(any(InputStream.class), eq(mockFilePath), eq(StandardCopyOption.REPLACE_EXISTING)), times(1));
            mockedFiles.verify(() -> Files.createDirectories(any()), never()); // Tidak perlu membuat direktori
        }
    }
    
    @Test
    void storeFile_createsDirectoryIfMissing() throws IOException {
        // Setup mock file input
        when(mockFile.getOriginalFilename()).thenReturn("data.png");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            
            // Mock Path resolution
            mockedPaths.when(() -> Paths.get(mockUploadDir)).thenReturn(mockUploadPath);
            when(mockUploadPath.resolve(anyString())).thenReturn(mockFilePath);
            
            // Simulasikan direktori BELUM ada (meliputi coverage if (!Files.exists))
            mockedFiles.when(() -> Files.exists(mockUploadPath)).thenReturn(false);
            
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                       .thenReturn(0L);

            // Eksekusi
            fileStorageService.storeFile(mockFile, mockTodoId);

            // Verifikasi Files.createDirectories dipanggil
            mockedFiles.verify(() -> Files.createDirectories(mockUploadPath), times(1));
        }
    }
    
    @Test
    void storeFile_noExtensionOrNullFilename_shouldGenerateFilenameWithoutExtension() throws IOException {
        String expectedFilename = "cover_" + mockTodoId.toString();
        
        // Case 1: originalFilename tidak mengandung "."
        when(mockFile.getOriginalFilename()).thenReturn("data_file_no_ext"); 
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            
            mockedPaths.when(() -> Paths.get(mockUploadDir)).thenReturn(mockUploadPath);
            when(mockUploadPath.resolve(expectedFilename)).thenReturn(mockFilePath);
            mockedFiles.when(() -> Files.exists(mockUploadPath)).thenReturn(true);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                       .thenReturn(0L);

            // Eksekusi
            String actualFilename = fileStorageService.storeFile(mockFile, mockTodoId);

            // Verifikasi
            assertEquals(expectedFilename, actualFilename);
        }
        
        // Case 2: originalFilename null
        when(mockFile.getOriginalFilename()).thenReturn(null);
        // Skip setup file input karena sudah di-mock di atas
        
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            
            mockedPaths.when(() -> Paths.get(mockUploadDir)).thenReturn(mockUploadPath);
            when(mockUploadPath.resolve(expectedFilename)).thenReturn(mockFilePath);
            mockedFiles.when(() -> Files.exists(mockUploadPath)).thenReturn(true);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                       .thenReturn(0L);

            // Eksekusi
            String actualFilename = fileStorageService.storeFile(mockFile, mockTodoId);
            
            // Verifikasi
            assertEquals(expectedFilename, actualFilename);
        }
    }

    // --- Test deleteFile ---

    @Test
    void deleteFile_fileExists_shouldReturnTrue() throws IOException {
        String filename = "file_to_delete.jpg";
        
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            
            mockedPaths.when(() -> Paths.get(mockUploadDir)).thenReturn(mockUploadPath);
            when(mockUploadPath.resolve(filename)).thenReturn(mockFilePath);
            
            // Mock Files.deleteIfExists: returns true (file was deleted)
            mockedFiles.when(() -> Files.deleteIfExists(mockFilePath)).thenReturn(true);

            // Eksekusi
            boolean result = fileStorageService.deleteFile(filename);

            // Verifikasi
            assertTrue(result);
        }
    }
    
    @Test
    void deleteFile_ioExceptionOccurs_shouldReturnFalse() throws IOException {
        String filename = "file_with_io_error.jpg";
        
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            
            mockedPaths.when(() -> Paths.get(mockUploadDir)).thenReturn(mockUploadPath);
            when(mockUploadPath.resolve(filename)).thenReturn(mockFilePath);
            
            // Mock Files.deleteIfExists to throw IOException (meliputi coverage catch block)
            mockedFiles.when(() -> Files.deleteIfExists(mockFilePath)).thenThrow(new IOException("Permission denied"));

            // Eksekusi
            boolean result = fileStorageService.deleteFile(filename);

            // Verifikasi
            assertFalse(result); 
        }
    }
    
    // --- Test loadFile ---
    
    @Test
    void loadFile_shouldReturnCorrectPath() {
        String filename = "test_file.txt";
        
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
            // Mock Path resolution
            mockedPaths.when(() -> Paths.get(mockUploadDir)).thenReturn(mockUploadPath);
            when(mockUploadPath.resolve(filename)).thenReturn(mockFilePath);
            
            // Eksekusi
            Path result = fileStorageService.loadFile(filename);

            // Verifikasi
            assertEquals(mockFilePath, result);
        }
    }
    
    // --- Test fileExists ---
    
    @Test
    void fileExists_fileIsPresent_shouldReturnTrue() throws IOException {
        String filename = "check_exists.png";
        
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            
            // Mock Path resolution
            mockedPaths.when(() -> Paths.get(mockUploadDir)).thenReturn(mockUploadPath);
            when(mockUploadPath.resolve(filename)).thenReturn(mockFilePath);
            
            // Mock Files.exists: returns true (meliputi coverage method fileExists)
            mockedFiles.when(() -> Files.exists(mockFilePath)).thenReturn(true);

            // Eksekusi
            boolean result = fileStorageService.fileExists(filename);

            // Verifikasi
            assertTrue(result);
        }
    }
}