package ApiService;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class api_service {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final HttpClient httpClient = HttpClient.create().secure(spec -> spec.sslContext(SslContextBuilder.forClient())
                    .defaultConfiguration(SslProvider.DefaultConfigurationType.TCP)
                    .handshakeTimeout(Duration.ofSeconds(30000))
                    .closeNotifyFlushTimeout(Duration.ofSeconds(30000))
                    .closeNotifyReadTimeout(Duration.ofSeconds(30000)))
            .resolver(spec -> spec.queryTimeout(Duration.ofSeconds(10000)))
            .doOnConnected(conn -> conn
                    .addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(10000, TimeUnit.SECONDS)));
    ;
    private final WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient)).baseUrl("https://openexchangerates.org/api").build();

    @KafkaListener(topics = "request_topic")
    @SendTo("response_topic")
    public String getCurrencyData(String url) {
        Map<String, Object> data = webClient.get()
                .uri(url)
                .header("Authorization", "Token efdb29251f0e4c1ab8ec069799669557")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Received data is null or empty");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }



}
