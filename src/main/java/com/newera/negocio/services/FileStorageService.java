package com.newera.negocio.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:#{null}}")
    private String configuredUploadDir;

    private String getBaseUploadDir() {
        if (configuredUploadDir != null && !configuredUploadDir.isEmpty()) {
            return configuredUploadDir;
        }
        // Auto-detect OS
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return "E:\\casino\\newEra\\clientes";
        } else {
            return "/var/www/shared/assets/clientes";
        }
    }

    public String saveProfileImage(MultipartFile file, Integer userId) throws IOException {
        return saveDocument(file, userId, "foto_perfil");
    }

    private static final java.util.List<String> ALLOWED_EXTENSIONS = java.util.List.of(".jpg", ".jpeg", ".png", ".webp");
    private static final java.util.List<String> ALLOWED_CONTENT_TYPES = java.util.List.of(
        "image/jpeg", "image/png", "image/webp"
    );

    public String saveDocument(MultipartFile file, Integer userId, String prefix) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo proporcionado está vacío.");
        }

        // Validar tipo de archivo básico
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Solo se aceptan imágenes JPG, PNG o WEBP.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            extension = ".jpg"; // Normalizar si la extensión original difiere pero el mime-type es válido
        }

        Path clientDir = Paths.get(getBaseUploadDir(), String.valueOf(userId));
        
        if (!Files.exists(clientDir)) {
            Files.createDirectories(clientDir);
        }

        // Borrar imagen anterior con el mismo prefijo
        try (Stream<Path> files = Files.list(clientDir)) {
            files.filter(p -> p.getFileName().toString().startsWith(prefix + "."))
                 .forEach(p -> {
                     try {
                         Files.delete(p);
                     } catch (IOException e) {
                         System.err.println("No se pudo eliminar archivo anterior: " + p);
                     }
                 });
        }

        String newFilename = prefix + extension;
        Path filePath = clientDir.resolve(newFilename);
        
        file.transferTo(filePath.toFile());

        // Devolvemos la URL pública para que el frontend pueda consumirla directamente
        return "https://65.38.98.77/assets/clientes/" + userId + "/" + newFilename;
    }
}
