---
name: kafka-configuration
description: >
  Configures Spring Boot's Kafka starter through `application.properties` / `application.yml`.
  Use this skill when Kafka configuration needs to be created, either standalone or as part of a larger task (e.g. before adding a `KafkaTemplate` producer or a `@KafkaListener` consumer).
---

# Preflight: Spring MCP

This skill is part of the **Spring Agent Toolkit** and is designed to work with the **Spring MCP server** (provided by the Amplicode IntelliJ plugin). Before doing anything else, check your tool list for any Spring MCP tool — they are exposed under the `amplicode` MCP server (e.g. `get_project_summary`, `list_module_dependencies`, `list_application_properties_files`); harnesses that flatten MCP tools into the tool list use the `mcp__amplicode__` prefix on the same names.

- **If at least one Amplicode tool is available** — MCP is connected. Proceed with the skill below.
- **If none are available** — stop and invoke the **`amplicode-install`** skill (bundled with the Spring Agent Toolkit). It installs the Amplicode plugin and walks the user through the **«Настроить Spring Agent»** welcome-screen button + MCP-client restart. After it completes, the MCP tools become available — resume this skill.
- If `amplicode-install` is not registered in your skill list, tell the user (in their language): *"This skill needs the Amplicode IntelliJ plugin and its MCP server. Install it from https://amplicode.ru/marketplace into IntelliJ IDEA Ultimate/Community or GigaIDE, open any project, click «Настроить Spring Agent» on the Amplicode welcome screen, then restart your MCP client."*

---

# Kafka Configuration

Wires Spring Boot's Kafka starter through `application.properties` / `application.yml` and, optionally, a generated `KafkaConfiguration` class. Ensures the starter dependency is on the module classpath.

> **CRITICAL: Code ONLY from `examples/` files. If no matching example — STOP and ask user.**
> **CRITICAL: For questions with a fixed set of choices, prefer `AskUserQuestion` > its analogue > plain text list.**
> **CRITICAL: Read the conversation context BEFORE running Step 1.** Half the questions in Steps 2–3 may already be answered.

## Two paths (Step 3 picks)

- **Path A — `serializerSource = properties`** (default). All six `spring.kafka.*` keys go into the property source: `bootstrap-servers`, `consumer.group-id`, producer key/value serializer, consumer key/value deserializer. `KafkaAutoConfiguration` creates `ProducerFactory`, `ConsumerFactory`, `KafkaTemplate` (bean `kafkaTemplate`), and `kafkaListenerContainerFactory` from them.
- **Path B — `serializerSource = @Configuration`**. Only `bootstrap-servers` + `consumer.group-id` go into the property source. A `KafkaConfiguration` class is generated with four beans: `{prefix}ProducerFactory`, `{prefix}KafkaTemplate`, `{prefix}ConsumerFactory`, `kafkaListenerContainerFactory`. `{prefix}` is the lowercased simple name of `producerValueType`. The listener factory bean name is **fixed** — autoconfig backs off via `@ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")`, any other name leaves the autoconfig factory in place and silently routes `@KafkaListener` to the wrong one.

## Defaults

| Option | Default | Always ask? |
|---|---|---|
| `producerKeyType` / `producerValueType` / `consumerKeyType` / `consumerValueType` | `java.lang.String` | YES — always ask producer key + value; consumer defaults to symmetric | |
| `consumerGroup` | — | YES (mandatory) |
| `bootstrapServers` | `localhost:9092` | NO |
| `serializerSource` | `properties` | YES |
| `className` | `KafkaConfiguration` | NO (only for Path B) |
| `packageName` | `mainPackage` | NO (only for Path B) |
| `language` / `bootVersion` | from `get_project_summary` | NO |

**Smart defaults.** If the user says "use defaults" / "all defaults" / "minimal configuration" → skip every `Always ask = NO` question. Only ask the mandatory ones (`consumerGroup`, `serializerSource`, and any type the user already mentioned non-default for).

**Smart answer recognition.** When the user provides a value directly ("for `OrderEvent` messages", "group `orders`"), accept it without asking again. Multiple answers in one message → accept all.

**Batch questions.** Group related questions into one `AskUserQuestion` call (up to 4 questions). Recommended option first, `(Recommended)` in its label.

## Decision-making — context first, then ask

For every input: try to derive from `get_project_summary`, `list_module_dependencies`, `list_application_properties_files`, prior turns, the user's prompt. Only ask when context yields no clear default.

1. **Context unambiguous → decide silently.** `language`, `bootVersion`, `mainPackage`, `bootstrapServers = localhost:9092`, single-file `propsFile`.
2. **Strong signal → one-line confirmation.** State decision + alternatives; user can accept silently.
3. **No clear default → `AskUserQuestion`** with recommended option first.
4. **Empty for a critical input → ask plainly.** `consumerGroup`, `serializerSource`, custom type FQNs.

## Step 0 — Conversation context (mental, no tool calls)

Re-read the user's prompt and prior turns; tick off everything already stated:

| Input | Signal in the prompt |
|---|---|
| `producerValueType` / `producerKeyType` / `consumerValueType` / `consumerKeyType` | "publish `X`", "send `X`", "key as `Y`", "consume `X`", "`@KafkaListener` receives `X`". Consumer defaults to symmetric with producer. |
| `consumerGroup` | "group `X`", "consumer group id …" |
| `serializerSource` | "use properties", "in a `@Configuration` class", "properties only", "bean classes" |
| `className` / `packageName` | "name it FooConfig", "in package …" |
| `language` | Java / Kotlin / file extensions |
| smart defaults | "use defaults", "all defaults" → all types default to `java.lang.String`, only ask consumerGroup + serializerSource |

Tick → skip the corresponding question. Do not announce Step 0.

## Step 1 — Gather context (parallel MCP calls + file reads)

| Tool | Variable |
|---|---|
| `get_project_summary` | `language`, `bootVersion`, `mainPackage`, `buildFile`, `modules` list |
| `list_module_dependencies(moduleName)` | `presentDeps` |
| `list_application_properties_files(moduleName)` | `propsFiles` |
| `list_spring_beans(moduleName)` | `existingBeans` |

After the calls, `Read` each path in `propsFiles` and parse any existing `spring.kafka.*` keys into `existingProps`.

**Multi-module selection.** If `modules` has more than one entry:
1. Run `list_module_dependencies` + `list_application_properties_files` for each module in parallel.
2. Score every module: +1 if its deps contain `org.springframework.boot:spring-boot-starter-kafka`; +1 if its property files contain any `spring.kafka.*` key.
3. Exactly one module with score ≥ 1 → select it silently as `moduleName`.
4. Two or more, or all-zero → ask the user which module. Then re-run the module-scoped tools for the chosen one.

**Derived:**
- `bootBranch` — `boot3` (Boot 3.0/3.1), `boot3.2` (3.2/3.3/3.4/3.5), `boot4` (≥4.0). Used in Step 5 + Step 6.
- `kafkaAlreadyPresent` — `presentDeps` contains `org.springframework.boot:spring-boot-starter-kafka`. Skip Step 5b if true.
- `singlePropsFile` — `len(propsFiles) == 1`. Skip the props-file question in Step 3 if true.
- `existingBootstrapServers` — value of `spring.kafka.bootstrap-servers` from `existingProps` if present, else `null`. Used silently in Step 2.
- `existingConsumerGroup` — value of `spring.kafka.consumer.group-id` from `existingProps` if present, else `null`.
- `existingKafkaConfig` — entries in `existingBeans` belonging to `@Configuration` classes that declare any of `KafkaTemplate`, `ProducerFactory`, `ConsumerFactory`, `*ListenerContainerFactory` beans. Each entry carries the class FQN, source file path, and the bean names already declared. Used in Step 4.

## Step 2 — All questions in ONE batch

Ask everything in a single `AskUserQuestion` call (up to 5 questions). Pre-fill defaults from context, skip already-answered:

1. `Producer key type?` — options: `java.lang.String` (Recommended), `java.lang.Integer`, `java.lang.Long`, `java.util.UUID`, `java.lang.Void`, `Custom (specify FQN)`
2. `Producer value type?` — same options
3. `Consumer group id?` — plain text, mandatory. If `existingConsumerGroup` is non-null → pre-fill silently.
4. `Bootstrap servers?` — default `localhost:9092`. Если `existingBootstrapServers` is non-null → pre-fill silently.
5. `Where to describe serializers?` — `application.properties (Recommended)` (Path A) / `@Configuration class` (Path B)

Consumer types default to producer types (symmetric). Only ask separately if user indicated asymmetric.

If the user says "use defaults" / "all defaults" — skip type questions, default all four to `java.lang.String`. Only ask `consumerGroup`, `bootstrapServers`, `serializerSource`.

## Step 3 — Class target (Path B only)

If `existingKafkaConfig` is non-empty, apply Decision principle 2 (one-line confirmation), naming the existing class:

> Project already has Kafka beans in `{class.fqn}`. Add new beans there? (Yes/No)

- Yes → `classTarget = existing`. Reuse the existing class's `packageName`, `className`, and source file path. Step 5 then uses `Edit` to append beans, not `Write` to create a new file. Inserted bean names that collide with existing ones get a numeric suffix per Step 5 rules.
- No (or `existingKafkaConfig` was empty) → `classTarget = new`:
  - `className` — default `KafkaConfiguration`; on file collision in `{packageName}/{className}.{java|kt}` append numeric suffix.
  - `packageName` — default `mainPackage`; ask only if multiple `@Configuration` packages exist with no clear winner.

Usually answered silently from context.

## Step 4 — Write properties + add dependency

### 4a. Properties

Always write:

```
spring.kafka.bootstrap-servers={bootstrapServers}
spring.kafka.consumer.group-id={consumerGroup}
```

If `serializerSource = properties`, also append the four (de)serializer keys (FQNs from `examples/serializer-mapping.md`):

```
spring.kafka.producer.key-serializer={producerKeySerializer}
spring.kafka.producer.value-serializer={producerValueSerializer}
spring.kafka.consumer.key-deserializer={consumerKeyDeserializer}
spring.kafka.consumer.value-deserializer={consumerValueDeserializer}
```

If `consumerKeyType` OR `consumerValueType` is a POJO, also append:

```
spring.kafka.consumer.properties[spring.json.trusted.packages]={packages}
```

`{packages}` = comma-separated packages of the POJO types on the consumer side.

If `consumerKeyType` is a POJO, also append:

```
spring.kafka.consumer.properties[spring.json.key.default.type]={consumerKeyTypeFqn}
```

If `consumerValueType` is a POJO, also append:

```
spring.kafka.consumer.properties[spring.json.value.default.type]={consumerValueTypeFqn}
```

YAML equivalents are in `examples/serializer-mapping.md`. Overwrite existing keys; never delete unrelated ones.

### 4b. Dependency

If `kafkaAlreadyPresent = true`, skip. Otherwise add `org.springframework.boot:spring-boot-starter-kafka` to `buildFile` (same artifact id for Boot 3.x and 4.x), then call `refresh_build_system_model`.

## Step 5 — Generate `@Configuration` (Path B only)

1. Pick `examples/configuration-class/{language}-{bootBranch}.md`.
2. Substitute variables listed in the template.
3. `{prefix}` = `decapitalize({producerValueType} simple name)` — `String` → `string`, `OrderEvent` → `orderEvent`, `UUID` → `uUID` (only first char lowered).
4. Bean name collision rules (check against `existingBeans` from Step 1 and, for `classTarget = existing`, against the target class's declared beans):
   - For `{prefix}ProducerFactory` / `{prefix}KafkaTemplate` / `{prefix}ConsumerFactory` — if a bean with that exact name exists, append numeric suffix (`stringProducerFactory_1`, ...).
   - Listener factory: default name `kafkaListenerContainerFactory`. If it already exists, use `{prefix}KafkaListenerContainerFactory` and warn in Step 6 that `@KafkaListener` for `{producerValueType}` must set `containerFactory = "{prefix}KafkaListenerContainerFactory"` explicitly.
5. Detect indentation:
   - `classTarget = existing` → sample indentation from the target file.
   - `classTarget = new` → `.editorconfig` → sample any existing source file → fallback 4-space.
6. FQN handling: replace every FQN in the body with its short name; emit one `import` line per unique FQN after `package`; skip `java.lang` and same-package classes. Java grouping: third-party block (sorted alphabetically) + blank line + `java.*` block (sorted). Kotlin: `kotlin.*` joins the third-party block.
7. Write step:
   - `classTarget = new` → `Write` the full file at `{module sourceRoot}/{packageName as path}/{className}.{java|kt}`.
   - `classTarget = existing` → `Read` the target file, then `Edit` to append the four `@Bean` methods before the class's closing brace and merge required imports into the existing import block (no duplicates). Do not create a new file.

## Step 6 — Report

Match the user's conversation language. Include:
- Path taken.
- Files written or edited (paths) and the keys/beans added. For Path B note whether a new class was created or beans were appended to an existing one.
- Dependency: added / already present.
- Pre-filled values from existing project state (when applicable): «Reused existing `spring.kafka.bootstrap-servers={existingBootstrapServers}` / `consumer.group-id={existingConsumerGroup}` from `{propsFile}` — overwrote with the new values only if you provided them.»
- How to use: inject `KafkaTemplate<{producerKeyType}, {producerValueType}>` (bean `kafkaTemplate` for Path A, `{prefix}KafkaTemplate` for Path B). If the listener factory bean is named exactly `kafkaListenerContainerFactory`, `@KafkaListener` uses it by default and no explicit `containerFactory` is needed.
- If a listener factory was name-suffixed: warn that `@KafkaListener` for `{producerValueType}` must set `containerFactory = "{prefix}KafkaListenerContainerFactory"` explicitly.

## Anti-hallucination checklist

- [ ] (De)serializer FQNs come from `examples/serializer-mapping.md` rows matching each of the four type parameters — not from memory.
- [ ] `bootstrap-servers` + `consumer.group-id` are written regardless of `serializerSource`.
- [ ] Path A: the four serializer keys ARE in the property source; no `@Configuration` is generated.
- [ ] Path B: the four serializer keys are NOT in the property source; they live inside the generated class.
- [ ] Listener factory bean name is exactly `kafkaListenerContainerFactory` (or `{prefix}KafkaListenerContainerFactory` only when the default was already taken by a previous generation).
- [ ] Listener factory return type is `ConcurrentKafkaListenerContainerFactory<{consumerKeyType}, {consumerValueType}>` — never raw.
- [ ] `KafkaProperties` import matches `bootBranch`: `boot3` / `boot3.2` use `org.springframework.boot.autoconfigure.kafka`; `boot4` uses `org.springframework.boot.kafka.autoconfigure`.
- [ ] `boot3.2` calls `buildProducerProperties(sslBundles.getIfAvailable())` / `buildConsumerProperties(sslBundles.getIfAvailable())`; `boot3` and `boot4` pass no argument.
- [ ] If a POJO consumer type: `spring.json.trusted.packages` (Path A) or `JsonDeserializer.TRUSTED_PACKAGES` / `JacksonJsonDeserializer.TRUSTED_PACKAGES` (Path B) is set to the relevant package(s) — never `*`, never the FQN.
- [ ] If `consumerKeyType` is a POJO: `spring.json.key.default.type` (Path A) or `JsonDeserializer.KEY_DEFAULT_TYPE` / `JacksonJsonDeserializer.KEY_DEFAULT_TYPE` (Path B) is set to the key type FQN.
- [ ] If `consumerValueType` is a POJO: `spring.json.value.default.type` (Path A) or `JsonDeserializer.VALUE_DEFAULT_TYPE` / `JacksonJsonDeserializer.VALUE_DEFAULT_TYPE` (Path B) is set to the value type FQN.
- [ ] Indentation matches detected project style.
