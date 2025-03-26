package com.service.socketio.Session;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.service.socketio.service.SecurityService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class SessionManager {

    private static final Logger log = LogManager.getLogger(SessionManager.class);
    private static final long TOKEN_CHECK_INTERVAL = 2; // minutes

    private final SecurityService securityService;
    private final Map<SocketIOClient, Long> sessionExpiryMap = new ConcurrentHashMap<>();

    public SessionManager(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void addSession(SocketIOClient client, String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            long expiryTime = decodedJWT.getExpiresAt().getTime();
            sessionExpiryMap.put(client, expiryTime);
            log.debug("Tracking session {} with expiry at {}", client.getSessionId(), expiryTime);
        } catch (Exception e) {
            log.warn("Failed to track session {}: {}", client.getSessionId(), e.getMessage());
        }
    }

    public void removeSession(SocketIOClient client) {
        sessionExpiryMap.remove(client);
        log.debug("Removed session {} from tracking", client.getSessionId());
    }

    @Scheduled(fixedRate = TOKEN_CHECK_INTERVAL, timeUnit = TimeUnit.MINUTES)
    public void checkExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        log.debug("Running session expiry check at {}", currentTime);

        sessionExpiryMap.entrySet().removeIf(entry -> {
            SocketIOClient clientId = entry.getKey();
            long expiryTime = entry.getValue();

            if (expiryTime <= currentTime) {
                removeSession(clientId);
                securityService.disconnectClient(clientId);
                return true;
            }

            return false;
        });
    }

}