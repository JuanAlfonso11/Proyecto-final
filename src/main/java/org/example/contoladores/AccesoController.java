package org.example.contoladores;
import org.example.modelos.TipoUsuario;
import org.example.modelos.Acceso;
import org.example.modelos.Url;
import org.example.modelos.Usuario;
import org.example.servicios.AccesoServicios;
import org.example.servicios.UrlServicios;
import org.example.servicios.UsuarioServicios;
import io.javalin.Javalin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.javalin.http.Context;
import org.jasypt.util.text.StrongTextEncryptor;

import static io.javalin.apibuilder.ApiBuilder.*;
public class AccesoController {
    Javalin app;
    StrongTextEncryptor encriptador;
    public AccesoController(Javalin app){
        this.app = app;
        this.encriptador = new StrongTextEncryptor();
        this.encriptador.setPassword("ExamenFinal");
    }


    public Boolean Autenticar(String usuarioID, String password){
        Usuario user = UsuarioServicios.getInstance().getUsuarioByUsuario(usuarioID);
        if(user != null){
            return user.isHabilitado() && encriptador.decrypt(user.getPassword()).equals(password);
        }
        else{
            return null;
        }
    }
    public void invalidarSesion(Context ctx){
        ctx.sessionAttribute("usuarioID_ExamenFinal", null);
        ctx.sessionAttribute("usuario_ExamenFinal", null);
        ctx.sessionAttribute("tipoUsuario_ExamenFinal", TipoUsuario.INVITADO);
        ctx.sessionAttribute("SExamenFinal", "null");
        ctx.cookie("ExamenFinal", "null");
    }
    public void invalidarCookie(Context ctx){
        ctx.cookie("ExamenFinal", "null");
    }
    public Boolean verificarURLSession(Context ctx) {
        ArrayList<Url> urlSession = ctx.sessionAttribute("urlSession");
        if (urlSession != null) {
            return true;
        } else {
            urlSession = new ArrayList<>();
            ctx.sessionAttribute("urlSession", urlSession);
            return false;
        }
    }

    public void rutas(){
        app.before(ctx ->{
            verificarURLSession(ctx);

            String cookie = ctx.cookie("ExamenFinal");
            if(cookie !=null){
                String[] cookieParts = cookie.split("-");
                if(cookieParts !=null && cookieParts.length==2){
                    if (cookieParts[0].equals("null") || cookieParts[1].equals("null")){
                        invalidarSesion(ctx);
                        return;
                    }
                    else {
                        String usuarioID = cookieParts[0];
                        String password = cookieParts[1];
                        encriptador = new StrongTextEncryptor();
                        encriptador.setPassword("ExamenFinal");
                        try {
                            usuarioID = encriptador.decrypt(usuarioID);
                            password = encriptador.decrypt(password);
                            if(Autenticar(usuarioID,password)!=null && Autenticar(usuarioID,password)){
                                ctx.sessionAttribute("SExamenFinal", cookie);
                            }
                            else{
                                invalidarSesion(ctx);
                            }
                        }catch (Exception e){
                            invalidarSesion(ctx);
                        }
                    }

                }
            }
            else {}
            String session = ctx.sessionAttribute("SExamenFinal");
            if(session==null){
                ctx.sessionAttribute("SExamenFinal", null);
            }
            else {
                String[] sessionParts = session.split("-");
                if(sessionParts !=null && sessionParts.length==2){
                    if(sessionParts[0].equals("null")||sessionParts[1].equals("null")){
                        invalidarSesion(ctx);
                    }
                    else {
                        String usuarioID = sessionParts[0];
                        String password = sessionParts[1];
                        encriptador = new StrongTextEncryptor();
                        encriptador.setPassword("ExamenFinal");
                        try{
                            usuarioID = encriptador.decrypt(usuarioID);
                            password = encriptador.decrypt(password);
                            if(Autenticar(usuarioID,password)!=null && Autenticar(usuarioID,password)){
                                Usuario user = UsuarioServicios.getInstance().getUsuarioByUsuario(usuarioID);
                                ctx.sessionAttribute("usuarioID_ExamenFinal",user.getUsuario());
                                ctx.sessionAttribute("usuario_ExamenFinal",user);
                                ctx.sessionAttribute("tipoUsuario_ExamenFinal",user.getTipoUsuario());
                            }
                            else {
                                invalidarSesion(ctx);
                                return;
                            }
                        }catch (Exception e){
                            invalidarSesion(ctx);
                            return;
                        }
                    }
                }
            }
            String path = ctx.path();
            if(!path.equals("/")){
                Matcher matcher = Pattern.compile("^/([a-zA-Z0-9]{7})$").matcher(path);
                if(matcher.matches()){
                    String hash = matcher.group(1);
                    Url url = UrlServicios.getInstance().buscarUrlPorHash(hash);
                    if(url!=null){
                        if(!url.isActivo()){
                        ctx.redirect("/");
                        return;
                    }
                    String ip=ctx.ip();
                    String userAgent = ctx.header("User-Agent");
                    String SO = ctx.header("sec-ch-ua-platform");
                    System.out.println("Navegador: "+userAgent);
                    System.out.println("IP: "+ip);
                    Acceso acceso = new Acceso(ip,userAgent,new Date(),url);
                    AccesoServicios.getInstance().crearAcceso(acceso);
                    url.getAccesos().add(acceso);
                    ctx.redirect(url.getUrlOriginal());
                }
            }
            }

        });

        app.routes(()->{
            path("/",()->{
                get("/",ctx->{
                    Usuario usuario = ctx.sessionAttribute("usuario_ExamenFinal");
                    TipoUsuario tipoUsuario = ctx.sessionAttribute("tipoUsuario_ExamenFinal");

                    if(usuario!=null){
                        System.out.println("Usuario: "+usuario.getUsuario());
                        Map<String,Object> model = new HashMap<>();
                        model.put("usuario",usuario);
                        model.put("tipoUsuario",tipoUsuario);
                        ctx.render("vistas/bienvenida.html",model);
                    }
                    else {
                        System.out.println("Usuario no logeuado");
                        ctx.render("vistas/bienvenida.html");
                    }
                });
            });
        });
    }
}
