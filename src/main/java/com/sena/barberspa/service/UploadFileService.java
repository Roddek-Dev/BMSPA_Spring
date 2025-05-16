package com.sena.barberspa.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadFileService {
    private final String storagePath = "C:/images/";

    public String saveImages(MultipartFile file, String nombre) throws IOException {
        if (file == null || file.isEmpty()) {
            return "default.jpg";
        }

        // Validar tipo de archivo
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }

        // Crear directorio si no existe
        File directory = new File(storagePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generar nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = "profile_" + nombre + "_" + System.currentTimeMillis() + extension;

        // Guardar el archivo
        Path path = Paths.get(storagePath + newFilename);
        Files.write(path, file.getBytes());

        return newFilename;
    }

    public void deleteImage(String filename) throws IOException {
        if (filename != null && !filename.isEmpty() && !"default.jpg".equals(filename)) {
            Path path = Paths.get(storagePath + filename);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }
    }
}
