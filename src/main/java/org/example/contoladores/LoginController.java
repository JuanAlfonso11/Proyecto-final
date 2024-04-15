package org.example.contoladores;

import org.example.modelos.TipoUsuario;
import org.example.modelos.Usuario;
import org.example.servicios.UsuarioServicios;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jasypt.util.text.StrongTextEncryptor;

import java.util.HashMap;
import java.util.Map;


import static io.javalin.apibuilder.ApiBuilder.*;
public class LoginController {
    Javalin app;
    private StrongTextEncryptor encriptador;

    public LoginController(Javalin app) {
        this.app = app;
    }

    public void rutas() {
        app.routes(() -> {
            get("/logout", ctx -> {
                invalidarSesion(ctx);
                ctx.redirect("/");
            });
            path("/login", () -> {
                get("/", ctx -> {
                    if (ctx.sessionAttribute("usuarioID_ExamenFinal") != null) {
                        ctx.redirect("/");
                    }
                    ctx.render("vistas/login.html");
                });
                post("/", ctx -> {
                    String usuario = ctx.formParam("usuario");
                    String password = ctx.formParam("password");
                    if (usuario == "Invitado") {
                        Map<String, Object> modelo = new HashMap<>();
                        modelo.put("menjase", "Usuario o password incorrectos");
                        ctx.render("/vistas/login.html", modelo);
                        ctx.redirect("/");
                        return;
                    }
                    if (Autenticar(usuario, password) != null && Autenticar(usuario, password)) {
                        Usuario user = UsuarioServicios.getInstance().getUsuarioByUsuario(usuario);
                        if (!user.isHabilitado()) {
                            Map<String, Object> modelo = new HashMap<>();
                            modelo.put("mensaje", "Cuenta Bloqueada");
                            ctx.render("/vistas/login.html", modelo);
                        }
                        ctx.sessionAttribute("usuarioID_ExamenFinal", user.getUsuario());
                        ctx.sessionAttribute("usuario_ExamenFinal", user);
                        ctx.sessionAttribute("tipoUsuario_ExamenFinal", user.getTipoUsuario());
                        ctx.sessionAttribute("SExamenFinal", encriptador.encrypt(usuario) + "-" + encriptador.encrypt(password));
                        if (ctx.formParam("recordar") != null) {
                            ctx.cookie("ExamenFinal", encriptador.encrypt(usuario) + "-" + encriptador.encrypt(password));
                        } else {
                            ctx.cookie("ExamenFinal", "null");
                        }
                        ctx.redirect("/urls");
                    } else {
                        System.out.println("Usuario o password incorrectos");
                        Map<String, Object> modelo = new HashMap<>();
                        modelo.put("mensaje", "Usuario o password incorrectos");
                        ctx.render("/vistas/login.html", modelo);
                    }
                });
            });
            path("/register", () -> {
                get("/", ctx -> {
                    ctx.render("vistas/register.html");
                });
                post("/", ctx -> {
                    String usuario = ctx.formParam("usuario");
                    String nombre = ctx.formParam("nombre");
                    String password = ctx.formParam("password");
                    String email = ctx.formParam("email");
                    if (UsuarioServicios.getInstance().existeUsuario(usuario)) {
                        Map<String, Object> modelo = new HashMap<>();
                        modelo.put("mensaje", "Usuario ya existe");
                        ctx.render("/vistas/register.html", modelo);
                        return;
                    }
                    Usuario nuevoUsuario = new Usuario(usuario, nombre, email, password, TipoUsuario.CLIENTE);
                    UsuarioServicios.getInstance().addUsuario(nuevoUsuario);
                    ctx.redirect("/login");
                });
            });
        });
    }
    public Boolean Autenticar(String usuarioID, String password){
        Usuario user = UsuarioServicios.getInstance().getUsuarioByUsuario(usuarioID);
        if(user != null){
            encriptador = new StrongTextEncryptor();
            encriptador.setPassword("ExamenFinal");
            return user.isHabilitado() && encriptador.decrypt(user.getPassword()).equals(password);
        }
        else{
            return false;
        }
    }
    public void invalidarSesion(Context ctx){
        ctx.sessionAttribute("usuarioID_ExamenFinal", null);
        ctx.sessionAttribute("usuario_ExamenFinal", null);
        ctx.sessionAttribute("tipoUsuario_ExamenFinal", null);
        ctx.sessionAttribute("SExamenFinal", null);
        ctx.cookie("ExamenFinal", "null");
    }

    public void invalidarCookie(Context ctx){
        ctx.cookie("ExamenFinal", "null");
    }
}
