package com.service.socketio.controller;

import com.service.socketio.Session.SessionManager;
import com.service.socketio.service.SecurityService;
import com.service.socketio.service.SocketIOService;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

@CrossOrigin(origins = "*")
@Component
public class SocketIOController {

    private static final Logger log = LogManager.getLogger(SocketIOController.class);
    private static final String NAMESPACE_PREFIX = "/socket-io/";
    private static final String SELFCHECK_SEND_EVENT = "selfcheckSend";
    private static final String SELFCHECK_CONNECTION_EVENT = "selfcheckConnection";
    private static final String AUTH_HEADER = "Authorization";

    private final SocketIOServer socketServer;

    private final SecurityService securityService;
    private final SocketIOService socketIOService;
    private final SessionManager sessionManager;

    @Autowired
    public SocketIOController(SocketIOServer socketServer,
                              SecurityService securityService,
                              SocketIOService socketIOService,
                              SessionManager sessionManager) {
        this.socketServer = Objects.requireNonNull(socketServer, "SocketIOServer cannot be null");
        this.securityService = Objects.requireNonNull(securityService, "SecurityService cannot be null");
        this.socketIOService = Objects.requireNonNull(socketIOService, "SocketIOService cannot be null");
        this.sessionManager = Objects.requireNonNull(sessionManager, "SessionManager cannot be null");
        initializeListeners();
    }

    private void initializeListeners() {
        List<String> namespaces = socketIOService.fetchNamespaces();
        if (namespaces.isEmpty()) {
            log.warn("No namespaces found. Socket.IO listeners won't be initialized.");
            return;
        }

        namespaces.forEach(namespace -> {
            String fullNamespace = NAMESPACE_PREFIX + namespace;
            try {
                initializeNamespaceListeners(fullNamespace);
            } catch (Exception e) {
                log.error("Failed to initialize listeners for namespace: {}", fullNamespace, e);
            }
        });
    }

    private void initializeNamespaceListeners(String namespace) {
        var namespaceInstance = socketServer.addNamespace(namespace);

        namespaceInstance.addConnectListener(createConnectListener(namespace));
        namespaceInstance.addDisconnectListener(createDisconnectListener(namespace));
        namespaceInstance.addEventListener(SELFCHECK_SEND_EVENT, Object.class, createMessageSendListener(namespace));
        namespaceInstance.addEventListener(SELFCHECK_CONNECTION_EVENT, Object.class, createSelfcheckConnectionListener(namespace));

        log.info("Initialized listeners for namespace: {}", namespace);
    }

    private ConnectListener createConnectListener(String namespace) {
        return client -> {
            String token = extractToken(client.getHandshakeData().getHttpHeaders(),
                    client.getHandshakeData().getAuthToken());

            if (!isTokenValid(token)) {
                log.warn("Failed authentication for connection in namespace: {}. Client IP: {}",
                        namespace, client.getRemoteAddress());
                client.disconnect();
                return;
            }

            sessionManager.addSession(client, token);

            log.info("User connected with valid token in namespace: {}. Client ID: {}",
                    namespace, client.getSessionId());
            sendConnectionStatus(namespace, client, true);
        };
    }

    private DisconnectListener createDisconnectListener(String namespace) {
        return client -> {
            sessionManager.removeSession(client);
            log.info("User disconnected in namespace: {}. Client ID: {}", namespace, client.getSessionId());
            sendConnectionStatus(namespace, client, false);
        };
    }

    private void sendConnectionStatus(String namespace, com.corundumstudio.socketio.SocketIOClient client, boolean status) {
        try {
            socketServer.getNamespace(namespace)
                    .getBroadcastOperations()
                    .sendEvent(SELFCHECK_CONNECTION_EVENT, client, status);
        } catch (Exception e) {
            log.error("Error sending connection status for namespace: {}", namespace, e);
        }
    }

    private boolean isTokenValid(String token) {
        return token != null && securityService.validateToken(token);
    }

    private DataListener<Object> createMessageSendListener(String namespace) {
        return (client, message, acknowledge) -> {
            if (message == null) {
                handleInvalidData(namespace, "Invalid message received", acknowledge);
                return;
            }

            log.info("{}: Message received from self-check device. Client ID: {}, message: {}",
                    namespace, client.getSessionId(), message);

            try {
                socketServer.getNamespace(namespace)
                        .getBroadcastOperations()
                        .sendEvent(SELFCHECK_SEND_EVENT, client, message);

                sendAck(acknowledge, "Message sent successfully in namespace: " + namespace);
            } catch (Exception e) {
                log.error("{}: Error sending Message. Client ID: {}", namespace, client.getSessionId(), e);
                sendAck(acknowledge, "Error: Failed to send Message in namespace: " + namespace);
            }
        };
    }

    private DataListener<Object> createSelfcheckConnectionListener(String namespace) {
        return (client, message, acknowledge) -> {
            if (message == null) {
                handleInvalidData(namespace, "Invalid Message received", acknowledge);
                return;
            }

            log.info("{}: Message received from self-check device. Client ID: {}, Message: {}",
                    namespace, client.getSessionId(), message);

            try {
                socketServer.getNamespace(namespace)
                        .getBroadcastOperations()
                        .sendEvent(SELFCHECK_CONNECTION_EVENT, client, message);

                sendAck(acknowledge, "Message sent successfully in namespace: " + namespace);
            } catch (Exception e) {
                log.error("{}: Error sending Message. Client ID: {}", namespace, client.getSessionId(), e);
                sendAck(acknowledge, "Error: Failed to send Message in namespace: " + namespace);
            }
        };
    }

    private String extractToken(HttpHeaders headers, Object authToken) {
        if (headers != null && headers.contains(AUTH_HEADER)) {
            return headers.get(AUTH_HEADER);
        }

        if (authToken instanceof LinkedHashMap) {
            LinkedHashMap<?, ?> tokenMap = (LinkedHashMap<?, ?>) authToken;
            Object authTokenValue = tokenMap.get(AUTH_HEADER);
            return authTokenValue != null ? authTokenValue.toString() : null;
        }

        if (authToken instanceof String) {
            return (String) authToken;
        }

        return null;
    }

    private void sendAck(com.corundumstudio.socketio.AckRequest acknowledge, String message) {
        if (acknowledge != null && !acknowledge.isAckRequested()) {
            try {
                acknowledge.sendAckData(message);
            } catch (Exception e) {
                log.error("Failed to send acknowledgement", e);
            }
        }
    }

    private void handleInvalidData(String namespace, String errorMessage, com.corundumstudio.socketio.AckRequest acknowledge) {
        log.warn("{}: {}", namespace, errorMessage);
        sendAck(acknowledge, "Error: " + errorMessage + " in namespace: " + namespace);
    }
}