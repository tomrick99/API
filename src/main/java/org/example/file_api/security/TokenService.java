package org.example.file_api.security;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * 负责生成和解析 JWT。
 * 使用同一把密钥生成和验证 Token。
 * Spring创建TokenService对象 放进Spring容器 Controller可以注入使用
 */
@Service
public class TokenService {

    // Token有效期为两小时
    private static final Duration TOKEN_TTL = Duration.ofHours(2);

    // 学习阶段在项目启动时生成密钥；项目重启后，旧 Token 会失效。
    // 生成签名密钥
    private final SecretKey signingKey = Jwts.SIG.HS256.key().build();

    // 生成Token发方法
    public String generateToken(String username) {

        // 内部计算时间
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(TOKEN_TTL);

        //生成JWT
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    /**
     * 验证 JWT 的签名和有效期，验证成功后返回其中的用户名。
     * Token 被篡改、格式错误或已经过期时，JJWT 会抛出 JwtException。
     */
    public String extractUsername(String token) {
        // 创建一个JWT解析工具 使用服务端的signingKey验证Token签名 用生成的相同密钥 build把前面的验证配置组装成真正的JWT解析器 parseSignedClaims解析前端传来的JWT再检查JWT格式 前面是否正确 Token有没有过期 最后读取JWT的用户名
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public long getExpirationSeconds() {
        return TOKEN_TTL.toSeconds();
    }
}
