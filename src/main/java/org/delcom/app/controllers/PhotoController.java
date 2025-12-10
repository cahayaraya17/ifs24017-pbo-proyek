package org.delcom.app.controllers;

import jakarta.validation.Valid;
import org.delcom.app.dto.PhotoForm;
import org.delcom.app.entities.Photo;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.PhotoService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;
    private final FileStorageService fileStorageService;

    public PhotoController(PhotoService photoService, FileStorageService fileStorageService) {
        this.photoService = photoService;
        this.fileStorageService = fileStorageService;
    }

    // Helper method untuk mengambil User yang sedang login
    private User getAuthUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    @GetMapping
    public String index(Model model) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/login";

        List<Photo> photos = photoService.getAllPhotos(user.getId());
        model.addAttribute("photos", photos);
        return "pages/photos/index"; // Asumsi path view
    }

    @GetMapping("/create")
    public String create(Model model) {
        if (getAuthUser() == null) return "redirect:/auth/login";
        model.addAttribute("photoForm", new PhotoForm());
        return "pages/photos/create";
    }

    @PostMapping("/create")
    public String store(@Valid @ModelAttribute PhotoForm photoForm, 
                        BindingResult result, 
                        RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/login";

        // Validasi file
        if (photoForm.getFile() == null || photoForm.getFile().isEmpty()) {
            result.rejectValue("file", "file.required", "Gambar harus diupload.");
        }

        if (result.hasErrors()) {
            return "pages/photos/create";
        }

        // 1. Simpan data Photo (tanpa filename dulu)
        Photo newPhoto = new Photo(
            user.getId(),
            photoForm.getTitle(),
            photoForm.getCategory(),
            photoForm.getDescription(),
            photoForm.getPrice()
        );
        newPhoto = photoService.createPhoto(newPhoto);

        try {
            // 2. Simpan file fisik
            String filename = fileStorageService.storeFile(photoForm.getFile(), newPhoto.getId());
            
            // 3. Update data Photo dengan filename
            photoService.updatePhotoFile(newPhoto.getId(), filename);

        } catch (IOException e) {
            // Jika gagal simpan file, hapus data photo yang sudah terbuat
            photoService.deletePhoto(newPhoto.getId());
            redirectAttributes.addFlashAttribute("error", "Gagal menyimpan file: " + e.getMessage());
            return "redirect:/photos/create";
        }

        redirectAttributes.addFlashAttribute("success", "Foto berhasil disimpan.");
        return "redirect:/photos";
    }
    
    @GetMapping("/{id}")
    public String show(@PathVariable UUID id, Model model) {
        if (getAuthUser() == null) return "redirect:/auth/login";

        Photo photo = photoService.getPhotoById(id);
        if (photo == null || !photo.getUserId().equals(getAuthUser().getId())) {
            return "redirect:/photos";
        }
        
        model.addAttribute("photo", photo);
        model.addAttribute("photoForm", new PhotoForm());
        return "pages/photos/show";
    }

    @PostMapping("/{id}/update")
    public String updateData(@PathVariable UUID id, 
                             @Valid @ModelAttribute("photoForm") PhotoForm photoForm, 
                             BindingResult result, 
                             RedirectAttributes redirectAttributes) {
        if (getAuthUser() == null) return "redirect:/auth/login";

        Photo existingPhoto = photoService.getPhotoById(id);
        if (existingPhoto == null || !existingPhoto.getUserId().equals(getAuthUser().getId())) {
            redirectAttributes.addFlashAttribute("error", "Foto tidak ditemukan.");
            return "redirect:/photos";
        }

        // Karena kita hanya mengupdate data teks, kita tidak validasi file
        // (File akan divalidasi di endpoint /update-file)
        if (result.hasFieldErrors("title") || result.hasFieldErrors("category") || result.hasFieldErrors("price")) {
            // Jika ada error pada field lain, tampilkan kembali halaman show
            return show(id, new org.springframework.ui.ConcurrentModel()); 
        }

        // Update data
        photoService.updatePhotoData(
            id, 
            photoForm.getTitle(), 
            photoForm.getCategory(), 
            photoForm.getDescription(), 
            photoForm.getPrice()
        );

        redirectAttributes.addFlashAttribute("success", "Data foto berhasil diupdate.");
        return "redirect:/photos/" + id;
    }

    @PostMapping("/{id}/update-file")
    public String updateFile(@PathVariable UUID id, 
                             @RequestParam("file") MultipartFile file, 
                             RedirectAttributes redirectAttributes) throws IOException {
        if (getAuthUser() == null) return "redirect:/auth/login";

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Pilih file gambar baru.");
            return "redirect:/photos/" + id;
        }

        Photo photo = photoService.getPhotoById(id);
        if (photo == null || !photo.getUserId().equals(getAuthUser().getId())) {
            redirectAttributes.addFlashAttribute("error", "Foto tidak ditemukan.");
            return "redirect:/photos";
        }

        // Hapus file lama jika ada
        if(photo.getFilename() != null && fileStorageService.fileExists(photo.getFilename())) {
            fileStorageService.deleteFile(photo.getFilename());
        }

        // Simpan file baru
        try {
            String newFilename = fileStorageService.storeFile(file, id);
            photoService.updatePhotoFile(id, newFilename);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menyimpan file baru: " + e.getMessage());
            return "redirect:/photos/" + id;
        }

        redirectAttributes.addFlashAttribute("success", "Gambar berhasil diganti.");
        return "redirect:/photos/" + id;
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        if (getAuthUser() == null) return "redirect:/auth/login";

        Photo photo = photoService.getPhotoById(id);
        if (photo != null && photo.getUserId().equals(getAuthUser().getId())) {
            // 1. Hapus file fisik
            if (photo.getFilename() != null) {
                fileStorageService.deleteFile(photo.getFilename());
            }
            // 2. Hapus data dari DB
            photoService.deletePhoto(id);
            redirectAttributes.addFlashAttribute("success", "Foto berhasil dihapus.");
        }
        return "redirect:/photos";
    }
    
    @GetMapping("/chart")
    public String chart(Model model) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/login";

        List<Object[]> chartData = photoService.getChartData(user.getId());
        model.addAttribute("chartData", chartData);
        // Map data ke format yang mudah dibaca JS/View (opsional, bisa dilakukan di view)
        
        return "pages/photos/chart";
    }
}