package CurrencyRateAPI.service;

import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class WSClient {

    public static void main(String[] args) {
        // Создание клиента WebSocket
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // Подключение к серверу
        String url = "ws://localhost:8008/ws";
        MyStompSessionHandler sessionHandler = new MyStompSessionHandler();
        try {
            stompClient.connect(url, sessionHandler).get();
            System.out.println("Подключились");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        StompSession session = sessionHandler.getSession();
        session.subscribe("/topic/greetings", sessionHandler);

        // Отправка сообщения
        session.send("/app/greetings", "Hello");

        // Ожидание ввода пользователя для завершения работы
        new Scanner(System.in).nextLine();

        // Закрытие сессии
        session.disconnect();
    }

    static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        private StompSession session;

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            this.session = session;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("Получено сообщение: " + payload);
        }

        public StompSession getSession() {
            return session;
        }
    }
}
