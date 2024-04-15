package org.example.contoladores;

import java.net.InetAddress;
import java.net.UnknownHostException;
public class Direccionamiento {
    private static  Direccionamiento intancia;
    private final String redServidor;
    private final int puertoServidor;
    private String direccionServidor;

    public Direccionamiento(String red, int puerto){
        this.redServidor=red;
        this.puertoServidor=puerto;
        if(puerto==80){
            this.direccionServidor=red;
        }else if(puerto==443){
            this.direccionServidor="https://" + red;
        }else{
            this.direccionServidor="http://" + red + ":" + puerto;
        }
    }

    public static Direccionamiento getInstance() throws  UnknownHostException{
        if(intancia==null){
            InetAddress addr = InetAddress.getLocalHost();
            String ip = addr.getHostAddress();
            intancia = new Direccionamiento(ip,5000);
        }
        return intancia;
    }
    public String getRedServidor(){return redServidor;}
    public int getPuertoServidor(){return  puertoServidor;}
    public String getDireccionServidor(){return direccionServidor;}
}
