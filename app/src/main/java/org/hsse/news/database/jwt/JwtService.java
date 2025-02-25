package org.hsse.news.database.jwt;

import io.jsonwebtoken.Jwts;
import org.hsse.news.database.user.models.UserId;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey secretKey = Jwts.SIG.HS256.key().build();

    public UserId getUserId(final String token) {
        final String subject = Jwts.parser()
                .verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
        return new UserId(UUID.fromString(subject));
    }

    public String generateToken(final UserId userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                .signWith(secretKey).compact();
    }
}
