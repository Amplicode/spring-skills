# Amplicode Spring Skills

**Spring Skills** is an open set of skills for the **Spring Agent Toolkit**: proven instructions, workflows, and examples that help AI agents work with Spring Boot projects predictably.

The repository is maintained by the Amplicode and Spring AIO teams and focuses on practical Spring development tasks: exploring an existing application, modifying the Spring Data model, creating DTOs and mappers, adding REST controllers, configuring Spring Security, HTTP automation, and debugging via IntelliJ IDEA.

## Why It Matters

Typical AI agents write Spring-like code reasonably well, but they often rely on the style of an "average GitHub project". Real projects are different: they have local conventions for JPA mappings, repositories, DTOs, MapStruct, REST naming, transactions, security, tests, and migrations.

Spring Skills give the agent a narrower, Spring-aware working model:

- explore the current application first, and only then change code;
- follow the project's conventions instead of inventing new ones;
- use structured Spring context from Spring MCP when it is available;
- break typical Spring tasks into focused workflows;
- run multiple skills together without conflicting instructions.

More about the idea and the Toolkit's composition: [Spring Agent Toolkit: the ultimate set for your AI agent](https://habr.com/ru/companies/haulmont/articles/1034688/).

## Quick Start

Install all skills globally into all supported AI agents:

```bash
npx skills add Amplicode/spring-skills -g
```

After that, open a Spring Boot project in your agent and give it a concrete Spring task, for example:

```text
Explore this project and explain its domain model.
```

```text
Add a CRUD REST controller for Customer using DTOs and MapStruct.
```

```text
Create a Connekt script that tests the visit creation API.
```

For the full Spring Agent Toolkit experience, install the Amplicode plugin in IntelliJ IDEA or OpenIDE, open your project, and click **"Set Up Spring Agent"** on the Amplicode welcome screen. This connects the MCP tools that allow skills to get the Spring structure of the project directly from the IDE.

Full instructions: [Spring Agent Toolkit — connecting to AI agents](https://amplicode.ru/documentation/spring-agent/)

## What's Inside

| Skill | What it helps the agent do | Status |
|-------|-----------------------------|--------|
| [`spring-explore`](skills/spring-explore/SKILL.md) | Explore a Spring Boot application and gather project context: stack, modules, domain entities, repositories, services, and REST endpoints. | Ready |
| [`spring-planning`](skills/spring-planning/SKILL.md) | Create a structured implementation plan in `docs/plans/`: context gathering, approach selection, and task decomposition. | Ready |
| [`spring-data-jpa`](skills/spring-data-jpa/SKILL.md) | Work with JPA entities, repositories, projections, and transactional code following the project's conventions. | Ready |
| [`spring-data-jdbc`](skills/spring-data-jdbc/SKILL.md) | Work with Spring Data JDBC aggregates, `AggregateReference`, `@MappedCollection`, embedded objects, and JDBC repositories. | Ready |
| [`crud-rest-controller`](skills/crud-rest-controller/SKILL.md) | Create Spring REST controllers with CRUD endpoints based on a Spring Data repository, optionally with DTOs, mapping, and pagination. | In progress |
| [`dto-creator`](skills/dto-creator/SKILL.md) | Create DTOs for entities: Java class, Java record, Java + Lombok, or Kotlin data class. | In progress |
| [`mapper-creator`](skills/mapper-creator/SKILL.md) | Create mappers between entities and DTOs via MapStruct or a custom converter. | In progress |
| [`spring-security-configuration`](skills/spring-security-configuration/SKILL.md) | Generate Spring Security configuration for authentication, authorization, HTTP protection, and supporting beans/properties. | In progress |
| [`kafka-configuration`](skills/kafka-configuration/SKILL.md) | Configure the Spring Boot Kafka starter via `application.properties` / `application.yml` and, when needed, generate a `KafkaConfiguration`. | Ready |
| [`connekt-script-writer`](skills/connekt-script-writer/SKILL.md) | Write `.connekt.kts` scripts for Kotlin-based HTTP automation and endpoint testing. | Ready |
| [`codefmt`](skills/codefmt/SKILL.md) | Reformat source code through IntelliJ IDEA/OpenIDE using the project's own code style settings and optimize imports for changed files. | Ready |
| [`run-tests`](skills/run-tests/SKILL.md) | Run a Gradle project's tests by type (unit, integration, all) or module, through the IDE or a console fallback, and report a compact pass/fail result. | Ready |
| [`coverage`](skills/coverage/SKILL.md) | Measure a project's code coverage — the whole project (unit + integration merged) or just one test group, a package/class glob or a subproject — and report the coverage number. | Ready |
| [`java-debug`](skills/java-debug/SKILL.md) | Debug Java applications via IntelliJ Debug MCP: breakpoints, debug sessions, stepping, evaluate expression, and stack inspection. | In progress |
| [`amplicode-install`](skills/amplicode-install/SKILL.md) | Install the Amplicode IntelliJ plugin into supported IDEs and guide the user through Spring Agent setup. | Ready |

## How It Works

The Spring Agent Toolkit combines three layers:

1. **Skills** from this repository explain to the agent how to perform recurring Spring tasks.
2. **Spring MCP** provides structured information from the IDE: entities, repositories, bean dependencies, endpoints, module dependencies, migrations, and source files.
3. **Agent integrations** install the same skills into Codex, Claude Code, OpenCode, Gemini CLI, Qwen Code, Kilo Code, Veai, and GitHub Copilot CLI where supported.

Most Spring skills start with a preflight check. If Spring MCP is connected, the agent uses it for precise application analysis. If MCP is unavailable, the skill either helps set up Amplicode or falls back to reading files directly when such a fallback is safe for the specific workflow.

## Installation

### Recommended way: `npx skills`

Install globally into all detected supported agents:

```bash
npx skills add Amplicode/spring-skills -g
```

Install only for selected agents:

```bash
npx skills add Amplicode/spring-skills -g -a claude-code -a codex -a gemini-cli
```

List the skills without installing:

```bash
npx skills add Amplicode/spring-skills --list
```

Update installed skills:

```bash
npx skills update
```

Without the `-g` flag, skills are installed into the current project. With `-g`, they go into the home directories of supported agents.

### Alternative: installer from the repository

macOS/Linux:

```bash
curl -fsSL https://raw.githubusercontent.com/Amplicode/spring-skills/main/install.sh | bash
```

Windows PowerShell:

```powershell
irm https://raw.githubusercontent.com/Amplicode/spring-skills/main/install.ps1 | iex
```

The installer clones the repository into `~/.agents/.amplicode/spring-skills`, creates skill links for supported agents, and installs marketplace plugins for Claude Code and GitHub Copilot CLI if their CLIs are available on the system.

## Requirements

- An AI coding agent with skills/plugins support.
- Git and Node.js/npm for installation via `npx skills`.
- OpenIDE, IntelliJ IDEA, or GigaIDE with the Amplicode plugin for Spring analysis via MCP.
- A Spring Boot project to use the Spring-specific skills.

## Example Workflow

Ask the agent to implement a Spring feature:

```text
Add an endpoint that returns all pets for an owner by owner id.
```

With Spring Skills installed, the agent can take the task through focused steps:

1. `spring-explore` checks the project structure, entities, repositories, services, and existing endpoints.
2. `spring-data-jpa` or `spring-data-jdbc` applies the rules of the project's persistence stack.
3. `dto-creator` and `mapper-creator` create response types and conversion code if they are needed.
4. `crud-rest-controller` adds or extends the REST layer.
5. `connekt-script-writer` can create an HTTP script to verify the endpoint.

The key point here is not just code generation. Skills help the agent ask the missing questions, reuse local conventions, and avoid mixing incompatible Spring approaches.

## Repository Structure

```text
.
├── skills/                    # Skill definitions, references, and examples
├── install.sh                 # macOS/Linux installer
├── install.ps1                # Windows installer
├── .codex-plugin/             # Codex plugin manifest
├── .claude-plugin/            # Claude Code plugin manifest
└── .github/plugin/            # GitHub Copilot plugin manifest
```

Each skill lives in its own directory and starts with `SKILL.md`. Many skills also include `references/` and `examples/` so the agent uses proven patterns instead of freely improvising large chunks of code.

## Project Status

The repository is under active development. Skills with the **Ready** status are already suitable for regular use in their primary scenarios. Skills with the **In progress** status have a working workflow but are still being extended, tuned, and tested on more Spring projects and agent runtimes.

Feedback from real projects is especially valuable: missing conventions, awkward questions, unsupported Spring patterns, and situations where several skills don't work together perfectly.

## Contributing

Contributions are welcome. Especially useful are:

- new Spring skills;
- additional examples for Java and Kotlin projects;
- improved fallbacks for runtimes without MCP;
- bug reports with specific project context;
- improvements to install scripts and agent manifests;
- documentation fixes.

When changing a skill, keep it focused on a single task and describe its trigger conditions explicitly. Skills in this repository must work together, so avoid broad instructions that could conflict with neighboring Spring workflows.

## Links

- [Spring Agent Toolkit documentation](https://amplicode.ru/documentation/spring-agent/)
- [Habr article: Spring Agent Toolkit](https://habr.com/ru/companies/haulmont/articles/1034688/)
- [Amplicode](https://amplicode.ru/)
- [Haulmont](https://www.haulmont.ru/)
