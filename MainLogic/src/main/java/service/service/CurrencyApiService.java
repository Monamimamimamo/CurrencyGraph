package service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.repo.CurrenciesApiRepo;
import common.Currencies_Api;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CurrencyApiService{
    private final CurrenciesApiRepo currenciesApiRepo;

    public Mono<List<Currencies_Api>> takeValutesData_api(Mono<Map<String, Object>> valuteList) {
        return valuteList.map(valuteMap -> {
            List<Currencies_Api> result = new ArrayList<>();
            for (Map.Entry<String, Object> entry : valuteMap.entrySet()) {
                Currencies_Api currency = new Currencies_Api();
                currency.setCharCode(entry.getKey());
                currency.setName(entry.getValue() != null ? entry.getValue().toString() : null);

                currenciesApiRepo.save(currency);
                result.add(currency);
            }
            return result;
        });
    }
    public Map<String, Object> getLatestFromApi (ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate) throws Exception {
        ProducerRecord<String, String> record = new ProducerRecord<>("request_topic", "/latest.json");
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "response_topic".getBytes()));
        RequestReplyFuture<String, String, String> future = replyingKafkaTemplate.sendAndReceive(record);
        String response = future.get().value();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response, Map.class);
    }
}
