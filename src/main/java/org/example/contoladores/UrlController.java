package org.example.contoladores;

import org.example.modelos.Acceso;
import org.example.modelos.TipoUsuario;
import org.example.modelos.Url;
import org.example.modelos.Usuario;
import org.example.servicios.AccesoServicios;
import org.example.servicios.UrlServicios;
import org.example.servicios.UsuarioServicios;
import org.example.utils.Parseador;
import io.javalin.Javalin;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.javalin.http.Context;


import static io.javalin.apibuilder.ApiBuilder.*;

public class UrlController {
    Javalin app;

    public UrlController(Javalin app) {
        this.app = app;
    }
    public void rutas(){
        app.routes(()->{
            path("/urls",()->{
                get("/",ctx->{
                    Usuario user = getUsuarioCTX(ctx);
                    int pageNumber = 1;
                    if(ctx.queryParam("page")!=null){
                        pageNumber =Integer.parseInt(ctx.queryParam("page"));
                    }
                    int pageSize = 3;
                    if(user==null) {
                        Map<String, Object> modelo = new HashMap<>();
                        ArrayList<Url> urlSession = ctx.sessionAttribute("urlSession");
                        if (urlSession == null) {
                            urlSession = new ArrayList<>();
                        }
                        int totalPages = (int) Math.ceil((double) urlSession.size() / pageSize);
                        System.out.println("Total Pages: " + totalPages);
                        modelo.put("totalPages", totalPages);
                        modelo.put("pageNumber", pageNumber);
                        ArrayList<Url> urls = UrlServicios.getInstance().getPaginatedProductos(pageNumber, pageSize, urlSession);
                        modelo.put("urls", urls);
                        ctx.render("vistas/url.html", modelo);

                    }else if(user.getTipoUsuario().equals(TipoUsuario.CLIENTE)){
                        Map<String, Object> modelo = new HashMap<>();
                        ArrayList<Url> misUrls = UrlServicios.getInstance().getUrlsUsuario(user.getUsuario());
                        ArrayList<Url> urls = UrlServicios.getInstance().getPaginatedProductos(pageNumber, pageSize, misUrls);
                        int totalPages = (int) Math.ceil((double) misUrls.size() / pageSize);
                        modelo.put("totalPages", totalPages);
                        modelo.put("pageNumber", pageNumber);
                        modelo.put("urls", urls);
                        modelo.put("usuario", user);
                        ctx.render("vistas/url.html", modelo);//cambiar a thymeleaf
                    }
                    else{
                        Map<String,Object> modelo = new HashMap<>();
                        ArrayList<Url> misUrls= UrlServicios.getInstance().getUrlsUsuario(user.getUsuario());
                        ArrayList<Url> urls = UrlServicios.getInstance().getPaginatedProductos(pageNumber,pageSize,misUrls);
                        int totalPages = (int) Math.ceil((double) misUrls.size() / pageSize);
                        modelo.put("totalPages",totalPages);
                        modelo.put("pageNumber",pageNumber);
                        modelo.put("urls",urls);
                        modelo.put("usuario",user);
                        ctx.render("vistas/url.html",modelo);//cambiar a thymeleaf
                    }
                });
                post("/add", ctx -> {
                    boolean invitado = false;
                    String urlParam = ctx.formParam("myURL");
                    String titulo = ctx.formParam("titulo");
                    if (urlParam == null || urlParam.isEmpty()) {
                        ctx.redirect("/");
                        return;
                    }
                    Usuario user = getUsuarioCTX(ctx);
                    if (user == null) {
                        user = UsuarioServicios.getInstance().getInvitado();
                        invitado = true;
                    } else if (user.getTipoUsuario().equals(TipoUsuario.INVITADO)) {
                        invitado = true;
                    }
                    // Save the user entity before creating the Url entity
                    UsuarioServicios.getInstance().addUsuario(user);
                    String hash;
                    if (titulo == null || titulo.isEmpty()) {
                        hash = UrlServicios.getInstance().crearUrl(urlParam, Parseador.getTitulo(urlParam), user);
                    } else {
                        hash = UrlServicios.getInstance().crearUrl(urlParam, ctx.formParam("titulo"), user);
                    }
                    Url url = UrlServicios.getInstance().buscarUrlPorHash(hash);
                    if (url == null) {
                        ctx.redirect("/cutlink/urls/");
                        return;
                    }
                    if (invitado) {
                        ArrayList<Url> urlSession = ctx.sessionAttribute("urlSession");
                        if (urlSession == null) {
                            urlSession = new ArrayList<>();
                        }
                        urlSession.add(url);
                        ctx.sessionAttribute("urlSession", urlSession);
                    }
                    ctx.redirect("/urls/resumen/"+hash);
                });

                get("/resumen/{hash}",ctx->{
                    Usuario user = getUsuarioCTX(ctx);
                    Map<String,Object> modelo = new HashMap<>();
                    String id = ctx.pathParam("hash");
                    Url url = UrlServicios.getInstance().buscarUrlPorHash(id);
                    if(url==null){
                        ctx.redirect("/urls");
                        return;
                    }
                    if(user==null){
                        ArrayList<Url> urlSession = ctx.sessionAttribute("urlSession");
                        if(urlSession==null){
                            ctx.redirect("/urls");
                            return;
                        }
                        if(urlSession.stream().noneMatch(u->u.getHash().equals(url.getHash()))){
                            ctx.redirect("/urls");
                            return;
                        }
                    }
                    else{
                        if(user.getTipoUsuario().equals(TipoUsuario.CLIENTE)){
                            if(!url.getUsuario().getUsuario().equals(user.getUsuario())){
                                ctx.redirect("/urls");
                                return;
                            }
                        }
                    }
                    modelo.put("usuario",user);
                    modelo.put("url",url);
                    int tamanoAcceso = url.getAccesos().size();
                    modelo.put("cantAccesos",tamanoAcceso);
                    if(tamanoAcceso>0){
                        modelo.put("ultimoAcceso",url.getAccesos().get(tamanoAcceso-1).getFechaString());
                        modelo.put("accesos",AccesoServicios.getInstance().getAccesosFromHash(id));
                        modelo.put("paisMasVisitado",AccesoServicios.getInstance().getTopCountryWithMostAccesses(id));
                    }
                    List<Acceso> accesos = url.getAccesos();
                    Map<String,Long> plataformasData =accesos.stream()
                            .collect(Collectors.groupingBy(Acceso::getPlataforma,Collectors.counting()));
                    modelo.put("plataformasLabels",plataformasData.keySet().toArray());
                    modelo.put("plataformasData",plataformasData.values().toArray());

                    Map<String,Long> navegadoresData = accesos.stream()
                            .collect(Collectors.groupingBy(Acceso::getNavegador,Collectors.counting()));
                    modelo.put("navegadoresLabels",navegadoresData.keySet().toArray());
                    modelo.put("navegadoresData",navegadoresData.values().toArray());

                    Map<Integer, Long> accesosPorHora = new HashMap<>();
                    for(Acceso acceso:url.getAccesos()){
                        Calendar calendar=Calendar.getInstance();
                        calendar.setTime(acceso.getLocalDateTime());
                        int hora= calendar.get(Calendar.HOUR_OF_DAY);
                        accesosPorHora.put(hora,accesosPorHora.getOrDefault(hora,0L)+1);
                    }
                    Map<Integer,Long> horasDelDia = IntStream.rangeClosed(0,23)
                            .boxed()
                            .collect(Collectors.toMap(h->h,h->accesosPorHora.getOrDefault(h,0L)));

                    Map<Integer, Long> horasConAccesos = new HashMap<>(horasDelDia);
                    horasConAccesos.putAll(accesosPorHora);

                    modelo.put("horasLabels",horasDelDia.keySet().toArray());
                    modelo.put("horasData",horasDelDia.values().toArray());

                    ctx.render("vistas/urlResumen.html",modelo);//cambiar a thymeleaf
                });
            });
        });
    }
    public Usuario getUsuarioCTX(Context ctx){
        return ctx.sessionAttribute("usuario_ExamenFinal");
    }
}
