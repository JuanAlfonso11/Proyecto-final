package org.example.modelos;
import java.util.UUID;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;
@Entity
public class CodigoQR {
    @Id
    private ObjectId id;

    private String url;

    private String tipo;

    private boolean activo;
@Reference
    private Usuario usuario;


    public CodigoQR(String url, Usuario usuario, String tipo) {
        this.url = url;
        this.usuario = usuario;
        this.tipo = tipo;
        this.activo = true;
    }

    public CodigoQR() {

    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }


}
