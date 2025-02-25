package org.hsse.news.database.jwt;

import io.jsonwebtoken.Jwts;
import org.hsse.news.database.user.models.UserId;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey secretKey = Jwts.SIG.HS256.key().build();

    public UserId getUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
        return new UserId(UUID.fromString(subject));
    }
}
