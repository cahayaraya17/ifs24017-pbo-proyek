package org.delcom.app.controllers;

import org.delcom.app.dto.PhotoForm;
import org.delcom.app.entities.Photo;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.PhotoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Isolasi hanya PhotoController
@WebMvcTest(PhotoController.class)
class PhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhotoService photoService;

    @MockBean
    private FileStorageService fileStorageService;

    // Data Mock
    private final UUID photoId = UUID.randomUUID();
    private final Long userId = 1L;
    private User mockUser;
    private Photo mockPhoto;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");

        mockPhoto = new Photo(userId, "Test Title", "Category A", "Description", new BigDecimal("100.00"));
        mockPhoto.setId(photoId);
        mockPhoto.setFilename("mock_filename.jpg");
        
        // Setup Security Context secara manual untuk menguji getAuthUser()
        // Ini memastikan `getAuthUser()` di Controller bekerja.
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(mockUser);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    // Helper untuk mensimulasikan kegagalan autentikasi/sesi habis (user=null)
    private void setupUnauthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("anonymousUser"); // Standard Spring Security behavior for unauthenticated
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    // --- Test Method getAuthUser() (melalui use case) ---
    
    @Test
    void index_unauthenticated_shouldRedirectToLogin() throws Exception {
        setupUnauthenticated(); // Simulasikan sesi habis

        mockMvc.perform(get("/photos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    // --- Test CRUD (Index, Create, Detail, Edit, Update, Delete) ---

    @Test
    @WithMockUser(username = "testuser")
    void index_authenticated_shouldReturnView() throws Exception {
        when(photoService.getAllPhotos(userId)).thenReturn(Collections.singletonList(mockPhoto));

        mockMvc.perform(get("/photos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/photos/index"))
                .andExpect(model().attributeExists("photos"));
        
        verify(photoService, times(1)).getAllPhotos(userId);
    }

    @Test
    @WithMockUser(username = "testuser")
    void create_shouldReturnFormView() throws Exception {
        mockMvc.perform(get("/photos/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/photos/form"))
                .andExpect(model().attributeExists("photoForm"));
    }

    // A. Testing POST /store - Success
    @Test
    @WithMockUser(username = "testuser")
    void store_validForm_shouldCreatePhotoAndRedirect() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "some image data".getBytes());
        
        // Mocking behavior saat createPhoto dipanggil
        when(photoService.createPhoto(any(Photo.class))).thenReturn(mockPhoto);
        when(fileStorageService.storeFile(eq(file), eq(photoId))).thenReturn("new_mock_filename.jpg");

        mockMvc.perform(multipart("/photos/store")
                        .file(file)
                        .param("title", "New Photo Title")
                        .param("category", "Nature")
                        .param("description", "A beautiful shot")
                        .param("price", "250.00")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photos"))
                .andExpect(flash().attributeExists("success"));

        // Verifikasi bahwa servis dipanggil
        verify(photoService, times(1)).createPhoto(any(Photo.class));
        verify(fileStorageService, times(1)).storeFile(eq(file), eq(photoId));
        verify(photoService, times(1)).updatePhotoFile(eq(photoId), eq("new_mock_filename.jpg"));
    }

    // B. Testing POST /store - Validation Error (Missing Field)
    @Test
    @WithMockUser(username = "testuser")
    void store_validationError_shouldReturnForm() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "some image data".getBytes());
        
        // Title dihilangkan, yang akan memicu result.hasErrors()
        mockMvc.perform(multipart("/photos/store")
                        .file(file)
                        .param("category", "Nature")
                        .param("description", "A beautiful shot")
                        .param("price", "250.00")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/photos/form"))
                .andExpect(model().attributeHasErrors("photoForm"));

        verify(photoService, never()).createPhoto(any());
    }

    // C. Testing POST /store - Missing File Error
    @Test
    @WithMockUser(username = "testuser")
    void store_missingFile_shouldReturnFormWithError() throws Exception {
        // Simulasikan file yang null atau kosong
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", "".getBytes());

        mockMvc.perform(multipart("/photos/store")
                        .file(emptyFile)
                        .param("title", "Valid Title")
                        .param("category", "Nature")
                        .param("description", "Desc")
                        .param("price", "10.00")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/photos/form"))
                .andExpect(model().attributeHasFieldErrors("photoForm", "file")); // Cek error file
        
        verify(photoService, never()).createPhoto(any());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void detail_photoExists_shouldReturnDetailView() throws Exception {
        when(photoService.getPhotoById(photoId)).thenReturn(mockPhoto);

        mockMvc.perform(get("/photos/{id}", photoId))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/photos/detail"))
                .andExpect(model().attribute("photo", mockPhoto));
    }

    @Test
    @WithMockUser(username = "testuser")
    void detail_photoNotFound_shouldRedirectToIndex() throws Exception {
        when(photoService.getPhotoById(photoId)).thenReturn(null);

        mockMvc.perform(get("/photos/{id}", photoId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photos"));
    }
    
    // --- Test Edit & Update ---

    @Test
    @WithMockUser(username = "testuser")
    void edit_photoExists_shouldReturnFormView() throws Exception {
        when(photoService.getPhotoById(photoId)).thenReturn(mockPhoto);

        mockMvc.perform(get("/photos/{id}/edit", photoId))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/photos/form"))
                .andExpect(model().attributeExists("photoForm"))
                .andExpect(model().attribute("photoId", photoId));
    }

    @Test
    @WithMockUser(username = "testuser")
    void edit_photoNotFound_shouldRedirectToIndex() throws Exception {
        when(photoService.getPhotoById(photoId)).thenReturn(null);

        mockMvc.perform(get("/photos/{id}/edit", photoId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photos"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void update_validForm_shouldUpdatePhotoAndRedirect() throws Exception {
        // Menggunakan perform(post) karena ini bukan multipart
        mockMvc.perform(post("/photos/{id}/update", photoId)
                        .param("title", "Updated Title")
                        .param("category", "Art")
                        .param("description", "New Desc")
                        .param("price", "300.00")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photos"))
                .andExpect(flash().attributeExists("success"));

        // Verifikasi bahwa updatePhotoData dipanggil dengan data baru
        verify(photoService, times(1)).updatePhotoData(
                eq(photoId), 
                eq("Updated Title"), 
                eq("Art"), 
                eq("New Desc"), 
                eq(new BigDecimal("300.00")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void update_validationError_shouldReturnForm() throws Exception {
        // Mengosongkan Title (misalnya)
        mockMvc.perform(post("/photos/{id}/update", photoId)
                        .param("title", "") 
                        .param("category", "Art")
                        .param("description", "New Desc")
                        .param("price", "300.00")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/photos/form"))
                .andExpect(model().attributeHasFieldErrors("photoForm", "title"));

        verify(photoService, never()).updatePhotoData(any(), any(), any(), any(), any());
    }

    // --- Test Update Image ---

    @Test
    @WithMockUser(username = "testuser")
    void updateImage_validFile_shouldReplaceFileAndRedirect() throws Exception {
        MockMultipartFile newFile = new MockMultipartFile("file", "new_test.png", "image/png", "new image data".getBytes());
        String oldFilename = mockPhoto.getFilename();

        when(photoService.getPhotoById(photoId)).thenReturn(mockPhoto);
        when(fileStorageService.storeFile(eq(newFile), eq(photoId))).thenReturn("new_filename.png");

        mockMvc.perform(multipart("/photos/{id}/update-image", photoId)
                        .file(newFile)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/photos/" + photoId))
                .andExpect(flash().attributeExists("success"));

        // Verifikasi: Hapus file lama, simpan file baru, update database
        verify(fileStorageService, times(1)).