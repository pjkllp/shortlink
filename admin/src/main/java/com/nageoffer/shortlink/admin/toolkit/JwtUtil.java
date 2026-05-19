package com.nageoffer.shortlink.admin.toolkit;

import com.nageoffer.shortlink.admin.common.enums.UserErrorCode;
import com.nageoffer.shortlink.admin.common.exceptions.ClientException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    // 1. 固定密钥（避免重启后密钥变化导致令牌失效，生产环境建议从配置文件读取）
    private static final String SECRET_KEY = "dafsjjfnjfhfhdjfhdsfskfsdfhjfehfjfhfhsdjhfdshfdjcsdcddscdd";
    private static final SecretKey SECRET = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 2. 正确计算过期时间（当前时间 + 24小时，单位：毫秒）
    private static final long EXPIRATION_TIME = 30*60* 1000; // 30分钟
    private static final long REFRESH_EXPIRATION_TIME = 3 * 24 * 60 * 60 * 1000L; // 3天

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * 生成JWT令牌
     */
    public static String generateJwt(String id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", id);

        // 过期时间 = 当前时间 + 有效期
        Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);

        return Jwts.builder()
                .setClaims(claims) // 自定义载荷（用户ID）
                .setExpiration(expirationDate) // 过期时间
                .signWith(SECRET, SignatureAlgorithm.HS256) // 使用固定密钥签名
                .compact();
    }

    /**
     * 解析JWT令牌
     * @return 解析成功返回用户ID，失败返回-1
     */
    public static String parseJwt(String token) {
        try {
            // 移除 Bearer 前缀（如果存在）
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET) // 使用与生成时相同的密钥
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 验证令牌是否过期（虽然JWT库会自动校验，但可显式处理）
            if (claims.getExpiration().before(new Date())) {
                throw new ClientException(UserErrorCode.USER_TOKEN_ERROR);
            }

            return claims.get("username", String.class); // 返回用户ID

        } catch (Exception e) {
            throw new ClientException(UserErrorCode.USER_TOKEN_ERROR);
        }
    }

    /**
     * 生成刷新令牌，过期时间3天
     */
    public static String generateRefreshToken(String id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", id);
        claims.put("type", "refresh");

        Date expirationDate = new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SECRET, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析刷新令牌，只接受 type=refresh 的令牌
     */
    public static String parseRefreshToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.getExpiration().before(new Date())) {
                throw new ClientException(UserErrorCode.USER_TOKEN_ERROR);
            }

            if (!"refresh".equals(claims.get("type", String.class))) {
                throw new ClientException(UserErrorCode.USER_TOKEN_ERROR);
            }

            return claims.get("username", String.class);

        } catch (Exception e) {
            throw new ClientException(UserErrorCode.USER_TOKEN_ERROR);
        }
    }
}