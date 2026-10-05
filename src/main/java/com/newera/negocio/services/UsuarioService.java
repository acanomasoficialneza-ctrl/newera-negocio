package com.newera.negocio.services;

import com.newera.negocio.models.RegistroRequest;
import com.newera.negocio.models.UsuariosAuth;
import com.newera.negocio.models.PerfilCliente;
import com.newera.negocio.models.PerfilAdmin;
import com.newera.negocio.repositories.UsuariosAuthRepository;
import com.newera.negocio.repositories.PerfilClienteRepository;
import com.newera.negocio.repositories.PerfilAdminRepository;
import com.newera.negocio.services.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuariosAuthRepository authRepository;
    private final PerfilClienteRepository clienteRepository;
    private final PerfilAdminRepository adminRepository;
    private final AuditoriaService auditoriaService;
    private final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    @Transactional
    public UsuariosAuth registrarUsuario(RegistroRequest request, Integer callerId) {
        UsuariosAuth auth = new UsuariosAuth();
        auth.setCorreo(request.getCorreo());
        auth.setPass(request.getPass()); // TODO: encrypt
        
        String rol = (request.getRol() != null) ? request.getRol() : "CLIENTE";
        auth.setRol(rol);
        auth.setEstado("ACTIVO");

        auth = authRepository.save(auth);

        if ("CLIENTE".equalsIgnoreCase(rol)) {
            PerfilCliente cliente = new PerfilCliente();
            cliente.setUsuarioAuth(auth);
            cliente.setNombreCompleto(request.getNombreCompleto());
            cliente.setBalance(BigDecimal.ZERO);
            cliente.setMargenLibre(BigDecimal.ZERO);
            cliente.setMargenUtilizado(BigDecimal.ZERO);
            cliente.setEstadoKyc("PENDIENTE");
            cliente.setUrlFotoPerfil("/assets/images/default-avatar.png");
            cliente.setUrlIneFrente("/assets/images/default-ine.png");
            cliente.setFechaRegistro(LocalDateTime.now());
            clienteRepository.save(cliente);

            // Send notification to Supremo
            try {
                java.util.Map<String, Object> req = new java.util.HashMap<>();
                req.put("idUsuarioDestino", 1); // Supremo ID
                req.put("correoDestino", "supremo@newera.com");
                req.put("titulo", "Nuevo Cliente Registrado");
                req.put("mensaje", "El cliente " + request.getNombreCompleto() + " se ha registrado. Por favor asigne un administrador.");
                req.put("tipo", "WARNING");
                restTemplate.postForObject("http://localhost:8084/api/v1/notificaciones/enviar", req, Object.class);
            } catch (Exception e) {
                System.err.println("Error enviando notificacion a Supremo: " + e.getMessage());
            }
        } else {
            PerfilAdmin admin = new PerfilAdmin();
            admin.setUsuarioAuth(auth);
            admin.setNombreCompleto(request.getNombreCompleto());
            admin.setDepartamento("DIRECCION"); // Default
            admin.setFechaRegistro(LocalDateTime.now());
            adminRepository.save(admin);
        }

        auditoriaService.registrarLog("AUTH", "Nuevo usuario (" + rol + ") registrado - Nombre: " + request.getNombreCompleto() + " (" + request.getCorreo() + ")", "127.0.0.1", callerId);

        return auth;
    }
}
