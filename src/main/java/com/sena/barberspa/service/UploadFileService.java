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
	
	// usa la misma ruta que en ResourceWebConfiguration
	private final String storagePath = "C:/images/";

	// metodo para subir la imagen del producto
	public String saveImages(MultipartFile file, String nombre) throws IOException {
		// validacion de imagenes
		if (!file.isEmpty()) {
			byte[] bytes = file.getBytes();
			// variable de tipo path que redirige al directo
			// se importa el path de .nio.file
			Path path = Paths.get(storagePath + nombre + "_" + file.getOriginalFilename());
			Files.write(path, bytes);
			return nombre + "_" + file.getOriginalFilename();
		}
		return "default.jpg";
	}
	
	//metodo para la eliminacion de la imagen del producto
	public void deleteImage(Object object) {
		String ruta = "C//images";
		File file = new File(ruta + object);
		file.delete();
	}
}
