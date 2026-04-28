package com.backend.kashiapp.user.infraestructure.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

    //Clave y tiempo de expiracion del token, inyectados desde el archivo de configuracion
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    //Metodo para generar un token JWT
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email) 
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
    //Metodo para extraer el correo del usuario
    public String extractEmail(String token){
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}