package org.delcom.app.services;

import org.delcom.app.entities.Photo;
import org.delcom.app.repositories.PhotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PhotoService {
    private final PhotoRepository photoRepository;

    public PhotoService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public List<Photo> getAllPhotos(UUID userId) {
        return photoRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Photo getPhotoById(UUID id) {
        return photoRepository.findById(id).orElse(null);
    }

    @Transactional
    public Photo createPhoto(Photo photo) {
        return photoRepository.save(photo);
    }

    @Transactional
    public Photo updatePhotoData(UUID id, String title, String category, String desc, java.math.BigDecimal price) {
        Photo photo = getPhotoById(id);
        if (photo != null) {
            photo.setTitle(title);
            photo.setCategory(category);
            photo.setDescription(desc);
            photo.setPrice(price);
            return photoRepository.save(photo);
        }
        return null;
    }

    @Transactional
    public void updatePhotoFile(UUID id, String filename) {
        Photo photo = getPhotoById(id);
        if (photo != null) {
            photo.setFilename(filename);
            photoRepository.save(photo);
        }
    }

    @Transactional
    public void deletePhoto(UUID id) {
        photoRepository.deleteById(id);
    }
    
    public List<Object[]> getChartData(UUID userId) {
        return photoRepository.countPhotosByCategory(userId);
    }
}