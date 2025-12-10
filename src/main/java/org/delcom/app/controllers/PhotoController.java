package org.delcom.app.controllers;

import jakarta.validation.Valid;
import org.delcom.app.dto.PhotoForm;
import org.delcom.app.entities.Photo;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.PhotoService;
// Import Security Context
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;
    private final FileStorageService fileStorageService;
    // Hapus AuthContext dari sini

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
        // Redirect login jika sesi habis
        if (user == null) return "redirect:/auth/login"; 
        
        List<Photo> photos = photoService.getAllPhotos(user.getId());
        model.addAttribute("photos", photos);
        return "pages/photos/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("photoForm", new PhotoForm());
        return "pages/photos/form";
    }

    @PostMapping("/store")
    public String store(@Valid @ModelAttribute("photoForm") PhotoForm form,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) throws IOException {
        if (result.hasErrors()) return "pages/photos/form";
        
        if (form.getFile() == null || form.getFile().isEmpty()) {
            result.rejectValue("file", "error.file", "File foto wajib diunggah");
            return "pages/photos/form";
        }

        User user = getAuthUser();
        if (user == null) return "redirect:/auth/login";

        Photo photo = new Photo(user.getId(), form.getTitle(), form.getCategory(), form.getDescription(), form.getPrice());
        Photo savedPhoto = photoService.createPhoto(photo);
        String filename = fileStorageService.storeFile(form.getFile(), savedPhoto.getId());
        photoService.updatePhotoFile(savedPhoto.getId(), filename);

        redirectAttributes.addFlashAttribute("success", "Foto berhasil ditambahkan ke portofolio.");
        return "redirect:/photos";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        Photo photo = photoService.getPhotoById(id);
        if (photo == null) return "redirect:/photos";
        model.addAttribute("photo", photo);
        return "pages/photos/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        Photo photo = photoService.getPhotoById(id);
        if (photo == null) return "redirect:/photos";

        PhotoForm form = new PhotoForm();
        form.setTitle(photo.getTitle());
        form.setCategory(photo.getCategory());
        form.setDescription(photo.getDescription());
        form.setPrice(photo.getPrice());
        
        model.addAttribute("photoForm", form);
        model.addAttribute("photoId", id);
        return "pages/photos/form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable UUID id, 
                         @Valid @ModelAttribute("photoForm") PhotoForm form,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "pages/photos/form";

        photoService.updatePhotoData(id, form.getTitle(), form.getCategory(), form.getDescription(), form.getPrice());
        redirectAttributes.addFlashAttribute("success", "Informasi foto berhasil diperbarui.");
        return "redirect:/photos";
    }

    @PostMapping("/{id}/update-image")
    public String updateImage(@PathVariable UUID id, 
                              @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                              RedirectAttributes redirectAttributes) throws IOException {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Pilih file gambar baru.");
            return "redirect:/photos/" + id;
        }

        Photo photo = photoService.getPhotoById(id);
        if(photo.getFilename() != null) {
            fileStorageService.deleteFile(photo.getFilename());
        }

        String newFilename = fileStorageService.storeFile(file, id);
        photoService.updatePhotoFile(id, newFilename);

        redirectAttributes.addFlashAttribute("success", "Gambar berhasil diganti.");
        return "redirect:/photos/" + id;
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        Photo photo = photoService.getPhotoById(id);
        if (photo != null) {
            if (photo.getFilename() != null) {
                fileStorageService.deleteFile(photo.getFilename());
            }
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
        return "pages/photos/chart";
    }
}