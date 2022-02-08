package org.tperio.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tperio.domain.entity.Usuario;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${spring.jwt.expiration}")
    private String expiration;

    @Value("${spring.jwt.secret}")
    private String secret;

    public String gerarToken( Usuario usuario ){
        long expString = Long.valueOf(expiration);
        LocalDateTime dataHoraExpiracao = LocalDateTime.now().plusMinutes(expString);
        Instant instant = dataHoraExpiracao.atZone(ZoneId.systemDefault()).toInstant();
        Date data = Date.from(instant);

        return Jwts
                .builder()
                .setSubject(usuario.getLogin())
                .setExpiration(data)
                .signWith(SignatureAlgorithm.HS512, secret )
                .compact();
    }

    public boolean tokenValido( String token ){
        try{
            Claims claims = obterClaims(token);
            Date dataExpiracao = claims.getExpiration();
            LocalDateTime data =
                    dataExpiracao.toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime();
            return !LocalDateTime.now().isAfter(data);
        }catch (Exception e){
            return false;
        }
    }

    public String obterLoginUsuario(String token) throws ExpiredJwtException {
        return (String) obterClaims(token).getSubject();
    }

    private Claims obterClaims( String token ) throws ExpiredJwtException {
        return Jwts
                .parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
