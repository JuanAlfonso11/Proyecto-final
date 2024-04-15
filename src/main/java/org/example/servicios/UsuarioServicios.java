package org.example.servicios;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.updates.UpdateOperators;
import org.example.modelos.TipoUsuario;
import org.example.modelos.Usuario;
import org.example.utils.MongoDBConnection;
import org.bson.types.ObjectId;
import org.jasypt.util.text.StrongTextEncryptor;
import java.util.List;

public class UsuarioServicios extends GestionDB<Usuario> {
    private static UsuarioServicios instancia;
    private Datastore datastore;

    private UsuarioServicios() {
        super(Usuario.class);
        MongoClient cliente = MongoDBConnection.connect();
        datastore = Morphia.createDatastore(cliente);
    }

    public static UsuarioServicios getInstance() {
        if (instancia == null) {
            instancia = new UsuarioServicios();
        }
        return instancia;
    }

    public Usuario addUsuario(Usuario usuario) {
        // Check if user already exists based on a unique attribute like username or email
        if (usuarioExists(usuario.getUsuario())) {
            System.out.println("User already exists: " + usuario.getUsuario());
            return null; // Optionally return the existing user or handle this case as needed
        }

        // Encrypt the password
        String plainPassword = usuario.getPassword();
        StrongTextEncryptor encriptador = new StrongTextEncryptor();
        encriptador.setPassword("ExamenFinal");  // Ensure this key is securely managed and consistent
        String encryptedPassword = encriptador.encrypt(plainPassword);
        usuario.setPassword(encryptedPassword);

        // Save the new user to the database
        datastore.save(usuario);
        return usuario;
    }

    public Usuario getUsuarioByUsuario(String usuario) {
        return datastore.find(Usuario.class)
                .filter("usuario", usuario)
                .first();
    }
    public List<Usuario> getUsuarios() {
        return datastore.find(Usuario.class).iterator().toList();
    }

    public List<Usuario> getUsuariosByTipo(TipoUsuario tipo) {
        return datastore.find(Usuario.class)
                .filter("tipoUsuario", tipo)
                .iterator().toList();
    }

    public List<Usuario> getUsuariosByTipoAndUsuario(TipoUsuario tipo, String usuario) {
        return datastore.find(Usuario.class)
                .filter("tipoUsuario", tipo.toString()) // Convert TipoUsuario to String before comparing
                .filter("usuario", usuario)
                .iterator().toList();
    }

    public void modificarUsuario(Usuario usuario) {
        ObjectId id = usuario.getId();  // No need to create a new ObjectId
        datastore.find(Usuario.class)
                .filter("_id", id)
                .update(UpdateOperators.set("usuario", usuario))
                .execute();
    }

    public void eliminarUsuario(String usuario) {
        if (usuario.equals("admin")) {
            return;
        }
        datastore.find(Usuario.class)
                .filter("usuario", usuario)
                .delete();
    }

    public Usuario getInvitado() {
        return new Usuario("Invitado", "Invitado", "Invitado", "Invitado", TipoUsuario.INVITADO);
    }

    public boolean existeUsuario(String usuario) {
        Usuario user = datastore.find(Usuario.class)
                .filter("usuario", usuario)
                .first();
        return user != null;
    }

    public TipoUsuario habilitarUsuario(String usuario, Boolean habilitado) {
        Usuario user = datastore.find(Usuario.class)
                .filter("usuario", usuario)
                .first();
        if (user != null) {
            user.setHabilitado(habilitado);
            datastore.save(user);
        }
        return user != null ? user.getTipoUsuario() : null;
    }
    private boolean usuarioExists(String usuario) {
        return datastore.find(Usuario.class)
                .filter("usuario", usuario)
                .count() > 0;
    }
}
