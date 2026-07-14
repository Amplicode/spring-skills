# Amplicode Spring Skills

**Spring Skills** — открытый набор skills для **Spring Agent Toolkit**: проверенные инструкции, workflow и примеры, которые помогают AI-агентам предсказуемо работать со Spring Boot-проектами.

Репозиторий поддерживается командами Amplicode и Spring АйО и сфокусирован на практических задачах Spring-разработки: исследование существующего приложения, изменение Spring Data-модели, создание DTO и мапперов, добавление REST-контроллеров, настройка Spring Security, HTTP-автоматизация и отладка через IntelliJ IDEA.

## Зачем Это Нужно

Обычные AI-агенты неплохо пишут Spring-подобный код, но часто опираются на стиль "среднего проекта с GitHub". В реальных проектах всё иначе: есть локальные соглашения по JPA-маппингам, репозиториям, DTO, MapStruct, REST-неймингу, транзакциям, безопасности, тестам и миграциям.

Spring Skills дают агенту более узкую и Spring-aware модель работы:

- сначала изучать текущее приложение, а уже потом менять код;
- следовать соглашениям проекта, а не придумывать новые;
- использовать структурированный Spring-контекст из Spring MCP, когда он доступен;
- разбивать типовые Spring-задачи на сфокусированные workflow;
- работать несколькими skills вместе без конфликтующих инструкций.

Подробнее про идею и состав Toolkit: [Spring Agent Toolkit: ультимативный набор для вашего AI-агента](https://habr.com/ru/companies/haulmont/articles/1034688/).

## Быстрый Старт

Установить все skills глобально во все поддерживаемые AI-агенты:

```bash
npx skills add Amplicode/spring-skills -g
```

После этого откройте Spring Boot-проект в агенте и дайте ему конкретную Spring-задачу, например:

```text
Explore this project and explain its domain model.
```

```text
Add a CRUD REST controller for Customer using DTOs and MapStruct.
```

```text
Create a Connekt script that tests the visit creation API.
```

Для полного сценария Spring Agent Toolkit установите плагин Amplicode в IntelliJ IDEA или OpenIDE, откройте проект и нажмите **"Настроить Spring Agent"** на welcome screen Amplicode. Это подключит MCP-инструменты, через которые skills смогут получать Spring-структуру проекта напрямую из IDE.

Полная инструкция: [Spring Agent Toolkit — подключение к AI-агентам](https://amplicode.ru/documentation/spring-agent/)

## Что Внутри

| Skill | Что помогает делать агенту | Статус |
|-------|-----------------------------|--------|
| [`spring-explore`](skills/spring-explore/SKILL.md) | Исследовать Spring Boot-приложение и собрать контекст проекта: стек, модули, доменные сущности, репозитории, сервисы и REST-эндпоинты. | Готов |
| [`spring-planning`](skills/spring-planning/SKILL.md) | Создать структурированный план реализации в `docs/plans/`: сбор контекста, выбор подхода и декомпозиция задач. | Готов |
| [`spring-data-jpa`](skills/spring-data-jpa/SKILL.md) | Работать с JPA-сущностями, репозиториями, проекциями и транзакционным кодом с учётом соглашений проекта. | Готов |
| [`spring-data-jdbc`](skills/spring-data-jdbc/SKILL.md) | Работать с Spring Data JDBC-агрегатами, `AggregateReference`, `@MappedCollection`, embedded-объектами и JDBC-репозиториями. | Готов |
| [`crud-rest-controller`](skills/crud-rest-controller/SKILL.md) | Создавать Spring REST-контроллеры с CRUD-эндпоинтами на базе Spring Data repository, опционально с DTO, маппингом и пагинацией. | В разработке |
| [`dto-creator`](skills/dto-creator/SKILL.md) | Создавать DTO для сущностей: Java class, Java record, Java + Lombok или Kotlin data class. | В разработке |
| [`mapper-creator`](skills/mapper-creator/SKILL.md) | Создавать мапперы между entity и DTO через MapStruct или custom converter. | В разработке |
| [`spring-security-configuration`](skills/spring-security-configuration/SKILL.md) | Генерировать Spring Security-конфигурацию для authentication, authorization, HTTP protection и вспомогательных beans/properties. | В разработке |
| [`kafka-configuration`](skills/kafka-configuration/SKILL.md) | Настраивать Spring Boot Kafka starter через `application.properties` / `application.yml` и, при необходимости, генерировать `KafkaConfiguration`. | Готов |
| [`connekt-script-writer`](skills/connekt-script-writer/SKILL.md) | Писать `.connekt.kts` scripts для Kotlin-based HTTP automation и тестирования эндпоинтов. | Готов |
| [`codefmt`](skills/codefmt/SKILL.md) | Форматировать исходный код через IntelliJ IDEA/OpenIDE с использованием code style проекта и оптимизировать imports в изменённых файлах. | Готов |
| [`run-tests`](skills/run-tests/SKILL.md) | Запускать тесты Gradle-проекта по типу (unit, integration, all) или модулю — через IDE или консольный fallback — и возвращать компактный отчёт pass/fail. | Готов |
| [`coverage`](skills/coverage/SKILL.md) | Измерять покрытие кода проекта — целиком (unit + integration вместе) либо только по одной тестовой группе: package/class glob или подпроект — и возвращать число покрытия. | Готов |
| [`java-debug`](skills/java-debug/SKILL.md) | Отлаживать Java-приложения через IntelliJ Debug MCP: breakpoints, debug sessions, stepping, evaluate expression и stack inspection. | В разработке |
| [`amplicode-install`](skills/amplicode-install/SKILL.md) | Установить Amplicode IntelliJ plugin в поддерживаемые IDE и провести пользователя через настройку Spring Agent. | Готов |

## Как Это Работает

Spring Agent Toolkit объединяет три слоя:

1. **Skills** из этого репозитория объясняют агенту, как выполнять повторяющиеся Spring-задачи.
2. **Spring MCP** отдаёт структурированную информацию из IDE: сущности, репозитории, зависимости beans, эндпоинты, зависимости модулей, миграции и исходные файлы.
3. **Интеграции с агентами** устанавливают одни и те же skills в Codex, Claude Code, OpenCode, Gemini CLI, Qwen Code, Kilo Code, Veai и GitHub Copilot CLI там, где это поддерживается.

Большинство Spring skills начинают с preflight-проверки. Если Spring MCP подключён, агент использует его для точного анализа приложения. Если MCP недоступен, skill либо помогает настроить Amplicode, либо переходит к прямому чтению файлов, когда такой fallback безопасен для конкретного workflow.

## Установка

### Рекомендуемый способ: `npx skills`

Установить глобально во все обнаруженные поддерживаемые агенты:

```bash
npx skills add Amplicode/spring-skills -g
```

Установить только для выбранных агентов:

```bash
npx skills add Amplicode/spring-skills -g -a claude-code -a codex -a gemini-cli
```

Посмотреть список skills без установки:

```bash
npx skills add Amplicode/spring-skills --list
```

Обновить установленные skills:

```bash
npx skills update
```

Без флага `-g` skills устанавливаются в текущий проект. С флагом `-g` — в домашние каталоги поддерживаемых агентов.

### Альтернатива: installer из репозитория

macOS/Linux:

```bash
curl -fsSL https://raw.githubusercontent.com/Amplicode/spring-skills/main/install.sh | bash
```

Windows PowerShell:

```powershell
irm https://raw.githubusercontent.com/Amplicode/spring-skills/main/install.ps1 | iex
```

Installer клонирует репозиторий в `~/.agents/.amplicode/spring-skills`, создаёт ссылки на skills для поддерживаемых агентов и устанавливает marketplace plugins для Claude Code и GitHub Copilot CLI, если их CLI доступны в системе.

## Требования

- AI coding agent с поддержкой skills/plugins.
- Git и Node.js/npm для установки через `npx skills`.
- OpenIDE, IntelliJ IDEA или GigaIDE с плагином Amplicode для Spring-анализа через MCP.
- Spring Boot-проект для использования Spring-specific skills.

## Пример Workflow

Попросите агента реализовать Spring-фичу:

```text
Add an endpoint that returns all pets for an owner by owner id.
```

С установленными Spring Skills агент может провести задачу через сфокусированные шаги:

1. `spring-explore` проверит структуру проекта, сущности, репозитории, сервисы и существующие эндпоинты.
2. `spring-data-jpa` или `spring-data-jdbc` применит правила persistence-стека проекта.
3. `dto-creator` и `mapper-creator` создадут response-типы и conversion-код, если они нужны.
4. `crud-rest-controller` добавит или расширит REST-слой.
5. `connekt-script-writer` сможет создать HTTP-скрипт для проверки эндпоинта.

Главное здесь не просто генерация кода. Skills помогают агенту задавать недостающие вопросы, переиспользовать локальные соглашения и не смешивать несовместимые Spring-подходы.

## Структура Репозитория

```text
.
├── skills/                    # Skill definitions, references и examples
├── install.sh                 # macOS/Linux installer
├── install.ps1                # Windows installer
├── .codex-plugin/             # Codex plugin manifest
├── .claude-plugin/            # Claude Code plugin manifest
└── .github/plugin/            # GitHub Copilot plugin manifest
```

Каждый skill живёт в отдельной директории и начинается с `SKILL.md`. Многие skills дополнительно содержат `references/` и `examples/`, чтобы агент использовал проверенные паттерны вместо свободной импровизации большими фрагментами кода.

## Статус Проекта

Репозиторий активно развивается. Skills со статусом **Готов** уже подходят для регулярного использования в своих основных сценариях. Skills со статусом **В разработке** имеют рабочий workflow, но ещё расширяются, настраиваются и тестируются на большем количестве Spring-проектов и agent runtimes.

Особенно полезен feedback с реальных проектов: недостающие соглашения, неудобные вопросы, неподдержанные Spring-паттерны и ситуации, где несколько skills работают вместе неидеально.

## Contributing

Contributions are welcome. Особенно полезны:

- новые Spring skills;
- дополнительные examples для Java и Kotlin-проектов;
- улучшенные fallbacks для runtimes без MCP;
- bug reports с конкретным проектным контекстом;
- улучшения install scripts и agent manifests;
- исправления документации.

При изменении skill держите его сфокусированным на одной задаче и явно описывайте trigger conditions. Skills в этом репозитории должны работать вместе, поэтому избегайте широких инструкций, которые могут конфликтовать с соседними Spring workflow.

## Ссылки

- [Spring Agent Toolkit documentation](https://amplicode.ru/documentation/spring-agent/)
- [Habr article: Spring Agent Toolkit](https://habr.com/ru/companies/haulmont/articles/1034688/)
- [Amplicode](https://amplicode.ru/)
- [Haulmont](https://www.haulmont.ru/)
