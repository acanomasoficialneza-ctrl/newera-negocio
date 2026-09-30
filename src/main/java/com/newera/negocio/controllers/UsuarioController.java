package com.newera.negocio.controllers;

import com.newera.negocio.models.RegistroRequest;
import com.newera.negocio.models.UsuariosAuth;
import com.newera.negocio.models.PerfilCliente;
import com.newera.negocio.repositories.UsuariosAuthRepository;
import com.newera.negocio.repositories.PerfilClienteRepository;
import com.newera.negocio.repositories.PerfilAdminRepository;
import com.newera.negocio.models.PerfilAdmin;
import com.newera.negocio.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuariosAuthRepository authRepository;
    private final PerfilClienteRepository clienteRepository;
    private final PerfilAdminRepository adminRepository;
    private final FileStorageService fileStorageService;
    private final com.newera.negocio.services.UsuarioService usuarioService;
    private final com.newera.negocio.services.AuditoriaService auditoriaService;
    private final com.newera.negocio.repositories.AsignacionClienteRepository asignacionClienteRepository;

    @PostMapping("/registro")
    public ResponseEntity<UsuariosAuth> registrarUsuario(@RequestBody RegistroRequest request) {
        return ResponseEntity.ok(usuarioService.registrarUsuario(request, null));
    }

    @PutMapping("/{id}/documentos")
    public ResponseEntity<String> uploadDocumentos(
            @PathVariable Integer id, 
            @RequestParam(value = "fileFrente", required = false) MultipartFile fileFrente, 
            @RequestParam(value = "fileReverso", required = false) MultipartFile fileReverso) {
        try {
            PerfilCliente cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));
            
            boolean updated = false;
            if (fileFrente != null) {
                String urlFrente = fileStorageService.saveDocument(fileFrente, id, "ine_frente");
                cliente.setUrlIneFrente(urlFrente);
                updated = true;
            }
            if (fileReverso != null) {
                String urlReverso = fileStorageService.saveDocument(fileReverso, id, "ine_reverso");
                cliente.setUrlIneReverso(urlReverso);
                updated = true;
            }
            
            if (updated) {
                cliente.setEstadoKyc("PENDIENTE");
                clienteRepository.save(cliente);
            }
            
            return ResponseEntity.ok("Documentos guardados exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al subir documentos: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/foto-perfil")
    public ResponseEntity<String> updateProfileImage(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageService.saveProfileImage(file, id);
            
            PerfilCliente cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));
            
            cliente.setUrlFotoPerfil(fileUrl);
            clienteRepository.save(cliente);
            
            return ResponseEntity.ok("Imagen guardada en: " + fileUrl);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al subir imagen: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuario(@PathVariable Integer id) {
        UsuariosAuth auth = authRepository.findById(id).orElse(null);
        if (auth == null) return ResponseEntity.notFound().build();

        if ("CLIENTE".equals(auth.getRol())) {
            return clienteRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } else {
            return adminRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
    }

    @GetMapping("/media")
    public ResponseEntity<org.springframework.core.io.Resource> getMedia(@RequestParam String path) {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(path);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                String contentType = java.nio.file.Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsuarios() {
        return ResponseEntity.ok(authRepository.findAll());
    }

    @GetMapping("/clientes")
    public ResponseEntity<?> getClientes() {
        return ResponseEntity.ok(clienteRepository.findAll());
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getAdmins() {
        return ResponseEntity.ok(adminRepository.findAll());
    }

    @PostMapping("/registro/admin")
    public ResponseEntity<?> registrarAdmin(@RequestBody java.util.Map<String, String> body, @RequestHeader(value = "X-Auth-User-Id", required = false) String callerIdStr) {
        RegistroRequest request = new RegistroRequest();
        request.setCorreo(body.get("correo"));
        request.setPass(body.get("pass"));
        request.setNombreCompleto(body.get("nombreCompleto"));
        request.setRol(body.getOrDefault("rol", "ADMIN"));
        
        Integer callerId = (callerIdStr != null && !callerIdStr.isBlank() && !callerIdStr.equals("null")) ? Integer.valueOf(callerIdStr) : null;
        UsuariosAuth auth = usuarioService.registrarUsuario(request, callerId);
        
        PerfilAdmin admin = adminRepository.findById(auth.getIdUsuario()).orElseThrow();
        if (body.containsKey("telefono")) admin.setTelefono(body.get("telefono"));
        if (body.containsKey("departamento")) admin.setDepartamento(body.get("departamento"));
        adminRepository.save(admin);
        
        return ResponseEntity.ok(admin);
    }

    @org.springframework.beans.factory.annotation.Value("${app.defaults.foto-perfil:E:\\casino\\newEra\\fotoGenerica.png}")
    private String defaultFotoPerfil;

    @org.springframework.beans.factory.annotation.Value("${app.defaults.foto-credencial:E:\\casino\\newEra\\credencial.png}")
    private String defaultFotoCredencial;

    @PostMapping("/registro/cliente")
    public ResponseEntity<?> registrarClienteAdmin(@RequestBody java.util.Map<String, String> body, @RequestHeader(value = "X-Auth-User-Id", required = false) String callerIdStr) {
        RegistroRequest request = new RegistroRequest();
        request.setCorreo(body.get("correo"));
        request.setPass(body.get("pass"));
        request.setNombreCompleto(body.get("nombreCompleto"));
        request.setRol("CLIENTE");
        
        Integer callerId = (callerIdStr != null && !callerIdStr.isBlank() && !callerIdStr.equals("null")) ? Integer.valueOf(callerIdStr) : null;
        UsuariosAuth auth = usuarioService.registrarUsuario(request, callerId);
        
        PerfilCliente cliente = clienteRepository.findById(auth.getIdUsuario()).orElseThrow();
        if (body.containsKey("telefono")) cliente.setTelefono(body.get("telefono"));
        if (body.containsKey("estadoResidencia")) cliente.setEstadoResidencia(body.get("estadoResidencia"));
        if (body.containsKey("nombrePila")) cliente.setNombrePila(body.get("nombrePila"));
        if (body.containsKey("fechaNacimiento") && body.get("fechaNacimiento") != null && !body.get("fechaNacimiento").isBlank()) {
            cliente.setFechaNacimiento(java.sql.Date.valueOf(java.time.LocalDate.parse(body.get("fechaNacimiento"))));
        }
        if (body.containsKey("profesion")) cliente.setProfesion(body.get("profesion"));
        if (body.containsKey("experienciaTrading")) cliente.setExperienciaTrading(body.get("experienciaTrading"));
        if (body.containsKey("hobbie")) cliente.setHobbie(body.get("hobbie"));
        
        cliente.setEstadoKyc("APROBADO");
        cliente.setBalance(java.math.BigDecimal.ZERO);
        cliente.setMargenUtilizado(java.math.BigDecimal.ZERO);
        cliente.setMargenLibre(java.math.BigDecimal.ZERO);
        cliente.setUrlFotoPerfil(defaultFotoPerfil);
        cliente.setUrlIneFrente(defaultFotoCredencial);
        cliente.setUrlIneReverso(defaultFotoCredencial);

        clienteRepository.save(cliente);
        
        // Asignar por default a todos los roles "DIRECTOR"
        java.util.List<UsuariosAuth> directores = authRepository.findByRol("DIRECTOR");
        for (UsuariosAuth dir : directores) {
            com.newera.negocio.models.AsignacionCliente asignacion = new com.newera.negocio.models.AsignacionCliente();
            asignacion.setIdCliente(cliente.getIdUsuario());
            asignacion.setIdAdmin(dir.getIdUsuario());
            asignacion.setFechaAsignacion(java.time.LocalDateTime.now());
            asignacionClienteRepository.save(asignacion);
        }
        
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<?> updatePerfil(@PathVariable Integer id, @RequestBody java.util.Map<String, Object> body, @RequestHeader(value = "X-Auth-User-Id", required = false) String callerIdStr) {
        UsuariosAuth auth = authRepository.findById(id).orElse(null);
        if (auth == null) return ResponseEntity.notFound().build();

        if ("CLIENTE".equals(auth.getRol())) {
            PerfilCliente cliente = clienteRepository.findById(id).orElseThrow();
            if (body.containsKey("nombreCompleto")) cliente.setNombreCompleto((String) body.get("nombreCompleto"));
            if (body.containsKey("nombrePila")) cliente.setNombrePila((String) body.get("nombrePila"));
            if (body.containsKey("telefono")) cliente.setTelefono((String) body.get("telefono"));
            if (body.containsKey("profesion")) cliente.setProfesion((String) body.get("profesion"));
            if (body.containsKey("estadoResidencia")) cliente.setEstadoResidencia((String) body.get("estadoResidencia"));
            if (body.containsKey("experienciaTrading")) cliente.setExperienciaTrading((String) body.get("experienciaTrading"));
            if (body.containsKey("hobbie")) cliente.setHobbie((String) body.get("hobbie"));
            if (body.containsKey("fechaNacimiento") && body.get("fechaNacimiento") != null && !((String)body.get("fechaNacimiento")).isBlank()) {
                cliente.setFechaNacimiento(java.sql.Date.valueOf(java.time.LocalDate.parse((String)body.get("fechaNacimiento"))));
            }
            
            boolean authModified = false;
            if (body.containsKey("pass")) {
                auth.setPass((String) body.get("pass"));
                authModified = true;
            }
            if (body.containsKey("correo")) {
                auth.setCorreo((String) body.get("correo"));
                authModified = true;
            }
            if (authModified) {
                authRepository.save(auth);
            }
            
            PerfilCliente savedCliente = clienteRepository.save(cliente);
            
            Integer callerId = (callerIdStr != null && !callerIdStr.isBlank() && !callerIdStr.equals("null")) ? Integer.valueOf(callerIdStr) : null;
            if (callerId != null) {
                auditoriaService.registrarLog("INFO", "ACTUALIZACION_PERFIL_CLIENTE: Se actualizó el perfil del cliente ID: " + id, "System", callerId);
            }
            
            return ResponseEntity.ok(savedCliente);
        } else {
            PerfilAdmin admin = adminRepository.findById(id).orElseThrow();
            if (body.containsKey("nombreCompleto")) admin.setNombreCompleto((String) body.get("nombreCompleto"));
            if (body.containsKey("telefono")) admin.setTelefono((String) body.get("telefono"));
            if (body.containsKey("departamento")) admin.setDepartamento((String) body.get("departamento"));
            
            if (body.containsKey("rol")) {
                auth.setRol((String) body.get("rol"));
                authRepository.save(auth);
            }
            if (body.containsKey("pass")) {
                auth.setPass((String) body.get("pass"));
                authRepository.save(auth);
            }
            Integer callerId = (callerIdStr != null && !callerIdStr.isBlank() && !callerIdStr.equals("null")) ? Integer.valueOf(callerIdStr) : null;
            auditoriaService.registrarLog("INFO", "Actualizó perfil de administrador - ID: " + id + " - Nombre: " + admin.getNombreCompleto(), "127.0.0.1", callerId);
            return ResponseEntity.ok(adminRepository.save(admin));
        }
    }

    @PutMapping("/{id}/kyc")
    public ResponseEntity<?> updateKyc(@PathVariable Integer id, @RequestParam String estado, @RequestHeader(value = "X-Auth-User-Id", required = false) String callerIdStr) {
        PerfilCliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) return ResponseEntity.notFound().build();
        cliente.setEstadoKyc(estado);
        
        Integer callerId = (callerIdStr != null && !callerIdStr.isBlank() && !callerIdStr.equals("null")) ? Integer.valueOf(callerIdStr) : null;
        auditoriaService.registrarLog("INFO", "KYC de cliente " + cliente.getNombreCompleto() + " (ID: " + id + ") cambiado a " + estado, "127.0.0.1", callerId);
        
        return ResponseEntity.ok(clienteRepository.save(cliente));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> updateEstado(@PathVariable Integer id, @RequestBody java.util.Map<String, String> body, @RequestHeader(value = "X-Auth-User-Id", required = false) String callerIdStr) {
        UsuariosAuth auth = authRepository.findById(id).orElse(null);
        if (auth == null) return ResponseEntity.notFound().build();
        if (body.containsKey("estado")) {
            String nuevoEstado = body.get("estado");
            if (!nuevoEstado.equals(auth.getEstado())) {
                auth.setEstado(nuevoEstado);
                authRepository.save(auth);
                Integer callerId = (callerIdStr != null && !callerIdStr.isBlank() && !callerIdStr.equals("null")) ? Integer.valueOf(callerIdStr) : null;
                String nombreUsuario = "Desconocido";
                PerfilCliente pc = clienteRepository.findById(id).orElse(null);
                if (pc != null) {
                    nombreUsuario = pc.getNombreCompleto();
                } else {
                    PerfilAdmin pa = adminRepository.findById(id).orElse(null);
                    if (pa != null) nombreUsuario = pa.getNombreCompleto();
                }
                auditoriaService.registrarLog("WARNING", "Cambió estado de acceso a " + nuevoEstado + " del usuario " + nombreUsuario + " (ID: " + id + ")", "127.0.0.1", callerId);
            }
        }
        return ResponseEntity.ok(auth);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("aum", 15800000);
        stats.put("volumen24h", 1240000);
        stats.put("riesgoVivo", 2540000);
        stats.put("flujoNeto", 890500);
        stats.put("usuariosTotales", authRepository.count());
        stats.put("adminsTotales", adminRepository.count());
        return ResponseEntity.ok(stats);
    }
}
