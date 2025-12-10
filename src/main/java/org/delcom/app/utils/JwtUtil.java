package org.delcom.app.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

public class JwtUtil {
    // Kunci rahasia dummy. Ganti dengan kunci yang aman di aplikasi nyata.
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); 
    private static final long EXPIRATION_TIME = 86400000; // 24 jam

    /**
     * Metode dummy untuk validasi token.
     */
    public static boolean validateToken(String token, boolean allowExpired) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Jika tidak diizinkan expired, cek tanggal
            if (!allowExpired && claims.getExpiration().before(new Date())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            // Token tidak valid, format salah, atau expired (jika allowExpired=false)
            return false;
        }
    }

    /**
     * Metode dummy untuk ekstrak userId dari token.
     */
    public static UUID extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String userIdString = claims.getSubject();
            if (userIdString != null) {
                return UUID.fromString(userIdString);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}