package org.example.API.modelos.REST;

import org.example.API.BaseControlador;
import org.example.API.DTO.UrlDTO;
import org.example.API.modelos.REST.controladoresREST.LoginControllerREST;
import org.example.contoladores.LoginController;
import org.example.modelos.TipoUsuario;
import org.example.modelos.Url;
import org.example.modelos.Usuario;
import org.example.servicios.UrlServicios;
import org.example.servicios.UsuarioServicios;
import org.example.utils.Parseador;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import java.util.ArrayList;

import static io.javalin.apibuilder.ApiBuilder.*;
public class ControladorREST extends BaseControlador {

    public final static String LLAVE_SECRETA = "oeRaYY7Wo24sDqKSX3IM9ASGmdGPmkTd9jo1QTy4b7P9Ze5_9hKolVX8xNrQDcNRfVEdTZNOuOyqEGhXEbdJI-ZQ19k_o9MI0y3eZN2lp9jow55FfXMiINEdt1XR85VipRLSOkT6kSpzs2x-jbLDiz9iFVzkd81YKxMgPA7VfZeQUm4n-mOmnWMaVX30zGFU4L3oPBctYKkl4dYfqYWqRNfrgPJVi5DGFjywgxx0ASEiJHtV72paI3fDR2XwlSkyhhmY-ICjCRmsJN4fX1pdoL8a18-aQrvyu4j0Os6dVPYIoPvvY0SAZtWYKHfM15g7A3HD4cVREf9cUsprCRK93w";
    public final static String ACCEPT_TYPE_JSON = "application/json";
    public final static int UNAUTHORIZED = 401;
    public final static int FORBIDDEN = 403;

    public ControladorREST(Javalin app) {
        super(app);
    }

    public LoginControllerREST loginControllerREST = new LoginControllerREST(app);
    private UrlServicios urlServicios = UrlServicios.getInstance();

    @Override
    public void aplicarRutas() {
        app.routes(() -> {
            path("/rest", () -> {
                after(ctx -> {
                    ctx.header("Content-Type", ACCEPT_TYPE_JSON);
                });
                post("/login", ctx -> {
                    String username = ctx.queryParam("usuario");
                    String password = ctx.queryParam("password");
                    if (!loginControllerREST.Autenticar(username, password)) {
                        ctx.status(UNAUTHORIZED);
                    } else {
                        ctx.status(200);
                        ctx.result(LoginControllerREST.createJWT("ExamenFinal", "ExamenFinal", username, 604800));
                    }
                });
                path("/url", () -> {
                    after(ctx -> {
                        ctx.header("Content-Type", ACCEPT_TYPE_JSON);
                    });
                    get("/list", ctx -> {
                        System.out.println("Entro a list");
                        ArrayList<UrlDTO> urlDTO = new ArrayList<>();
                        Usuario user = getUsuariofromToken(ctx);
                        String urlParam = ctx.queryParam("url");
                        if (user == null) {
                            ctx.status(FORBIDDEN);
                        } else {
                            try {
                                urlServicios.getUrlsUsuario(user.getUsuario()).forEach(url -> {
                                    try {
                                        urlDTO.add(new UrlDTO(url));
                                    } catch (Exception e) {
                                        ctx.status(500);
                                    }
                                });
                                System.out.println("Entro al list URS");
                                ctx.status(200);
                                ctx.json(urlDTO);
                            } catch (Exception e) {
                                ctx.status(500);
                            }
                        }
                    });
                    get("/shorten", ctx -> {
                        System.out.println("Entro a shorten");
                        UrlDTO urlDTO;
                        Usuario user = getUsuariofromToken(ctx);
                        String urlParam = ctx.queryParam("url");
                        if (urlParam == null) {
                            ctx.status(406);
                            return;
                        }
                        if (user == null) {
                            System.out.println("Entro al Shorten null");
                            Usuario invitado = new Usuario("Invitado", "Invitado", "Invitado", "Invitado", TipoUsuario.INVITADO);
                            String hash = urlServicios.getInstance().crearUrl(urlParam, Parseador.getTitulo(urlParam), invitado);
                            Url url = (UrlServicios.getInstance().buscarUrlPorHash(hash));
                            try {
                                System.out.println("Entro al shorten INV");
                                urlDTO = new UrlDTO(url);
                                ctx.status(200);
                                ctx.json(urlDTO);
                            } catch (Exception e) {
                                ctx.status(500);
                            }
                        } else {
                            String hash = urlServicios.getInstance().crearUrl(urlParam, Parseador.getTitulo(urlParam), user);
                            Url url = (UrlServicios.getInstance().buscarUrlPorHash(hash));
                            try {
                                urlDTO = new UrlDTO(url);
                                ctx.status(200);
                                ctx.json(urlDTO);
                            } catch (Exception e) {
                                ctx.status(500);
                            }
                        }

                    });
                });
            });
        });
    }

    public Usuario getUsuariofromToken(Context ctx) {
        String header = "Authorizacion";
        String prefijo = "Bearer";
        String token;
        String headerAuth = ctx.header(header);
        if (headerAuth == null || !headerAuth.startsWith(prefijo)) {
            return null;
        }
        token = headerAuth.substring(prefijo.length()).trim();
        if (token.isEmpty()) {
            ctx.status(FORBIDDEN);
        }
        return loginControllerREST.JwtVerifier(token);


    }
}