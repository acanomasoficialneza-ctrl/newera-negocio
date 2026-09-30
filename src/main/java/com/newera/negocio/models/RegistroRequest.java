package com.newera.negocio.models;

import lombok.Data;

@Data
public class RegistroRequest {
    private String correo;
    private String pass;
    private String nombreCompleto;
    private String rol;
}
