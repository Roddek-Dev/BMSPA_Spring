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
    // Opción 1: Directorio en el home del usuario
    private final String storagePath = System.getProperty("user.home") + "/barberspa/images/";

    // Opción 2: Directorio en /tmp (se borra al reiniciar)
    // private final String storagePath = "/tmp/barberspa/images/";

    // Opción 3: Directorio en tu proyecto
    // private final String storagePath = "./uploads/images/";

    // Opción 4: Usando variable de entorno (más profesional)
    // private final String storagePath = System.getenv("BARBERSPA_IMAGES_PATH") != null ?
    //     System.getenv("BARBERSPA_IMAGES_PATH") : System.getProperty("user.home") + "/barberspa/images/";

    public String saveImages(MultipartFile file, String nombre) throws IOException {
        if (file == null || file.isEmpty()) {
            return "default.jpg";
        }

        // Validar tipo de archivo
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }

        // Crear directorio si no existe (con permisos para Linux)
        File directory = new File(storagePath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("No se pudo crear el directorio: " + storagePath);
            }
            // Establecer permisos de lectura/escritura en Linux
            directory.setReadable(true, false);
            directory.setWritable(true, false);
        }

        // Generar nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = "profile_" + nombre + "_" + System.currentTimeMillis() + extension;

        // Guardar el archivo
        Path path = Paths.get(storagePath + newFilename);
        Files.write(path, file.getBytes());

        // Establecer permisos del archivo en Linux
        File savedFile = path.toFile();
        savedFile.setReadable(true, false);
        savedFile.setWritable(true, true);

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

    // Método auxiliar para obtener la ruta completa
    public String getStoragePath() {
        return storagePath;
    }
}