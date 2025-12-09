package org.delcom.app.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "photos")
public class Photo {

    // --- 1. Mandatory Attribute: ID ---
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    // --- 2. Mandatory Attribute: UserID ---
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // --- 3. Attribute: Title ---
    @Column(nullable = false)
    private String title;

    // --- 4. Attribute: Category (e.g., Portrait, Landscape, Abstract) ---
    @Column(nullable = false)
    private String category;

    // --- 5. Attribute: Description ---
    @Column(columnDefinition = "TEXT")
    private String description;

    // --- 6. Attribute: Price (Selling price) ---
    @Column(nullable = false)
    private BigDecimal price;

    // --- 7. Attribute: Filename (Image Path) ---
    @Column(name = "filename")
    private String filename;

    // --- 8. Mandatory Attribute: CreatedAt ---
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- 9. Mandatory Attribute: UpdatedAt ---
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Photo() {}

    // Constructor custom
    public Photo(UUID userId, String title, String category, String description, BigDecimal price) {
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.description = description;
        this.price = price;
    }

    // Getters Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}