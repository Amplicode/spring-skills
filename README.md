# Amplicode Spring Skills

Этот репозиторий входит в состав **Spring Agent Toolkit** — набора инструментов Amplicode для AI-кодинга на Spring.

## Установка

Самый простой способ установить Spring Skills — через CLI `npx skills`.

Установить все skills глобально во все обнаруженные AI-агенты:

```bash
npx skills add Amplicode/spring-skills -g
```

Установить skills только для конкретных агентов, например Claude Code, Codex и Gemini CLI:

```bash
npx skills add Amplicode/spring-skills -g -a claude-code -a codex -a gemini-cli
```

Полезные команды:

```bash
# посмотреть список skills в репозитории, не устанавливая
npx skills add Amplicode/spring-skills --list

# обновить установленные skills до последних версий
npx skills update
```

Без флага `-g` skills устанавливаются в текущий проект, с флагом `-g` — в домашние каталоги агентов.

Полная инструкция: [Spring Agent Toolkit — подключение к AI-агентам](https://amplicode.ru/documentation/spring-agent/)

## Skills

| Skill | Description | Status |
|-------|-------------|--------|
| [`spring-explore`](skills/spring-explore/SKILL.md) | Automatically explores a Spring Boot application and builds project context: tech stack, module structure, domain entities, REST endpoints | Available |
| [`spring-data-jpa`](skills/spring-data-jpa/SKILL.md) | Rules and guidelines for working with Spring Data JPA — creating/modifying entities, repositories, projections, and transactional code | Available |
| [`spring-data-jdbc`](skills/spring-data-jdbc/SKILL.md) | Rules and guidelines for working with Spring Data JDBC — creating/modifying entities, aggregates, `AggregateReference` links, `@MappedCollection` associations, and JDBC repositories | Available |
| [`connekt-script-writer`](skills/connekt-script-writer/SKILL.md) | Writing `.connekt.kts` scripts — Kotlin-based HTTP automation and testing using the Connekt DSL | Available |
| [`spring-planning`](skills/spring-planning/SKILL.md) | Creates a structured implementation plan in `docs/plans/` with interactive context gathering, approach selection, and task breakdown | Available |
| [`crud-rest-controller`](skills/crud-rest-controller/SKILL.md) | Creates a Spring REST controller with CRUD endpoints backed by a Spring Data repository | In development |
| [`dto-creator`](skills/dto-creator/SKILL.md) | Creates a DTO (Data Transfer Object) class for an entity (Java class, record, Kotlin data class, with Lombok support) | In development |
| [`mapper-creator`](skills/mapper-creator/SKILL.md) | Creates a mapper between an entity and a DTO (MapStruct or custom converter) | In development |
| [`spring-security-configuration`](skills/spring-security-configuration/SKILL.md) | Creates a Spring Security configuration class with authentication, authorization, and HTTP protection setup | In development |
| [`kafka-configuration`](skills/kafka-configuration/SKILL.md) | Configures Spring Boot's Kafka | Available |
| [`java-debug`](skills/java-debug/SKILL.md) | Debugging applications via IntelliJ debugger: breakpoints, debug sessions, stepping, evaluating expressions, inspecting runtime state | In development |
