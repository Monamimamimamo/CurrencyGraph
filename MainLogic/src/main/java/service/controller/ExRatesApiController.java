package service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.ExchangeRatesApi;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import common.repo.ExRatesApiRepo;
import common.repo.HourlyRatesRepo;
import service.service.ExRatesApiService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/exchange_rates/api")
public class ExRatesApiController {
    private final ExRatesApiService exRatesApiService;
    private final ExRatesApiRepo exRatesApiRepo;
    private final HourlyRatesRepo hourlyRatesRepo;
    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    @PutMapping("/update_db/{year}")
    public Mono<String> updateDb(@PathVariable("year") int year) {
        int daysInYear = LocalDate.of(year, 12, 31).isLeapYear() ? 366 : 365;
        return Flux.range(1, daysInYear)
                .map(day -> LocalDate.of(year, 1, 1).plusDays(day - 1))
                .flatMap(date -> {
                    // Создаем сообщение для отправки
                    String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    ProducerRecord<String, String> record = new ProducerRecord<>("request_topic", String.format("/historical/%s.json", dateString));
                    record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "response_topic".getBytes()));
                    RequestReplyFuture<String, String, String> future = replyingKafkaTemplate.sendAndReceive(record, Duration.ofSeconds(600));

                    return Mono.fromFuture(future)
                            .map(response -> {
                                ObjectMapper objectMapper = new ObjectMapper();
                                Map<String, Object> responseMap = null;
                                try {
                                    responseMap = objectMapper.readValue(response.value(), Map.class);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                                return responseMap;
                            })
                            .flatMap(responseMap -> exRatesApiService.updateDB(Mono.justOrEmpty(responseMap), date));
                })
                .then(Mono.just("Обновление завершено"));
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/daily/{charCode}")
    List<ExchangeRatesApi> getDaily(@PathVariable String charCode,
                                    @RequestParam(required = false) int size) {
        return exRatesApiService.dailyCurrency(charCode, size);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/weekly/{charCode}")
    List<Map<String, Object>> getWeekly(@PathVariable String charCode,
                                        @RequestParam(required = false) int size) {
        return exRatesApiService.weeklyCurrency(charCode, size);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/monthly/{charCode}")
    List<Map<String, Object>> getMonthly(@PathVariable String charCode,
                                         @RequestParam(required = false) int size) {
        return exRatesApiService.monthlyCurrency(charCode, size);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/yearly/{charCode}")
    List<Map<String, Object>> getYearly(@PathVariable String charCode,
                                        @RequestParam(required = false) int size) {
        return exRatesApiService.yearlyCurrency(charCode, size);
    }


//    @PutMapping("/hourly/update_db")
//    @Scheduled(cron = "0 0 * * * *")
//    public Mono<String> hourly_update_db() throws Exception {
//        ProducerRecord<String, String> record = new ProducerRecord<>("request_topic", "/latest.json");
//        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "response_topic".getBytes()));
//        RequestReplyFuture<String, String, String> future = replyingKafkaTemplate.sendAndReceive(record);
//        String response = future.get().value();
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
//        return exRatesApiService.hourly_update_db(Mono.just(responseMap))
//                .thenReturn("Обновление завершено");
//    }
//
//    @PutMapping("/daily/update_db")
//    @Scheduled(cron = "0 5 * * * ?")
//    public Mono<String> dailyUpdate() throws Exception {
//        LocalDate yesterday = LocalDate.now().minusDays(1);
//        ProducerRecord<String, String> record = new ProducerRecord<>("request_topic", String.format("/historical/%s.json", yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
//        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "response_topic".getBytes()));
//        RequestReplyFuture<String, String, String> future = replyingKafkaTemplate.sendAndReceive(record);
//        String response = future.get().value();
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
//        return exRatesApiService.dailyUpdate(Mono.just(responseMap), yesterday)
//                .thenReturn("Обновление завершено");
//    }
}
