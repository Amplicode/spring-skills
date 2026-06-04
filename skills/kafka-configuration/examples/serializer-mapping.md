# Serializer / deserializer mapping

This table drives Step 5a (write properties) and Step 6 (generate `@Configuration` class). One lookup per type parameter; the skill has four independent type parameters (`producerKeyType`, `producerValueType`, `consumerKeyType`, `consumerValueType`), each resolved against the same table.

The serializer column is used on the producer side (writes `producer.{key|value}-serializer` or `props.put(..., XxxSerializer.class)` inside `{prefix}ProducerFactory`). The deserializer column is used on the consumer side (writes `consumer.{key|value}-deserializer` or `props.put(..., XxxDeserializer.class)` inside `{prefix}ConsumerFactory`).

Spring Boot's `KafkaAutoConfiguration` reads the property-source variant through `KafkaProperties.buildProducerProperties()` / `buildConsumerProperties()` and applies them to the auto-created `ProducerFactory` / `ConsumerFactory`.

## Type → (serializer, deserializer) FQN

All FQNs are real classes shipped by `org.apache.kafka:kafka-clients` (basic types) or `org.springframework.kafka:spring-kafka` (JSON variants).

| Type | `bootBranch` | Serializer FQN | Deserializer FQN |
|---|---|---|---|
| `java.lang.String` | any | `org.apache.kafka.common.serialization.StringSerializer` | `org.apache.kafka.common.serialization.StringDeserializer` |
| `java.lang.Integer` | any | `org.apache.kafka.common.serialization.IntegerSerializer` | `org.apache.kafka.common.serialization.IntegerDeserializer` |
| `java.lang.Long` | any | `org.apache.kafka.common.serialization.LongSerializer` | `org.apache.kafka.common.serialization.LongDeserializer` |
| `java.util.UUID` | any | `org.apache.kafka.common.serialization.UUIDSerializer` | `org.apache.kafka.common.serialization.UUIDDeserializer` |
| `java.lang.Void` | any | `org.apache.kafka.common.serialization.VoidSerializer` | `org.apache.kafka.common.serialization.VoidDeserializer` |
| Custom POJO | `boot3` / `boot3.2` | `org.springframework.kafka.support.serializer.JsonSerializer` | `org.springframework.kafka.support.serializer.JsonDeserializer` |
| Custom POJO | `boot4` | `org.springframework.kafka.support.serializer.JacksonJsonSerializer` | `org.springframework.kafka.support.serializer.JacksonJsonDeserializer` |

`JacksonJson*` classes exist only in `spring-kafka 4.x` (verified in `spring-kafka` repo at tag `v4.0.4`); on Boot 3.x they are not available and `Json*` must be used. The `TRUSTED_PACKAGES` constant resolves to the same property key `spring.json.trusted.packages` in both versions (verified in `JsonDeserializer.java:88` and `JacksonJsonDeserializer.java`).

## Extra JSON consumer properties

`spring.json.trusted.packages` is required when `consumerKeyType` OR `consumerValueType` is a Custom POJO. Without it, `JsonDeserializer` / `JacksonJsonDeserializer` rejects incoming records with `IllegalArgumentException: The class is not in the trusted packages`.

Value = comma-separated packages of the POJO types on the consumer side. Examples:

- `consumerValueType = com.example.orders.OrderEvent`, `consumerKeyType = String` → `spring.json.trusted.packages=com.example.orders`
- `consumerKeyType = com.example.ids.OrderId`, `consumerValueType = com.example.orders.OrderEvent` → `spring.json.trusted.packages=com.example.ids,com.example.orders`
- `consumerKeyType = String`, `consumerValueType = String` → no `trusted.packages` line at all

The producer-side types do not contribute to `trusted.packages` because `JsonSerializer` does not validate types on the way out.

`spring.json.key.default.type` is required when `consumerKeyType` is a Custom POJO. Value = the FQN of `consumerKeyType`.

`spring.json.value.default.type` is required when `consumerValueType` is a Custom POJO. Value = the FQN of `consumerValueType`.

These default type properties make POJO deserialization work even when incoming records do not include Spring JSON type headers. Do not add `key.default.type` for non-POJO keys, and do not add `value.default.type` for non-POJO values.

`.properties` syntax:

```
spring.kafka.consumer.properties[spring.json.trusted.packages]={packages}
spring.kafka.consumer.properties[spring.json.key.default.type]={consumerKeyTypeFqn}
spring.kafka.consumer.properties[spring.json.value.default.type]={consumerValueTypeFqn}
```

`.yml` syntax:

```yaml
spring:
  kafka:
    consumer:
      properties:
        "[spring.json.trusted.packages]": {packages}
        "[spring.json.key.default.type]": {consumerKeyTypeFqn}
        "[spring.json.value.default.type]": {consumerValueTypeFqn}
```

Only emit the default type line for the POJO side it applies to. The bracketed key form is required because the property names contain dots. Both `.properties` and `.yml` flavours work in any Boot 3.x and 4.x version — verified against Spring Boot 4 reference docs (`messaging/kafka.adoc`).

## Property block template — Path A (`serializerSource = properties`)

Final block to append (or overwrite per-key) into the chosen property source. Substitute `{...}` placeholders from the lookups above and the user's answers in Step 3.

`.properties`:

```
spring.kafka.bootstrap-servers={bootstrapServers}
spring.kafka.consumer.group-id={consumerGroup}
spring.kafka.producer.key-serializer={producerKeySerializer}
spring.kafka.producer.value-serializer={producerValueSerializer}
spring.kafka.consumer.key-deserializer={consumerKeyDeserializer}
spring.kafka.consumer.value-deserializer={consumerValueDeserializer}
```

If `consumerKeyType` OR `consumerValueType` is a Custom POJO append:

```
spring.kafka.consumer.properties[spring.json.trusted.packages]={packages}
```

If `consumerKeyType` is a Custom POJO append:

```
spring.kafka.consumer.properties[spring.json.key.default.type]={consumerKeyTypeFqn}
```

If `consumerValueType` is a Custom POJO append:

```
spring.kafka.consumer.properties[spring.json.value.default.type]={consumerValueTypeFqn}
```

`.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: {bootstrapServers}
    producer:
      key-serializer: {producerKeySerializer}
      value-serializer: {producerValueSerializer}
    consumer:
      group-id: {consumerGroup}
      key-deserializer: {consumerKeyDeserializer}
      value-deserializer: {consumerValueDeserializer}
```

If `consumerKeyType` OR `consumerValueType` is a Custom POJO add under `consumer:`:

```yaml
      properties:
        "[spring.json.trusted.packages]": {packages}
```

If `consumerKeyType` is a Custom POJO add under `consumer.properties`:

```yaml
        "[spring.json.key.default.type]": {consumerKeyTypeFqn}
```

If `consumerValueType` is a Custom POJO add under `consumer.properties`:

```yaml
        "[spring.json.value.default.type]": {consumerValueTypeFqn}
```

## Property block template — Path B (`serializerSource = @Configuration`)

Only two keys go into the property source. The four serializer FQNs are written inside the generated class (see `configuration-class/{language}-{bootBranch}.md`).

`.properties`:

```
spring.kafka.bootstrap-servers={bootstrapServers}
spring.kafka.consumer.group-id={consumerGroup}
```

`.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: {bootstrapServers}
    consumer:
      group-id: {consumerGroup}
```

`spring.json.trusted.packages` for Path B is set in code via `props.put(JsonDeserializer.TRUSTED_PACKAGES, "{packages}")` (or `JacksonJsonDeserializer.TRUSTED_PACKAGES` on `boot4`) inside `{prefix}ConsumerFactory` — see the variant template.

`spring.json.key.default.type` and `spring.json.value.default.type` for Path B are set in code via `JsonDeserializer.KEY_DEFAULT_TYPE` / `VALUE_DEFAULT_TYPE` (or `JacksonJsonDeserializer.KEY_DEFAULT_TYPE` / `VALUE_DEFAULT_TYPE` on `boot4`) for the POJO consumer side(s).

## Behaviour on existing keys

If a key is already present in the target file, overwrite its value. Do not append a duplicate key. Other unrelated keys must be left untouched.
