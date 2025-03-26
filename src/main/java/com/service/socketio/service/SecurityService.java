package com.service.socketio.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.corundumstudio.socketio.SocketIOClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {

    private static final Logger log = LogManager.getLogger(SecurityService.class);

    private final String tokenSecret;

    public SecurityService(Environment environment) {
        this.tokenSecret = Optional.ofNullable(environment.getProperty("auth.token.secret"))
                .orElseThrow(() -> new IllegalStateException("Token Secret is not defined in application.properties"));
    }

    public boolean validateToken(String token) {
        try {
            DecodedJWT decodedJwt = JWT.require(Algorithm.HMAC512(tokenSecret))
                    .build()
                    .verify(token);
            return true;
        } catch (TokenExpiredException e) {
            log.warn("Token is Expired: {}", e.getMessage());
        } catch (JWTVerificationException e) {
            log.warn("Token is Invalid: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error while Token Validation: ", e);
        }
        return false;
    }

    public void disconnectClient(SocketIOClient client) {

        if (client == null) {
            log.warn("Attempted to disconnect a null client");
            return;
        }

        try {
            client.disconnect();
            log.info("Disconnected client: {}.", client.getSessionId());
        } catch (Exception e) {
            log.error("Failed to disconnect client: {}", client.getSessionId(), e);
        }
    }
}
