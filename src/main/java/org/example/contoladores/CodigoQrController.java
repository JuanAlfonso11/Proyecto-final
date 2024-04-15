package org.example.contoladores;

import org.example.modelos.CodigoQR;
import org.example.modelos.TipoUsuario;
import org.example.modelos.Url;
import org.example.modelos.Usuario;
import org.example.servicios.CodigoQRServicios;
import org.example.servicios.UrlServicios;
import org.example.servicios.UsuarioServicios;
import io.javalin.Javalin;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.*;
public class CodigoQrController {
    Javalin app;

    public CodigoQrController(Javalin app) {
        this.app = app;
    }

    public void rutas(){
        app.routes(()->{
            path("/codigoQR",()-> {
                get("/", ctx -> {
                    int pageNumber = 1;
                    if (ctx.queryParam("page") != null) {
                        pageNumber = Integer.parseInt(ctx.queryParam("page"));
                    }
                    int pageSize = 3;
                    Usuario user = ctx.sessionAttribute("usuario_ExamenFinal");
                    Map<String, Object> modelo = new HashMap<>();
                    modelo.put("usuario", user);

                    if (user == null) {
                        ArrayList<CodigoQR> codigosQRSession = ctx.sessionAttribute("codigosQRSession");
                        if (codigosQRSession == null) {
                            codigosQRSession = new ArrayList<>();
                        }
                        int totalPages = (int) Math.ceil((double) codigosQRSession.size() / pageSize);
                        ArrayList<CodigoQR> codigosPaginados = CodigoQRServicios.getInstance().getPaginatedCodigos(pageNumber, pageSize, codigosQRSession);

                        System.out.println("Total Pages: " + totalPages);
                        modelo.put("totalPages", totalPages);
                        modelo.put("pageNumber", pageNumber);

                        modelo.put("codigosQR", codigosPaginados);
                        ctx.render("vistas/codigoQR.html", modelo);
                    } else {
                        ArrayList<CodigoQR> codigosQR = CodigoQRServicios.getInstance().getCodigosQRByUser(user);
                        int totalPages = (int) Math.ceil((double) codigosQR.size() / pageSize);
                        ArrayList<CodigoQR> codigosPaginados = CodigoQRServicios.getInstance().getPaginatedCodigos(pageNumber, pageSize, codigosQR);

                        System.out.println("Total Pages: " + totalPages);
                        modelo.put("totalPages", totalPages);
                        modelo.put("pageNumber", pageNumber);

                        modelo.put("codigosQR", codigosPaginados);
                        ctx.render("vistas/codigoQR.html", modelo);//cambiar a thymeleaf
                    }
                });
                post("/add", ctx -> {
                    Usuario user = ctx.sessionAttribute("usuario_ExamenFinal");
                    String tipoQR = ctx.formParam("tipo");

                    if (user == null) {
                        user = UsuarioServicios.getInstance().getInvitado();
                        ArrayList<CodigoQR> codigosQRSession = ctx.sessionAttribute("codigosQRSession");
                        if (codigosQRSession == null) {
                            codigosQRSession = new ArrayList<>();
                        }
                        CodigoQR codigoQR;
                        if (tipoQR.equals("url")) {
                            String url = ctx.formParam("url");
                            codigoQR = new CodigoQR(url, user, ctx.formParam("tipo"));
                            codigosQRSession.add(codigoQR);
                            CodigoQRServicios.getInstance().addCodigoQR(codigoQR);
                            ctx.sessionAttribute("codigosQRSession", codigosQRSession);
                        } else {
                            String url = "WIFI:T:" + ctx.formParam("wifi-tipo") + ";S:" + ctx.formParam("wifi-name") + ";P:" + ctx.formParam("wifi-password") + ";;";
                            codigoQR = new CodigoQR(url, user, ctx.formParam("tipo"));
                            codigosQRSession.add(codigoQR);
                            CodigoQRServicios.getInstance().addCodigoQR(codigoQR);
                            ctx.sessionAttribute("codigosQRSession", codigosQRSession);
                        }
                        ctx.redirect("/codigoQR");//cambiar a thymeleaf
                    }
                    CodigoQR codigoQR;
                    if (tipoQR.equalsIgnoreCase("url")) {
                        String url = ctx.formParam("url");
                        codigoQR = new CodigoQR(url, user, ctx.formParam("tipo"));
                        CodigoQRServicios.getInstance().addCodigoQR(codigoQR);
                    } else {
                        String url = "WIFI:T:" + ctx.formParam("wifi-tipo") + ";S:" + ctx.formParam("wifi-name") + ";P:" + ctx.formParam("wifi-password") + ";;";
                        codigoQR = new CodigoQR(url, user, ctx.formParam("tipo"));
                        CodigoQRServicios.getInstance().addCodigoQR(codigoQR);
                    }
                    ctx.redirect("/codigoQR");//cambiar a thymeleaf
                });
            });
        });
    }
}
