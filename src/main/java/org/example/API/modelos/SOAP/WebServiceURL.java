package org.example.API.modelos.SOAP;

import org.example.contoladores.LoginController;
import org.example.modelos.TipoUsuario;
import org.example.API.DTO.UrlDTO;
import org.example.modelos.Url;
import org.example.modelos.Usuario;
import org.example.servicios.UrlServicios;
import org.example.servicios.UsuarioServicios;
import org.example.utils.Parseador;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import org.eclipse.jetty.util.log.Log;
import org.jasypt.util.text.StrongTextEncryptor;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

@WebService
public class WebServiceURL implements Serializable {
    private StrongTextEncryptor encriptador;
    private UrlServicios urlServicios = UrlServicios.getInstance();

    @WebMethod
    public ArrayList<UrlDTO> getURLS(){
        ArrayList<UrlDTO> urlsDTO = new ArrayList<>();
        urlServicios.getUrls().forEach(url->{
            UrlDTO urlDto = null;
            try{
                urlDto = new UrlDTO(url);
            } catch (Exception e){
                throw new RuntimeException(e);
            }
            urlsDTO.add(urlDto);
        });
        return urlsDTO;
    }
    @WebMethod
    public UrlDTO crearURL(String urlOriginal, String clienteSOAP, String password) throws IOException{
        UrlDTO urlDTO;
        Usuario usuarioSOAP;
        if(clienteSOAP.equals("Invitado")){
            usuarioSOAP = new Usuario("Invitado", "Invitado", "Invitado", "Invitado", TipoUsuario.INVITADO);
        }
        else
            usuarioSOAP = Autenticar(clienteSOAP, password);
        if (usuarioSOAP == null){
            return null;
        }
        String hash = UrlServicios.getInstance().crearUrl(urlOriginal, Parseador.getTitulo(urlOriginal), usuarioSOAP);
        Url url = UrlServicios.getInstance().buscarUrlPorHash(hash);
        try {
            urlDTO = new UrlDTO(url);
            return urlDTO;
        } catch (Exception e) {
            return null;
        }
    }

    @WebMethod
    public ArrayList<UrlDTO> getURLSFromUser(String username, String password){
        Usuario usuarioSOAP = Autenticar(username, password);
        if(usuarioSOAP== null){
            return null;
        }
        ArrayList<UrlDTO> urlsDTO = new ArrayList<>();
        urlServicios.getUrlsUsuario(usuarioSOAP.getUsuario()).forEach(url->{
            try{
                urlsDTO.add(new UrlDTO(url));
            }
            catch (Exception e){
                throw new RuntimeException(e);
            }
        });
        return urlsDTO;
    }
    public Usuario Autenticar(String usuarioID, String password){
        Usuario user = UsuarioServicios.getInstance().getUsuarioByUsuario(usuarioID);
        if(user != null){
            encriptador = new StrongTextEncryptor();
            encriptador.setPassword("ExamenFinal");
            if(user.isHabilitado() && encriptador.decrypt(user.getPassword()).equals(password)){
                return user;
            }
            else{
                return null;
            }
        }
        else{
            return null;
        }
    }

}
