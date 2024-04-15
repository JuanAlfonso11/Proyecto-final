package org.example.utils;
import io.github.cdimascio.dotenv.Dotenv;
public class EnvServices {
    public static EnvServices instancia;
    public static EnvServices getInstance(){
        if(instancia==null){
            instancia = new EnvServices();
        }
        return instancia;
    }
    public String result(String clave){
        Dotenv dotenv = Dotenv.configure()
                .directory("/")
                .filename(".env")
                .load();
        return dotenv.get(clave);
    }
}
