package com.example.spring.dropbox.config;


import com.example.spring.dropbox.pojo.OrderRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;

import java.util.concurrent.CopyOnWriteArrayList;

public class MyWebSocketHandler extends TextWebSocketHandler {
    private static List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
     System.out.println("conection establesd");
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming WebSocket messages here
        String payload = message.getPayload();
        System.out.println("payload - " +  payload);
        // Process the message and send responses
       // session.sendMessage(new TextMessage("Received: " + payload));
        broadcastMessage("broad cast" + payload);
    }


    public void broadcastMessage(String message) {
        cleanupClosedSessions();
        for (WebSocketSession session : sessions) {
            try {
                System.out.println("broadcastMessage ");
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastObject(OrderRequestDto request) {
        cleanupClosedSessions();

        for (WebSocketSession session : sessions) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                System.out.println("broadcastMessage ");
                session.sendMessage(new TextMessage( mapper.writeValueAsString(request)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cleanupClosedSessions() {
        sessions.forEach(session->{
            if(!session.isOpen())
            {
                sessions.remove(session);
            }
        });
    }
}
