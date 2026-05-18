package com.ticketsecure.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;
// 1. Usa o novo conversor atualizado do Spring AMQP 4.0+ (Sem o "2" no nome)
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 2. Importação CORRETA do pacote moderno (tools.jackson) e da classe (JsonMapper)
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQconfig {
    public static final String FRAUD_CHECK_QUEUE = "ticketsecure.fraud.check.queue";
    public static final String FRAUD_RESULT_QUEUE = "ticketsecure.fraud.result.queue";
    
    @Bean
    public Queue fraudCheckQueue() {
        return new Queue(FRAUD_CHECK_QUEUE, true);
    }
    
    @Bean
    public Queue fraudResultQueue() {
        return new Queue(FRAUD_RESULT_QUEUE, true);
    }

    // 3. Recebe o JsonMapper (novo tradutor padrão) que o Spring Boot 4 já configurou para nós
    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        // Retorna o conversor moderno, perfeitamente compatível e sem avisos!
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(jsonMapper);
        return converter;
    
    }
}