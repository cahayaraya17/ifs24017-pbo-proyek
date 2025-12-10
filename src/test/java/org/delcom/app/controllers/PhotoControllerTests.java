package org.delcom.app.controllers;

import org.delcom.app.dto.PhotoForm;
import org.delcom.app.entities.Photo;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.PhotoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoControllerTest {

    @Mock
    private PhotoService photoService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private PhotoController photoController;

    private User mockUser;
    private Photo mockPhoto;

    @BeforeEach
    void setUp() {
        // Setup Dummy User
        mockUser = mock(User.class);
        lenient().when(mockUser.getId()).thenReturn(UUID.randomUUID());

        // Setup Dummy Photo
        mockPhoto = new Photo(UUID.randomUUID(), "Test Title", "Category", "Desc", new BigDecimal("1000"));
        mockPhoto.setId(UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- Helper untuk Mock Security Context ---
    private void mockSecurityContext(User user) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        if (user != null) {
            lenient().when(authentication.getPrincipal()).thenReturn(user);
        } else {
            lenient().when(authentication.getPrincipal()).thenReturn("anonymousUser");
        }
        
        SecurityContextHolder.setContext(securityContext);
    }

    // --- TEST INDEX ---

    @Test
    void testIndex_LoggedIn() {
        mockSecurityContext(mockUser);
        when(photoService.getAllPhotos(any(UUID.class))).thenReturn(new ArrayList<>());

        String view = photoController.index(model);

        assertEquals("pages/photos/index", view);
        verify(model).addAttribute(eq("photos"), anyList());
    }

    @Test
    void testIndex_NotLoggedIn() {
        mockSecurityContext(null); // User null/anonymous

        String view = photoController.index(model);

        assertEquals("redirect:/auth/login", view);
        verifyNoInteractions(photoService);
    }

    // --- TEST CREATE ---

    @Test
    void testCreate() {
        String view = photoController.create(model);
        
        assertEquals("pages/photos/form", view);
        verify(model).addAttribute(eq("photoForm"), any(PhotoForm.class));
    }

    // --- TEST STORE ---

    @Test
    void testStore_HasValidationErrors() throws IOException {
        when(bindingResult.hasErrors()).thenReturn(true);
        PhotoForm form = new PhotoForm();

        String view = photoController.store(form, bindingResult, redirectAttributes);

        assertEquals("pages/photos/form", view);
        verifyNoInteractions(photoService);
    }

    @Test
    void testStore_FileEmpty() throws IOException {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(multipartFile.isEmpty()).thenReturn(true);
        
        PhotoForm form = new PhotoForm();
        form.setFile(multipartFile);

        String view = photoController.store(form, bindingResult, redirectAttributes);

        assertEquals("pages/photos/form", view);
        verify(bindingResult).rejectValue("file", "error.file", "File foto wajib diunggah");
    }

    @Test
    void testStore_UserNotLoggedIn() throws IOException {
        mockSecurityContext(null);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(multipartFile.isEmpty()).thenReturn(false);

        PhotoForm form = new PhotoForm();
        form.setFile(multipartFile);

        String view = photoController.store(form, bindingResult, redirectAttributes);

        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void testStore_Success() throws IOException {
        mockSecurityContext(mockUser);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(photoService.createPhoto(any(Photo.class))).thenReturn(mockPhoto);
        when(fileStorageService.storeFile(any(), any())).thenReturn("new-image.jpg");

        PhotoForm form = new PhotoForm();
        form.setTitle("Title");
        form.setCategory("Cat");
        form.setDescription("Desc");
        form.setPrice(BigDecimal.TEN);
        form.setFile(multipartFile);

        String view = photoController.store(form, bindingResult, redirectAttributes);

        assertEquals("redirect:/photos", view);
        verify(photoService).updatePhotoFile(eq(mockPhoto.getId()), eq("new-image.jpg"));
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
    }

    // --- TEST DETAIL ---

    @Test
    void testDetail_Found() {
        when(photoService.getPhotoById(any(UUID.class))).thenReturn(mockPhoto);

        String view = photoController.detail(UUID.randomUUID(), model);

        assertEquals("pages/photos/detail", view);
        verify(model).addAttribute("photo", mockPhoto);
    }

    @Test
    void testDetail_NotFound() {
        when(photoService.getPhotoById(any(UUID.class))).thenReturn(null);

        String view = photoController.detail(UUID.randomUUID(), model);

        assertEquals("redirect:/photos", view);
    }

    // --- TEST EDIT ---

    @Test
    void testEdit_Found() {
        when(photoService.getPhotoById(any(UUID.class))).thenReturn(mockPhoto);

        String view = photoController.edit(UUID.randomUUID(), model);

        assertEquals("pages/photos/form", view);
        verify(model).addAttribute(eq("photoForm"), any(PhotoForm.class));
        verify(model).addAttribute(eq("photoId"), any(UUID.class));
    }

    @Test
    void testEdit_NotFound() {
        when(photoService.getPhotoById(any(UUID.class))).thenReturn(null);

        String view = photoController.edit(UUID.randomUUID(), model);

        assertEquals("redirect:/photos", view);
    }

    // --- TEST UPDATE ---

    @Test
    void testUpdate_ValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        PhotoForm form = new PhotoForm();

        String view = photoController.update(UUID.randomUUID(), form, bindingResult, redirectAttributes);

        assertEquals("pages/photos/form", view);
    }

    @Test
    void testUpdate_Success() {
        when(bindingResult.hasErrors()).thenReturn(false);
        PhotoForm form = new PhotoForm();
        form.setTitle("New Title");
        form.setCategory("New Cat");
        form.setDescription("New Desc");
        form.setPrice(BigDecimal.ONE);

        String view = photoController.update(UUID.randomUUID(), form, bindingResult, redirectAttributes);

        assertEquals("redirect:/photos", view);
        verify(photoService).updatePhotoData(any(), eq("New Title"), eq("New Cat"), eq("New Desc"), eq(BigDecimal.ONE));
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
    }

    // --- TEST UPDATE IMAGE ---

    @Test
    void testUpdateImage_FileEmpty() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(true);
        UUID id = UUID.randomUUID();

        String view = photoController.updateImage(id, multipartFile, redirectAttributes);

        assertEquals("redirect:/photos/" + id, view);
        verify(redirectAttributes).addFlashAttribute("error", "Pilih file gambar baru.");
    }

    @Test
    void testUpdateImage_Success_WithOldFile() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        mockPhoto.setFilename("old-file.jpg");
        when(photoService.getPhotoById(any(UUID.class))).thenReturn(mockPhoto);
        when(fileStorageService.storeFile(any(), any())).thenReturn("new-file.jpg");

        UUID id = mockPhoto.getId();
        String view = photoController.updateImage(id, multipartFile, redirectAttributes);

        assertEquals("redirect:/photos/" + id, view);
        verify(fileStorageService).deleteFile("old-file.jpg"); // Verify old file deleted
        verify(photoService).updatePhotoFile(id, "new-file.jpg");
        verify(redirectAttributes).addFlashAttribute("success", "Gambar berhasil diganti.");
    }

    @Test
    void testUpdateImage_Success_NoOldFile() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        mockPhoto.setFilename(null); // No old file
        when(photoService.getPhotoById(any(UUID.class))).thenReturn(mockPhoto);
        when(fileStorageService.storeFile(any(), any())).thenReturn("new-file.jpg");

        UUID id = mockPhoto.getId();
        String view = photoController.updateImage(id, multipartFile, redirectAttributes);

        assertEquals("redirect:/photos/" + id, view);
        verify(fileStorageService, never()).deleteFile(anyString()); // Verify delete NOT called
        verify(photoService).updatePhotoFile(id, "new-file.jpg");
    }

    // --- TEST DELETE ---

    @Test
    void testDelete_PhotoNotFound() {
        when(photoService.getPhotoById(any(UUID.class))).thenReturn(null);

        String view = photoController.delete(UUID.randomUUID(), redirectAttributes);

        assertEquals("redirect:/photos", view);
        verify(photoService, never()).deletePhoto(any());
    }

    @Test
    void testDelete_Success_WithFile() {
        mockPhoto.setFilename("existing.jpg");
        when(photoService.getPhotoById(any(UUID.class))).thenReturn(mockPhoto);

        String view = photoController.delete(mockPhoto.getId(), redirectAttributes);

        assertEquals("redirect:/photos", view);
        verify(fileStorageService).deleteFile("existing.jpg");
        verify(photoService).deletePhoto(mockPhoto.getId());
        verify(redirectAttributes).addFlashAttribute("success", "Foto berhasil dihapus.");
    }
    
    @Test
    void testDelete_Success_NoFile() {
        mockPhoto.setFilename(null);
        when(photoService.getPhotoById(any(UUID.class))).thenReturn(mockPhoto);

        String view = photoController.delete(mockPhoto.getId(), redirectAttributes);

        assertEquals("redirect:/photos", view);
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(photoService).deletePhoto(mockPhoto.getId());
    }

    // --- TEST CHART ---

    @Test
    void testChart_LoggedIn() {
        mockSecurityContext(mockUser);
        when(photoService.getChartData(any(UUID.class))).thenReturn(new ArrayList<>());

        String view = photoController.chart(model);

        assertEquals("pages/photos/chart", view);
        verify(model).addAttribute(eq("chartData"), anyList());
    }

    @Test
    void testChart_NotLoggedIn() {
        mockSecurityContext(null);

        String view = photoController.chart(model);

        assertEquals("redirect:/auth/login", view);
    }
    @Test
    void testStore_FileNull() throws IOException {
        // Setup: Tidak ada error binding awal
        when(bindingResult.hasErrors()).thenReturn(false);
        
        // Setup: Form dengan file NULL
        PhotoForm form = new PhotoForm();
        form.setFile(null); // Kondisi kiri (form.getFile() == null) bernilai TRUE

        // Execute
        String view = photoController.store(form, bindingResult, redirectAttributes);

        // Assert
        assertEquals("pages/photos/form", view);
        // Pastikan rejectValue tetap terpanggil
        verify(bindingResult).rejectValue("file", "error.file", "File foto wajib diunggah");
    }
    @Test
    void testIndex_AuthIsNull() {
        // 1. Setup SecurityContext agar getAuthentication() mengembalikan NULL
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null); 
        SecurityContextHolder.setContext(securityContext);

        // 2. Panggil controller (akan memicu getAuthUser)
        String view = photoController.index(model);

        // 3. Assert: Karena auth null, dianggap tidak login -> redirect
        assertEquals("redirect:/auth/login", view);
    }
    @Test
    void testIndex_PrincipalNotUserInstance() {
        // 1. Setup Authentication valid, TAPI principalnya String (bukan Object User)
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("anonymousUser"); // <--- Bukan instance of User

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // 2. Panggil controller
        String view = photoController.index(model);

        // 3. Assert: Karena bukan User, dianggap tidak login -> redirect
        assertEquals("redirect:/auth/login", view);
    }
}