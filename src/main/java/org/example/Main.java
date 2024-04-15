package org.example;
import org.example.API.modelos.REST.ControladorREST;
import org.example.API.modelos.SOAP.ControladorSOAP;
import org.example.GRPC.GrpcServer;
import org.example.contoladores.*;
import org.example.modelos.TipoUsuario;
import org.example.modelos.Usuario;
import org.example.servicios.UrlServicios;
import org.example.servicios.UsuarioServicios;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
public class Main {
    public static void main(String[] args) throws Exception {
        String direccion = Direccionamiento.getInstance().getRedServidor();
        int puerto = Direccionamiento.getInstance().getPuertoServidor();

        var app = Javalin.create(javalinConfig -> {
            javalinConfig.staticFiles.add(staticFileConfig -> {
                staticFileConfig.hostedPath = "/";
                staticFileConfig.directory = "/publico";
                staticFileConfig.location = Location.CLASSPATH;
            });
        });

        ControladorSOAP controladorSOAP = new ControladorSOAP(app);
        controladorSOAP.aplicarRutas();

        app.start(direccion, puerto);

        new UrlController(app).rutas();
        new AccesoController(app).rutas();
        new HomeController(app).rutas();
        new CodigoQrController(app).rutas();
        new AdministrarController(app).rutas();
        new LoginController(app).rutas();
        new ControladorREST(app).aplicarRutas();

        try {
            long startTime = System.currentTimeMillis();
            CountryController.getInstance().getCountry(direccion);
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println("Tiempo de respuesta: " + totalTime + "ms");
        } catch (Exception e) {
            throw new Exception("Error al obtener el país");
        }

        Usuario admin = new Usuario("admin", "admin", "admin@admin.com", "admin", TipoUsuario.ADMINISTRADOR);
        Usuario invitado = new Usuario("invitado", "invitado", "invitado", "invitado", TipoUsuario.INVITADO);
        Usuario clientePueba= new Usuario("cliente", "cliente", "cliente@prueba.com", "cliente", TipoUsuario.CLIENTE);

        UsuarioServicios.getInstance().addUsuario(admin);
        UsuarioServicios.getInstance().addUsuario(invitado);
        UsuarioServicios.getInstance().addUsuario(clientePueba);

        UrlServicios.getInstance().crearUrl("https://www.google.com", "Google", admin);
        UrlServicios.getInstance().crearUrl("https://www.youtube.com", "Youtube", admin);
        UrlServicios.getInstance().crearUrl("https://www.twitch.com", "Twitch", invitado);
        UrlServicios.getInstance().crearUrl("https://www.netflix.com", "Netflix", clientePueba);
        UrlServicios.getInstance().crearUrl("https://www.spotify.com", "Spotify", clientePueba);
        UrlServicios.getInstance().crearUrl("https://www.amazon.com", "Amazon", invitado);

        int port = 40055;
        GrpcServer grpcServer = new GrpcServer(port);
        grpcServer.start();


    }
}