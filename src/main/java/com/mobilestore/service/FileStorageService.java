package com.mobilestore.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class FileStorageService {

    @Autowired
    private Cloudinary cloudinary;

    public String saveImage(MultipartFile file) throws IOException {
        // This method uploads the multipart file to Cloudinary and returns the remote URL
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    public void deleteImage(String imageUrl) {
        // To delete an image from Cloudinary, you need to extract the 'public_id' from the URL
        // and call cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        // If you don't implement this, the old image will just remain orphaned on Cloudinary.
    }
}