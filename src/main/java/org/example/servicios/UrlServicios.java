package org.example.servicios;
import static dev.morphia.query.filters.Filters.eq;

import com.mongodb.client.model.UpdateOptions;
import dev.morphia.mapping.lazy.proxy.ReferenceException;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;
import com.mongodb.client.MongoClients;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import org.example.modelos.Url;
import org.example.modelos.Usuario;
import org.example.utils.GeneradorHash;
import dev.morphia.Datastore;
import org.bson.types.ObjectId;
import org.example.modelos.TipoUsuario;

import java.util.*;
import java.util.regex.Pattern;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UrlServicios extends GestionDB<Url> {

    private static UrlServicios instance;
    private Datastore datastore;

    private UrlServicios() {
        super(Url.class);
        this.datastore = Morphia.createDatastore(cliente);
    }

    public static UrlServicios getInstance() {
        if (instance == null) {
            instance = new UrlServicios();
        }
        return instance;
    }

    public String crearUrl(String urlOriginal, String titulo, Usuario usuario) throws IOException {
        if (urlOriginal == null || urlOriginal.isEmpty()) {
            throw new RuntimeException("La URL no puede estar vacía");
        } else if (urlOriginal.length() > 255) {
            throw new RuntimeException("La URL no puede tener más de 255 caracteres");
        }
        // Save the Usuario object before creating the Url object
        datastore.save(usuario);
        String hash = GeneradorHash.crearHash(urlOriginal);
        while (comprobarExistenciaHash(hash)) {
            hash = GeneradorHash.crearHash(urlOriginal + System.currentTimeMillis());
        }
        Url url;
        if (urlOriginal.contains("http://") || urlOriginal.contains("https://")) {
            url = new Url(urlOriginal, titulo, new Date(), hash, usuario);
        } else {
            url = new Url("http://" + urlOriginal, titulo, new Date(), hash, usuario);
        }
        crear(url); // Utiliza el método crear de GestionDB
        return hash;
    }
    private boolean comprobarExistenciaHash(String hash) {
        long count = datastore.find(Url.class).filter(Filters.eq("hash", hash)).count();
        return count > 0;
    }

    public Url buscarUrlPorUUID(UUID uuid) {
        return datastore.find(Url.class).filter(Filters.eq("_id", new ObjectId(uuid.toString()))).first();
    }

    public void habilitarUrl(UUID uuid, Boolean habilitar) {
        datastore.find(Url.class)
                .filter(Filters.eq("_id", new ObjectId(uuid.toString())))
                .update(UpdateOperators.set("activo", habilitar))
                .execute();
    }

    public Url buscarUrlPorHash(String hash) {
        return datastore.find(Url.class).filter(Filters.eq("hash", hash)).first();
    }

    // Ejemplo adaptado para buscar URLs por título

    public ArrayList<Url> topUrlsList(Usuario miUsuario) {
        List<Url> urls = new ArrayList<>();
        datastore.find(Url.class).iterator().toList().forEach(url -> {
            try {
                if (url.getUsuario() != null) {
                    // Perform a check to see if the Usuario exists without fetching the entire object
                    boolean userExists = datastore.find(Usuario.class)
                            .filter("_id", url.getUsuario().getId())
                            .count() > 0;
                    if (userExists) {
                        urls.add(url);
                    }
                }
            } catch (ReferenceException e) {
                System.err.println("Referenced 'Usuario' entity could not be found during a fetch for URL: " + url.getUrlOriginal());
            }
        });

        if (miUsuario.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR)) {
            return urls.stream()
                    .sorted((url1, url2) -> Integer.compare(url2.getAccesos().size(), url1.getAccesos().size()))
                    .limit(5)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            return urls.stream()
                    .filter(url -> url.getUsuario().equals(miUsuario))
                    .sorted((url1, url2) -> Integer.compare(url2.getAccesos().size(), url1.getAccesos().size()))
                    .limit(5)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }


    public HashMap<String, Integer> topUrls() {
        List<Url> urls = datastore.find(Url.class)
                .iterator().toList()
                .stream()
                .sorted((url1, url2) -> Integer.compare(url2.getAccesos().size(), url1.getAccesos().size()))
                .limit(5)
                .collect(Collectors.toList());
        HashMap<String, Integer> topUrls = new HashMap<>();
        urls.forEach(url -> topUrls.put(url.getUrlOriginal(), url.getAccesos().size()));
        return topUrls;
    }



    public ArrayList<Url> buscarUrlPorTitulo(String titulo) {
        return datastore.find(Url.class)
                .filter(Filters.eq("titulo", Pattern.compile(titulo)))
                .iterator().toList()
                .stream()
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public ArrayList<Url> getUrls() {
        return datastore.find(Url.class)
                .iterator().toList()
                .stream()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Url> getActiveUrls() {
        return datastore.find(Url.class)
                .filter(Filters.eq("activo", true))
                .iterator().toList()
                .stream()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public long getCountUrls() {
        return datastore.find(Url.class)
                .filter(Filters.eq("activo", true))
                .count();
    }

    public long getCountUrlsActive(Usuario usuario) {
        if (!usuario.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR)) {
            return datastore.find(Url.class)
                    .filter(Filters.eq("usuario", usuario))
                    .count();
        } else {
            return datastore.find(Url.class).count();
        }
    }

    public ArrayList<Url> getUrlsUsuario(String usuario) {
        return datastore.find(Url.class)
                .filter(Filters.eq("usuario", usuario))
                .iterator().toList()
                .stream()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Url> getPaginatedProductos(int pageNumber, int pageSize, List<Url> urls) {
        return urls.stream()
                .filter(Url::isActivo)
                .skip((long) (pageNumber - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public void saveEntityWithReference() {
        // Create and save the referenced entity
        Usuario usuario = new Usuario();
        datastore.save(usuario);

        // Create the entity that references the referenced entity
        Url url = new Url();
        url.setUsuario(usuario);

        // Save the entity that references the referenced entity
        datastore.save(url);
    }
}