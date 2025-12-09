package org.delcom.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

public class PhotoForm {
    @NotBlank(message = "Judul foto harus diisi")
    private String title;

    @NotBlank(message = "Kategori harus dipilih")
    private String category;

    private String description;

    @NotNull(message = "Harga harus diisi")
    @Min(value = 0, message = "Harga tidak boleh negatif")
    private BigDecimal price;

    // Optional saat edit data teks, Wajib saat create (divalidasi di controller)
    private MultipartFile file;

    // Getters Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }
}