package org.example.API.modelos.REST.controladoresREST;

import org.example.modelos.Usuario;
import org.example.servicios.UsuarioServicios;
import io.javalin.Javalin;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.jasypt.util.text.StrongTextEncryptor;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

public class LoginControllerREST {
    Javalin app;
    private StrongTextEncryptor encriptador;
    public LoginControllerREST(Javalin app){this.app = app;}

    private static String SECRET_KEY = "oeRaYY7Wo24sDqKSX3IM9ASGmdGPmkTd9jo1QTy4b7P9Ze5_9hKolVX8xNrQDcNRfVEdTZNOuOyqEGhXEbdJI-ZQ19k_o9MI0y3eZN2lp9jow55FfXMiINEdt1XR85VipRLSOkT6kSpzs2x-jbLDiz9iFVzkd81YKxMgPA7VfZeQUm4n-mOmnWMaVX30zGFU4L3oPBctYKkl4dYfqYWqRNfrgPJVi5DGFjywgxx0ASEiJHtV72paI3fDR2XwlSkyhhmY-ICjCRmsJN4fX1pdoL8a18-aQrvyu4j0Os6dVPYIoPvvY0SAZtWYKHfM15g7A3HD4cVREf9cUsprCRK93w";
    private Key secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public static String createJWT(String id, String isseur, String subject, long ttlMillis){
        SignatureAlgorithm signatureAlgorithm= SignatureAlgorithm.HS256;
        long nowMillis= System.currentTimeMillis();
        Date now= new Date(nowMillis);
        byte[] bytes= SECRET_KEY.getBytes();
        Key signingKey= new SecretKeySpec(bytes, signatureAlgorithm.getJcaName());

        JwtBuilder builder= Jwts.builder().setId(id)
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(isseur)
                .signWith(signatureAlgorithm, signingKey);
        if(ttlMillis>0){
            long expMillis= nowMillis+ttlMillis;
            Date exp= new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }
    public Usuario JwtVerifier(String jwtToken){
        try{
            Jws<Claims> jsw= Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwtToken);
            Claims claims= jsw.getBody();
            return verificarJWTUser(claims.getSubject());
        } catch (io.jsonwebtoken.security.SecurityException e){
            System.out.println("JWT token has an invalid signature");
        }catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("JWT token has expired");
        }catch(Exception e){
            System.out.println("Error:"+e.getMessage());
        }
        return null;
    }
    public Boolean Autenticar(String usuarioID, String password){
        Usuario user = UsuarioServicios.getInstance().getUsuarioByUsuario(usuarioID);
        if(user != null){
            encriptador = new StrongTextEncryptor();
            encriptador.setPassword("ExamenFinal");
            return user.isHabilitado() && encriptador.decrypt(user.getPassword()).equals(password);
        }
        else{
            return false;
        }
    }
    public Usuario verificarJWTUser(String usuarioID){
        Usuario user = UsuarioServicios.getInstance().getUsuarioByUsuario(usuarioID);
        if(user!=null){
            return user;
        }
        else{
            return null;
        }
    }

}
