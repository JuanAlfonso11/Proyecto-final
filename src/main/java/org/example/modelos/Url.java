package org.example.modelos;
import org.example.contoladores.Direccionamiento;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;
@Entity
public class Url {
    @Id
    private ObjectId id;
    private String urlOriginal;
    private String titulo;
    private String hash;
    private Date fechaCreacion;
    private String fechaStirng;
    private String direccion;
    private boolean activo;
    @Reference
    private Usuario usuario;
    @Reference
    private List<Acceso> accesos= new ArrayList<>();

    public Url(String urlOriginal,String titulo, Date fechaCreacion,String hash, Usuario usuario) throws UnknownHostException{
        if(urlOriginal.contains("http://")||urlOriginal.contains("https://")){
            this.urlOriginal= urlOriginal;
        }
        else {
            this.urlOriginal="http://"+ urlOriginal;
        }
        this.hash = hash;
        this.titulo=titulo;
        this.fechaCreacion=fechaCreacion;
        this.usuario=usuario;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        this.fechaStirng =formatter.format(fechaCreacion);
        this.direccion = Direccionamiento.getInstance().getDireccionServidor() + "/" + hash;
        this.activo=true;
    }
    public Url(){}

    public String getTitulo(){return titulo;}
    public void setTitulo(String titulo){this.titulo=titulo;}
    public ObjectId getId(){return id;}
    public void setId(ObjectId id){this.id=id;}
    public String getUrlOriginal(){return urlOriginal;}
    public void setUrlOriginal(String urlOriginal){this.urlOriginal=urlOriginal;}
    public String getHash(){return hash;}
    public void setHash(String hash){this.hash=hash;}
    public Date getFechaCreacion(){return fechaCreacion;}
    public void setFechaCreacion(Date fechaCreacion){this.fechaCreacion=fechaCreacion;}
    public String getDireccion(){return direccion;}
    public void setDireccion(String direccion){this.direccion=direccion;}
    public Usuario getUsuario(){return usuario;}
    public void setUsuario(Usuario usuario){this.usuario=usuario;}
    public List<Acceso> getAccesos(){return accesos;}
    public void setAccesos(List<Acceso> accesos){this.accesos=accesos;}
    public String getFechaString(){return fechaStirng;}
    public void setFechaString(String fechaStirng){this.fechaStirng=fechaStirng;}
    public boolean isActivo(){return activo;}
    public void setActivo(boolean activo){this.activo=activo;}


}
