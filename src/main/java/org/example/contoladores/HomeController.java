package org.example.contoladores;

import org.example.modelos.Url;
import org.example.modelos.Usuario;
import org.example.servicios.AccesoServicios;
import org.example.servicios.UrlServicios;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.javalin.apibuilder.ApiBuilder.*;
public class HomeController {
    Javalin app;

    public HomeController(Javalin app) {
        this.app = app;
    }

    public void rutas(){
        app.routes(()->{
            path("/home",()->{
                get("/",ctx->{
                    Map<String, Object> modelo = new HashMap<>();
                    Usuario user = getUsuarioCTX(ctx);
                    if(user==null){
                        ctx.redirect("/urls");
                        return;
                    }
                    else{
                        ArrayList<Url> topURLS = UrlServicios.getInstance().topUrlsList(user);
                        ArrayList<Integer> topURLAccesos = new ArrayList<>();
                        for(Url topURL:topURLS){
                            topURLAccesos.add(topURL.getAccesos().size());
                        }
                        HashMap<String,Long> Paises = AccesoServicios.getInstance().getCountriesTotalAccess(user);
                        modelo.put("Paises",Paises);
                        long totalAccesos = AccesoServicios.getInstance().countTotalAccesses(user);
                        modelo.put("cantUrls",UrlServicios.getInstance().getCountUrlsActive(user));
                        modelo.put("cantAccesos",totalAccesos);
                        modelo.put("topURL",topURLS);
                        modelo.put("cantTopURL",topURLAccesos);
                        modelo.put("usuario",user);
                    }
                    ctx.render("vistas/home.html",modelo);//cambiar a thyemeleaf
                });
            });
        });
    }
    public Usuario getUsuarioCTX(Context ctx){return ctx.sessionAttribute("usuario_ExamenFinal");}
}
