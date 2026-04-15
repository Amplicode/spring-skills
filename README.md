# Amplicode Spring Skills

Best skills for Spring Boot development powered by Amplicode Team.

## Installation

```shell
curl -sSL https://raw.githubusercontent.com/Amplicode/spring-skills/refs/heads/main/install.sh | bash
```

### Prerequisites

[Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI must be installed and running.

### Manual installation

```shell
claude plugin marketplace add https://github.com/Amplicode/spring-skills.git
claude plugin install spring-tools@spring-tools
claude plugin update spring-tools@spring-tools
```

## Skills

| Skill | Description | Status |
|-------|-------------|--------|
| `spring-explore` | Automatically explores a Spring Boot application and builds project context: tech stack, module structure, domain entities, REST endpoints | Available |
| `spring-data-jpa` | Rules and guidelines for working with Spring Data JPA — creating/modifying entities, repositories, projections, and transactional code | Available |
| `connekt-script-writer` | Writing `.connekt.kts` scripts — Kotlin-based HTTP automation and testing using the Connekt DSL | Available |
| `crud-rest-controller` | Creates a Spring REST controller with CRUD endpoints backed by a Spring Data repository | In development |
| `dto-creator` | Creates a DTO (Data Transfer Object) class for an entity (Java class, record, Kotlin data class, with Lombok support) | In development |
| `mapper-creator` | Creates a mapper between an entity and a DTO (MapStruct or custom converter) | In development |
| `spring-security-configuration` | Creates a Spring Security configuration class with authentication, authorization, and HTTP protection setup | In development |
| `java-debug` | Debugging applications via IntelliJ debugger: breakpoints, debug sessions, stepping, evaluating expressions, inspecting runtime state | In development |
