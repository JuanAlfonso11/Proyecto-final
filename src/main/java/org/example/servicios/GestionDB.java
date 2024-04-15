package org.example.servicios;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.MorphiaCursor;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;
import org.bson.types.ObjectId;
import dev.morphia.query.Query;
import org.example.utils.DatosEstaticos;
import org.example.utils.MongoDBConnection;

import java.util.List;
import java.util.ArrayList;

import org.example.modelos.CodigoQR;
import org.example.modelos.Usuario;
import org.example.modelos.temp;

@SuppressWarnings("removal")
public class GestionDB<T> {
    private Class<T> claseEntidad;
    MongoClient cliente = MongoDBConnection.connect();

    public GestionDB(Class<T> claseEntidad) {
        this.claseEntidad = claseEntidad;
    }

//    protected Datastore getConexionMorphia() {
//        return Morphia.createDatastore(cliente, MongoDbConexion.getNombreBaseDatos());
//    }

    public void crear(T entidad) {
        Datastore datastore = Morphia.createDatastore(cliente);
        try {
            datastore.save(entidad);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Query<T> find() {
        Datastore datastore = Morphia.createDatastore(cliente);
        Query<T> query = datastore.find(claseEntidad);
        return query;
    }

    public T findByID(String id) {
        Datastore datastore = Morphia.createDatastore(cliente);
        Query<T> query = datastore.find(claseEntidad).filter("_id", new ObjectId(id));
        return query.first();
    }

    public T Validacion(String usuario, String password) {
        Datastore datastore = Morphia.createDatastore(cliente);
        Query<T> query = datastore.find(claseEntidad).filter("usuario", usuario).filter("password", password);
        return query.first();
    }

    public List<CodigoQR> CodigoQRByUsuario(Object Id) {
        Datastore datastore = Morphia.createDatastore(cliente);
        Query<CodigoQR> query = datastore.find(CodigoQR.class).filter("usuario", Id);
        List<CodigoQR> nueva = new ArrayList<>();
        for (CodigoQR t : query) {
            Usuario u = t.getUsuario();
            if (u.getId().equals(Id)) {
                nueva.add(t);
            }
        }
        return nueva;
    }

    public void updateByID(String id, temp newdata) {
        Datastore datastore = Morphia.createDatastore(cliente);
        datastore.find(temp.class)
                .filter("_id", new ObjectId(id))
                .update(UpdateOperators.set("nombre", newdata.getNombre()),
                        UpdateOperators.set("correo", newdata.getCorreo()))
                .execute();
    }

    public void deleteByID(String id) {
        Datastore datastore = Morphia.createDatastore(cliente);
        datastore.find(claseEntidad).filter("_id", new ObjectId(id)).delete();
    }

//    private static class MongoDbConexion{
//        private static MongoClient mongoClient;
//        private static String nombreBaseDatos;
//
//        public static MongoClient getMongoClient(){
//            if(mongoClient == null){
//                ProcessBuilder processBuilder = new ProcessBuilder();
//                String URL_MONGODB = processBuilder.environment().get(DatosEstaticos.URL_MONGO.getValor());
//                nombreBaseDatos = processBuilder.environment().get(DatosEstaticos.DB_NOMBRE.getValor());
//                mongoClient = MongoClients.create(URL_MONGODB);
//            }
//            return mongoClient;
//        }
//
//        public static String getNombreBaseDatos(){
//            return nombreBaseDatos;
//        }
//    }
//}
}