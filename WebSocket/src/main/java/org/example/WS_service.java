package org.example;

import common.HourlyRates;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class WS_service {

    private final SimpMessagingTemplate simpMessagingTemplate;


    @KafkaListener(topics = "ws_topic", errorHandler = "customKafkaListenerErrorHandler")
    public void getCurrencyData(ConsumerRecord<String, HourlyRates> record) {
        try {
            if (record.value() == null || record.headers() == null) {
                log.warn("Пустое сообщение или заголовки отсутствуют");
                return;
            }
            Headers headers = record.headers();
            HourlyRates exchange = record.value();
            String charCode = null;

            for (Header header : headers) {
                if (header.key().equals("charCode")) {
                    charCode = new String(header.value());
                    break;
                }
            }

            if (charCode == null || !charCode.matches("[A-Z]{3}")) {
                log.warn("Некорректный формат кода валюты: {}", charCode);
                return;
            }

            if (exchange == null) {
                log.warn("Объекта валюты не существует");
                return;
            }

            simpMessagingTemplate.convertAndSend(STR."/topic/hourly/\{charCode}", exchange);
            simpMessagingTemplate.convertAndSend(STR."/topic/daily/\{charCode}", exchange);
            simpMessagingTemplate.convertAndSend(STR."/topic/weekly/\{charCode}", exchange);
            simpMessagingTemplate.convertAndSend(STR."/topic/monthly/\{charCode}", exchange);
            simpMessagingTemplate.convertAndSend(STR."/topic/yearly/\{charCode}", exchange);


        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения Kafka: {}", e.getMessage());
        }
    }
}
