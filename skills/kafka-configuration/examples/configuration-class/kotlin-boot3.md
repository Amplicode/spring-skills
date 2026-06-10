# Kafka `@Configuration` — Kotlin, Spring Boot 3.0 / 3.1

Generated when `(language, bootBranch) = (kotlin, boot3)` and `serializerSource = @Configuration`.

## Variables

- `{packageName}` — from Step 4.
- `{className}` — from Step 4.
- `{prefix}` — `decapitalize({producerValueType} simple name)`.
- `{producerKeyType}` / `{producerValueType}` / `{consumerKeyType}` / `{consumerValueType}` — simple names.
- `{producerKeySerializer}` / `{producerValueSerializer}` / `{consumerKeyDeserializer}` / `{consumerValueDeserializer}` — short class names; resolve FQNs via `examples/serializer-mapping.md`.
- `{jsonConsumerPropertiesLines}` — present only when `consumerKeyType` OR `consumerValueType` is a POJO; otherwise the lines are omitted. Always include `TRUSTED_PACKAGES` when any consumer side is a POJO. Include `KEY_DEFAULT_TYPE` only when `consumerKeyType` is a POJO. Include `VALUE_DEFAULT_TYPE` only when `consumerValueType` is a POJO:
  ```kotlin
  consumerProperties[JsonDeserializer.TRUSTED_PACKAGES] = "{packages}"
  consumerProperties[JsonDeserializer.KEY_DEFAULT_TYPE] = "{consumerKeyTypeFqn}"
  consumerProperties[JsonDeserializer.VALUE_DEFAULT_TYPE] = "{consumerValueTypeFqn}"
  ```
- `{listenerFactoryName}` — `kafkaListenerContainerFactory` by default; `{prefix}KafkaListenerContainerFactory` only on name collision.

## Code

```kotlin
package {packageName}

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class {className} {

    @Bean
    fun {prefix}ProducerFactory(properties: KafkaProperties): DefaultKafkaProducerFactory<{producerKeyType}, {producerValueType}> {
        val producerProperties = properties.buildProducerProperties()
        producerProperties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = {producerKeySerializer}::class.java
        producerProperties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = {producerValueSerializer}::class.java
        return DefaultKafkaProducerFactory(producerProperties)
    }

    @Bean
    fun {prefix}KafkaTemplate(
        {prefix}ProducerFactory: DefaultKafkaProducerFactory<{producerKeyType}, {producerValueType}>,
    ): KafkaTemplate<{producerKeyType}, {producerValueType}> =
        KafkaTemplate({prefix}ProducerFactory)

    @Bean
    fun {prefix}ConsumerFactory(properties: KafkaProperties): ConsumerFactory<{consumerKeyType}, {consumerValueType}> {
        val consumerProperties = properties.buildConsumerProperties()
        consumerProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = {consumerKeyDeserializer}::class.java
        consumerProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = {consumerValueDeserializer}::class.java
        {jsonConsumerPropertiesLines}
        return DefaultKafkaConsumerFactory(consumerProperties)
    }

    @Bean
    fun {listenerFactoryName}(
        {prefix}ConsumerFactory: ConsumerFactory<{consumerKeyType}, {consumerValueType}>,
    ): ConcurrentKafkaListenerContainerFactory<{consumerKeyType}, {consumerValueType}> {
        val factory = ConcurrentKafkaListenerContainerFactory<{consumerKeyType}, {consumerValueType}>()
        factory.consumerFactory = {prefix}ConsumerFactory
        return factory
    }
}
```

## Extra imports

Add an `import` line for each FQN that came in through a substitution: the four (de)serializer FQNs from `examples/serializer-mapping.md`, any POJO type FQNs, and `org.springframework.kafka.support.serializer.JsonDeserializer` when `{jsonConsumerPropertiesLines}` is present.
