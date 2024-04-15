package org.example.servicios;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import org.example.modelos.CodigoQR;
import org.example.modelos.Usuario;
import org.example.utils.MongoDBConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CodigoQRServicios extends GestionDB<CodigoQR> {
    public static CodigoQRServicios instancia;

    private CodigoQRServicios(){super(CodigoQR.class);}
    public static CodigoQRServicios getInstance(){
        if(instancia==null){
            instancia = new CodigoQRServicios();
        }
        return instancia;
    }

    public ArrayList<CodigoQR> getCodigosQR() {
        Datastore datastore = Morphia.createDatastore(getClient());
        Query<CodigoQR> query = datastore.find(CodigoQR.class);
        return new ArrayList<>(query.iterator().toList());
    }

    public ArrayList<CodigoQR> getCodigosQRByUser(Usuario usuario) {
        Datastore datastore = Morphia.createDatastore(getClient());
        Query<CodigoQR> query = datastore.find(CodigoQR.class).filter("usuario", usuario);
        return new ArrayList<>(query.iterator().toList());
    }

    public void addCodigoQR(CodigoQR codigoQR){
        Datastore datastore = Morphia.createDatastore(getClient());
        datastore.save(codigoQR);
    }

    public ArrayList<CodigoQR> getPaginatedCodigos(int pageNumber, int pageSize, ArrayList<CodigoQR> codigosQR) {
        int offset = (pageNumber - 1) * pageSize;
        return codigosQR.stream()
                .filter(CodigoQR::isActivo)
                .skip(offset)
                .limit(pageSize)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    private MongoClient getClient() {
        return MongoDBConnection.connect();
    }
}