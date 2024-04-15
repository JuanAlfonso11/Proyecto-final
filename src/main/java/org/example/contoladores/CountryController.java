package org.example.contoladores;
import  com.maxmind.geoip2.DatabaseReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
public class CountryController {
    private static CountryController instancia;
    private final String defaultCountry = "Dominica Republic";


    private CountryController() {

    }

    public static CountryController getInstance() {
        if (instancia == null) {
            instancia = new CountryController();
        }
        return instancia;
    }
    public String getPublicIP() throws Exception{
        try{
            URL whatIsMyIp = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatIsMyIp.openStream()));
            return in.readLine();
        } catch (IOException e){
            System.out.println("Error: "+ e.getMessage());
            return null;
        }
    }

    public String getCountry(String ip) throws Exception{
        String publicIP = null;
        try {
            InetAddress inetAddress= InetAddress.getByName(ip);
            byte[] addressByte = inetAddress.getAddress();
            if(inetAddress.isSiteLocalAddress()|| inetAddress.isAnyLocalAddress()||(addressByte[0]&0x80)==0)
            {
                publicIP=getPublicIP();
            }
            else {
                publicIP=ip;
            }
            File database = new File("src/main/resources/publico/DB/GeoLite2-Country.mmdb");
            DatabaseReader reader = new  DatabaseReader.Builder(database).build();
            InetAddress ipAddress = InetAddress.getByName(publicIP);
            String country = reader.country(ipAddress).getCountry().getName();
            System.out.println(country);
            return country;
        }catch (Exception e){
            System.out.println("Error: "+ e.getMessage());
            return defaultCountry;
        }
    }
}