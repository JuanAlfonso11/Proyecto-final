package org.example.API.modelos.SOAP;

import org.example.modelos.TipoUsuario;
import org.example.modelos.Usuario;
import org.example.servicios.UsuarioServicios;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import java.io.Serializable;

@WebService
public class WebServiceUsuario {
    private UsuarioServicios usuarioServicios = UsuarioServicios.getInstance();

    @WebMethod
    public Usuario crearUsuario(String username, String nombre, String correo, String password){
        if (!usuarioServicios.existeUsuario(username)){
            Usuario clienteSOAP = new Usuario(username, nombre,correo, password, TipoUsuario.CLIENTE);
            return usuarioServicios.addUsuario(clienteSOAP);
        }
            return null;
    }
}
