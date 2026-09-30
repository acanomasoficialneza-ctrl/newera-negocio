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

@Service
public class FileStorageService {

    private final String baseUploadDir = "E:\\casino\\newEra\\clientes";

    public String saveProfileImage(MultipartFile file, Integer userId) throws IOException {
        return saveDocument(file, userId, "foto_perfil");
    }

    public String saveDocument(MultipartFile file, Integer userId, String prefix) throws IOException {
        Path clientDir = Paths.get(baseUploadDir, String.valueOf(userId));
        
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

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String newFilename = prefix + extension;
        Path filePath = clientDir.resolve(newFilename);
        
        file.transferTo(filePath.toFile());

        // Devolvemos la ruta absoluta para que getMedia la encuentre sin problemas
        return filePath.toAbsolutePath().toString();
    }
}
