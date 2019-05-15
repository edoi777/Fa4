package nurgs.tool.batch.kafka;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;

public class KafkaSender {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSender.class);
    
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectWriter objectWriter;
    
    public KafkaSender(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        this.objectWriter = objectMapper.writer();
    }
    
    
    public void send(String jsonPayload) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "UserSpinHistory");
        map.put("payload", jsonPayload);
        try {
            String data = objectWriter.writeValueAsString(map);
            LOGGER.info("Sending to kafka : {}", data);
            kafkaTemplate.sendDefault(data)
            .addCallback(successCallback ->  LOGGER.debug("Kafka Success : {}", data), 
                    failureCallback -> LOGGER.error("Kafka Failed : {}", data));
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed processing payload : {}", jsonPayload);
        }
    }
    
    
    
}
