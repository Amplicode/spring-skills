# Kafka `@Configuration` — Java, Spring Boot 4

Generated when `(language, bootBranch) = (java, boot4)` and `serializerSource = @Configuration`.

## Variables

- `{packageName}` — from Step 4.
- `{className}` — from Step 4.
- `{prefix}` — `decapitalize({producerValueType} simple name)`.
- `{producerKeyType}` / `{producerValueType}` / `{consumerKeyType}` / `{consumerValueType}` — simple names.
- `{producerKeySerializer}` / `{producerValueSerializer}` / `{consumerKeyDeserializer}` / `{consumerValueDeserializer}` — short class names; resolve FQNs via `examples/serializer-mapping.md`.
- `{jsonConsumerPropertiesLines}` — present only when `consumerKeyType` OR `consumerValueType` is a POJO; otherwise the lines are omitted. Always include `TRUSTED_PACKAGES` when any consumer side is a POJO. Include `KEY_DEFAULT_TYPE` only when `consumerKeyType` is a POJO. Include `VALUE_DEFAULT_TYPE` only when `consumerValueType` is a POJO:
  ```java
  consumerProperties.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "{packages}");
  consumerProperties.put(JacksonJsonDeserializer.KEY_DEFAULT_TYPE, "{consumerKeyTypeFqn}");
  consumerProperties.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, "{consumerValueTypeFqn}");
  ```
- `{listenerFactoryName}` — `kafkaListenerContainerFactory` by default; `{prefix}KafkaListenerContainerFactory` only when the default name already exists in the module.

## Code

```java
package {packageName};

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

@Configuration
public class {className} {

    @Bean
    DefaultKafkaProducerFactory<{producerKeyType}, {producerValueType}> {prefix}ProducerFactory(KafkaProperties properties) {
        Map<String, Object> producerProperties = properties.buildProducerProperties();
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, {producerKeySerializer}.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, {producerValueSerializer}.class);
        return new DefaultKafkaProducerFactory<>(producerProperties);
    }

    @Bean
    KafkaTemplate<{producerKeyType}, {producerValueType}> {prefix}KafkaTemplate(
            DefaultKafkaProducerFactory<{producerKeyType}, {producerValueType}> {prefix}ProducerFactory) {
        return new KafkaTemplate<>({prefix}ProducerFactory);
    }

    @Bean
    ConsumerFactory<{consumerKeyType}, {consumerValueType}> {prefix}ConsumerFactory(KafkaProperties properties) {
        Map<String, Object> consumerProperties = properties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, {consumerKeyDeserializer}.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, {consumerValueDeserializer}.class);
        {jsonConsumerPropertiesLines}
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<{consumerKeyType}, {consumerValueType}> {listenerFactoryName}(
            ConsumerFactory<{consumerKeyType}, {consumerValueType}> {prefix}ConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<{consumerKeyType}, {consumerValueType}> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory({prefix}ConsumerFactory);
        return factory;
    }

}
```

## Extra imports

Add an `import` line for each FQN that came in through a substitution: the four (de)serializer FQNs from `examples/serializer-mapping.md`, any POJO type FQNs, and `org.springframework.kafka.support.serializer.JacksonJsonDeserializer` when `{jsonConsumerPropertiesLines}` is present.
