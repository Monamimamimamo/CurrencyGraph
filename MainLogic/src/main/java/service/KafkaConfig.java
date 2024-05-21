package service;


import common.HourlyRates;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    @Bean
    public ReplyingKafkaTemplate<String, String, String> stringReplyingKafkaTemplate(
            ProducerFactory<String, String> pf,
            ConcurrentKafkaListenerContainerFactory<String, String> factory) {

        ConcurrentMessageListenerContainer<String, String> repliesContainer = factory.createContainer("response_topic");
        repliesContainer.getContainerProperties().setGroupId("group_id");
        repliesContainer.setAutoStartup(false);

        return new ReplyingKafkaTemplate<>(pf, repliesContainer);
    }

    @Bean
    public ReplyingKafkaTemplate<String, HourlyRates, HourlyRates> hourlyRatesReplyingKafkaTemplate(
            ProducerFactory<String, HourlyRates> pf,
            ConcurrentKafkaListenerContainerFactory<String, HourlyRates> factory) {

        ConcurrentMessageListenerContainer<String, HourlyRates> repliesContainer = factory.createContainer("response_topic");
        repliesContainer.getContainerProperties().setGroupId("group_id");
        repliesContainer.setAutoStartup(false);

        return new ReplyingKafkaTemplate<>(pf, repliesContainer);
    }


    @Bean
    public ProducerFactory<String, HourlyRates> hourlyRatesProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

}
