package org.example.servicios;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Accumulators;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
//import dev.morphia.aggregation.expressions.impls.Accumulator;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import org.bson.Document;
import org.example.modelos.Acceso;
import org.example.modelos.TipoUsuario;
import org.example.modelos.Usuario;
import org.example.utils.MongoDBConnection;
import dev.morphia.aggregation.AggregationPipeline;
import dev.morphia.aggregation.expressions.Expressions;
import dev.morphia.aggregation.Accumulator;
import dev.morphia.aggregation.Accumulator;
import dev.morphia.aggregation.Aggregation;
import dev.morphia.query.Sort;
import dev.morphia.aggregation.Group;
import dev.morphia.aggregation.AggregationPipeline;
import dev.morphia.aggregation.Group;
import dev.morphia.aggregation.Aggregation;
import dev.morphia.aggregation.expressions.Expressions;
import dev.morphia.aggregation.stages.Projection;
import dev.morphia.query.filters.Filters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AccesoServicios extends GestionDB<Acceso> {

    private static AccesoServicios instance;
    private Datastore datastore;

    private AccesoServicios() {
        super(Acceso.class);
        MongoClient cliente = MongoDBConnection.connect();
        this.datastore = Morphia.createDatastore(cliente);
    }

    public static AccesoServicios getInstance() {
        if (instance == null) {
            instance = new AccesoServicios();
        }
        return instance;
    }

    public void crearAcceso(Acceso acceso) {
        datastore.save(acceso);
    }

    public List<Acceso> getAccesosFromHash(String hash) {
        Query<Acceso> query = datastore.find(Acceso.class)
                .filter(Filters.eq("url.hash", hash));
        return query.iterator().toList();
    }

    public long countAccesosFromHash(String hash) {
        return datastore.find(Acceso.class)
                .filter(Filters.eq("url.hash", hash))
                .count();
    }

    public HashMap<String, Long> getCountriesTotalAccess(Usuario usuario) {
        Query<Acceso> query;
        if (usuario.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR)) {
            query = datastore.find(Acceso.class);
        } else {
            query = datastore.find(Acceso.class)
                    .filter(Filters.eq("url.usuario", usuario));
        }

        List<Acceso> accesos = query.iterator().toList();
        HashMap<String, Long> countriesMap = new HashMap<>();
        for (Acceso acceso : accesos) {
            countriesMap.merge(acceso.getPais(), 1L, Long::sum);
        }
        return countriesMap;
    }

    public long countTotalAccesses(Usuario usuario) {
        Query<Acceso> query;
        if (usuario.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR)) {
            query = datastore.find(Acceso.class);
        } else {
            query = datastore.find(Acceso.class)
                    .filter(Filters.eq("url.usuario", usuario));
        }
        return query.count();
    }


    public String getTopCountryWithMostAccesses(String hash) {
        Query<Acceso> query = datastore.find(Acceso.class).filter(Filters.eq("url.hash", hash));
        AggregationPipeline pipeline = datastore.createAggregation(Acceso.class)
                .match(query)
                .group("$pais", Group.grouping("count", new Accumulator("$sum", "constant")))
                .sort(Sort.descending("count"))
                .limit(1);

        Iterator<Document> iterator = pipeline.aggregate(Document.class);
        if (iterator.hasNext()) {
            Document result = iterator.next();
            return result.getString("_id");
        }
        return null;
    }

    public String getTopBrowserWithMostAccesses(String hash) {
        Query<Acceso> query = datastore.find(Acceso.class).filter(Filters.eq("url.hash", hash));
        AggregationPipeline pipeline = datastore.createAggregation(Acceso.class)
                .match(query)
                .group("$navegador", Group.grouping("count", new Accumulator("$sum", "constant")))
                .sort(Sort.descending("count"))
                .limit(1);

        Iterator<Document> iterator = pipeline.aggregate(Document.class);
        if (iterator.hasNext()) {
            Document result = iterator.next();
            return result.getString("_id");
        }
        return null;
    }
    public long countPaisesFromHash(String hash) {
        return datastore.find(Acceso.class)
                .filter(Filters.eq("url.hash", hash))
                .count();
    }

    public List<String> getPaisesFromHash(String hash) {
        Query<Acceso> query = datastore.find(Acceso.class)
                .filter(Filters.eq("url.hash", hash));
        List<String> countries = new ArrayList<>();
        for (Acceso acceso : query.iterator().toList()) {
            countries.add(acceso.getPais());
        }
        return countries;
    }

}