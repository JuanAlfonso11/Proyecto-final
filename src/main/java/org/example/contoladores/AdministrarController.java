package org.example.contoladores;
import org.example.modelos.TipoUsuario;
import org.example.modelos.Usuario;
import org.example.modelos.Url;
import org.example.servicios.UsuarioServicios;
import org.example.servicios.UrlServicios;
import org.example.servicios.AccesoServicios;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.javalin.apibuilder.ApiBuilder.*;

public class AdministrarController {
    Javalin app;

    public AdministrarController(Javalin app) {
        this.app = app;
    }

    public void rutas(){
        app.routes(()->{
            app.before("/administrar/*",ctx->{
                Usuario usuario = getUsuarioCTX(ctx);
                if(usuario == null || !usuario.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR)){
                    ctx.redirect("/login");
                }
            });
            path("/administrar",()->{
                get("/habilitar/url/{bool}/{urlUUID}",ctx->{
                    Boolean habilitar = ctx.pathParam("bool").equals("true");
                    UrlServicios.getInstance().habilitarUrl(UUID.fromString(ctx.pathParam("urlUUID")),habilitar);
                    ctx.redirect("/administrar/urls");
                });

                get("/habilitar/usuario/{bool}/{usuario}",ctx->{
                    Boolean habilitar = ctx.pathParam("bool").equals("true");
                    String usuario = ctx.pathParam("usuario");
                    TipoUsuario tipoUsuario = UsuarioServicios.getInstance().habilitarUsuario(usuario,habilitar);
                    if(tipoUsuario.equals(TipoUsuario.ADMINISTRADOR)){
                        ctx.redirect("/administrar/Administradores");
                    }
                    else if(tipoUsuario.equals(TipoUsuario.CLIENTE)){
                        ctx.redirect("/administrar/Clientes");
                    }
                    else{
                        ctx.redirect("/administrar");
                    }
                });
                post("/{tipo}/",ctx->{
                    String tipo = ctx.pathParam("tipo");
                    String valor = ctx.pathParam("busqueda");

                    if(valor ==null|| valor.isEmpty()){
                        ctx.redirect("/"+tipo);
                        return;
                    }
                    if(tipo.equals("Administradores")){
                        Map<String,Object> modelo = new HashMap<>();
                        modelo.put("tipoAdministracion","Administradores");
                        modelo.put("usuarios",UsuarioServicios.getInstance().getUsuariosByTipoAndUsuario(TipoUsuario.ADMINISTRADOR,valor));
                        modelo.put("usuario",ctx.sessionAttribute("usuario_ExamenFinal"));
                        ctx.render("vistas/administrar.html",modelo);//cambiar a thymeleaf
                    }
                    else if(tipo.equals("Clientes")){
                        Map<String,Object> modelo =new HashMap<>();
                        modelo.put("tipoAdministracion",tipo);
                        modelo.put("usuarios",UsuarioServicios.getInstance().getUsuariosByTipoAndUsuario(TipoUsuario.CLIENTE,valor));
                        modelo.put("usuario",ctx.sessionAttribute("usuario_ExamenFinal"));
                        ctx.render("vistas/administrar-usuarios.html",modelo);//cambiar a thymeleaf
                    }
                    else if(tipo.equals("urls")){
                        Map<String, Object> modelo= new HashMap<>();
                        modelo.put("urls", UrlServicios.getInstance().buscarUrlPorTitulo(valor));
                        modelo.put("usuario",ctx.sessionAttribute("usuario_ExamenFinal"));
                        ctx.render("vistas/administrar-urls.html",modelo);//cambiar a thymeleaf
                    }
                    else {
                        ctx.redirect("/administrar");
                    }
                });
                get("/",ctx->{
                    Usuario usuario = getUsuarioCTX(ctx);
                    if(usuario == null|| !usuario.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR)){
                        ctx.redirect("/login");
                    }else{
                        Map<String, Object> modelo = new HashMap<>();
                        long totalAccesos = AccesoServicios.getInstance().countTotalAccesses(usuario);
                        modelo.put("cantUsuarios", UsuarioServicios.getInstance().getUsuarios().size());
                        modelo.put("cantUrls", UrlServicios.getInstance().getCountUrls());
                        modelo.put("cantAccesos",totalAccesos);
                        modelo.put("usuario",ctx.sessionAttribute("usuario_ExamenFinal"));
                        ctx.render("vistas/administrar.html",modelo);
                    }
                });
                get("/Administrar",ctx->{
                    Map<String, Object> modelo = new HashMap<>();
                    modelo.put("tipoAdministracion","Administradores");
                    modelo.put("usuarios",UsuarioServicios.getInstance().getUsuariosByTipo(TipoUsuario.ADMINISTRADOR));
                    modelo.put("usuario",ctx.sessionAttribute("usuario_ExamenFinal"));
                    ctx.render("vistas/administrar-usuarios.html",modelo);
                });
                get("/Clientes",ctx->{
                    Map<String, Object> modelo = new HashMap<>();
                    modelo.put("tipoAdministracion","Clientes");
                    modelo.put("usuarios",UsuarioServicios.getInstance().getUsuariosByTipo(TipoUsuario.CLIENTE));
                    modelo.put("usuario",ctx.sessionAttribute("usuario_ExamenFinal"));
                    ctx.render("vistas/administrar-usuarios.html",modelo);
                });
                get("/urls",ctx->{
                    Map<String, Object> modelo = new HashMap<>();
                    modelo.put("urls",UrlServicios.getInstance().getUrls());
                    modelo.put("usuario",ctx.sessionAttribute("usuario_ExamenFinal"));
                    ctx.render("vistas/administrar-urls.html",modelo);
                });
            });
        });
    }
    public Usuario getUsuarioCTX(Context ctx){return ctx.sessionAttribute("usuario_ExamenFinal");}
}
