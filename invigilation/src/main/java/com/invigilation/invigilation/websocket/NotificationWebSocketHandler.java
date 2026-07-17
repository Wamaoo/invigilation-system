package com.invigilation.invigilation.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // admin 会话: sessionId → session
    private final Map<String, WebSocketSession> adminSessions = new ConcurrentHashMap<>();
    // teacher 会话: teacherId → session
    private final Map<String, WebSocketSession> teacherSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return;

        String query = uri.getQuery();
        String role = getQueryParam(query, "role");
        String username = getQueryParam(query, "username");

        if ("admin".equals(role)) {
            adminSessions.put(session.getId(), session);
            log.info("Admin WebSocket 已连接: username={}, sessionId={}", username, session.getId());
        } else if ("teacher".equals(role)) {
            teacherSessions.put(username, session);
            log.info("Teacher WebSocket 已连接: teacherId={}, sessionId={}", username, session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        adminSessions.remove(session.getId());
        teacherSessions.values().remove(session);
        log.info("WebSocket 已断开: sessionId={}", session.getId());
    }

    /** 推送给所有在线的管理员 */
    public void notifyAdmin(String title, String message) {
        sendToSessions(adminSessions.values(), title, message, "admin");
    }

    /** 推送给指定教师 */
    public void notifyTeacher(String teacherId, String title, String message) {
        WebSocketSession session = teacherSessions.get(teacherId);
        if (session != null && session.isOpen()) {
            sendMessage(session, buildPayload(title, message, "teacher"));
        }
    }

    private void sendToSessions(Iterable<WebSocketSession> sessions, String title, String message, String role) {
        String payload = buildPayload(title, message, role);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                sendMessage(session, payload);
            }
        }
    }

    private void sendMessage(WebSocketSession session, String payload) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (IOException e) {
            log.error("WebSocket 发送消息失败: {}", e.getMessage());
        }
    }

    private String buildPayload(String title, String message, String targetRole) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "title", title,
                    "message", message,
                    "targetRole", targetRole,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return "{\"title\":\"" + title + "\",\"message\":\"" + message + "\"}";
        }
    }

    private String getQueryParam(String query, String key) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) {
                return pair[1];
            }
        }
        return null;
    }
}
