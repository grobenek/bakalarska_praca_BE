package szathmary.peter.bakalarka.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {
  @Value("${spring.security.jwt.secret}")
  private String jwtSecret;

  @Value("${spring.security.jwt.expiration}")
  private int jwtExpirationMs;

  public String generateJwt(String username) {
    SecretKey key = getSigningKey();

    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(key)
        .compact();
  }

  public String getUsernameFromJwt(String token) {
    SecretKey key = getSigningKey();
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public boolean validateJwt(String authToken) {
    try {
      SecretKey key = getSigningKey();
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
      return true;
    } catch (SignatureException
        | MalformedJwtException
        | UnsupportedJwtException
        | IllegalArgumentException
        | ExpiredJwtException e) {
      log.error("Error thrown validating jwt token: {}", e.getLocalizedMessage());
    }
    return false;
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
