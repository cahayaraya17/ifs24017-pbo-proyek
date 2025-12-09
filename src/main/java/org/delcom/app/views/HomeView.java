package org.delcom.app.views;

import org.delcom.app.entities.Photo;
import org.delcom.app.entities.User;
import org.delcom.app.services.PhotoService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Controller
public class HomeView {

    private final UserService userService;
    private final PhotoService photoService;

    public HomeView(UserService userService, PhotoService photoService) {
        this.userService = userService;
        this.photoService = photoService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // 1. Ambil Authentication dari Context Holder (Lebih aman daripada Principal)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        // Cek apakah principal yang login adalah tipe User kita
        if (auth != null && auth.getPrincipal() instanceof User) {
            // Casting langsung dari sesi (tidak perlu query DB by email lagi)
            user = (User) auth.getPrincipal(); 
            
            // Refresh data user dari DB untuk memastikan data terbaru (opsional tapi disarankan)
            user = userService.getUserById(user.getId());
        }

        // Kirim object user ke template
        model.addAttribute("user", user);

        if (user != null) {
            // 2. Ambil semua foto
            List<Photo> photos = photoService.getAllPhotos(user.getId());
            
            // Jaga-jaga jika photos null (walaupun repository biasanya return empty list)
            if (photos == null) {
                photos = Collections.emptyList();
            }

            // 3. Hitung Statistik
            int totalPhotos = photos.size();

            long totalCategories = photos.stream()
                    .map(Photo::getCategory)
                    .filter(Objects::nonNull) // Hindari null category
                    .distinct()
                    .count();

            BigDecimal totalPortfolioValue = photos.stream()
                    .map(Photo::getPrice)
                    .filter(Objects::nonNull) // PENTING: Filter harga yang null agar tidak Error 500
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 4. Masukkan ke Model
            model.addAttribute("totalPhotos", totalPhotos);
            model.addAttribute("totalCategories", totalCategories);
            model.addAttribute("totalPortfolioValue", totalPortfolioValue);
        } else {
            // Default value jika user belum login/sesi habis
            model.addAttribute("totalPhotos", 0);
            model.addAttribute("totalCategories", 0);
            model.addAttribute("totalPortfolioValue", BigDecimal.ZERO);
        }

        // Pastikan ConstUtil sudah ada TEMPLATE_PAGES_HOME
        return ConstUtil.TEMPLATE_PAGES_HOME; 
    }
}