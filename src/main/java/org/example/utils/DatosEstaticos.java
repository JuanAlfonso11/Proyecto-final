package org.example.utils;

public enum DatosEstaticos {

    USUARIO("USUARIO"),
    URL_MONGO("URL_MONGO"),
    DB_NOMBRE("DB_NOMBRE"),
    DOMINIO("jalvaradobatista.me");

    private String valor;

    DatosEstaticos(String valor){
        this.valor =  valor;
    }

    public String getValor() {
        return valor;
    }
}
