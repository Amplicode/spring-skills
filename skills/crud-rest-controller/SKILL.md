---
name: crud-rest-controller
description: >
  Creates a Spring REST controller with CRUD endpoints backed by a Spring Data repository.
  Use this skill when a CRUD controller needs to be created, either standalone or as part of a larger task.
---

# CRUD REST Controller

Generates a `@RestController` class with standard CRUD endpoints for an entity,
using a Spring Data repository, optional DTO mapping, pagination, filtering, and patch support.

---

> **CRITICAL: Code ONLY from examples/ files. If no matching example -- STOP and ask user.**
> **CRITICAL: For questions with a fixed set of choices, prefer `AskUserQuestion` > its analogue > plain text list. Plain numbered text lists are the last resort when no interactive tool is available.**
> **CRITICAL: Read the conversation context BEFORE running Step 1.** Half the questions in Steps 2–4 may already be answered by the user's prompt and prior turns. Re-asking what was already said is the #1 reason this skill feels slow.

---

## Defaults

The options below are grouped by topic. `language` and `bootVersion` are
auto-detected; all other options are resolved via the Decision-making
principle (derive from context → confirm → ask).

### Block 1 — Entity & repository

| Option | Default | Notes |
|--------|---------|-------|
| entity | -- | which entity to create controller for |
| repository | first existing for entity | which repository to use |

### Block 2 — DTO mode

| Option | Default | Notes |
|--------|---------|-------|
| DTO mode | DTO (recommended) | if no existing DTO for entity, delegate to `dto-creator`; user can opt out to use entity directly |

### Block 3 — Controller naming & paths

| Option | Default | Notes |
|--------|---------|-------|
| controllerName | `{EntityName}Controller` | suggest based on project naming convention |
| controllerPackage | same as existing controllers or mainPackage | auto-detected from project |
| basePath | `/rest` | persistent base path |
| resourcePath | `/{pluralizedEntityName}` | auto from entity name |

### Block 4 — Pagination & filtering

| Option | Default | Notes |
|--------|---------|-------|
| pagination | true | enable pagination for GET_LIST |
| paginationType | PAGE | PAGE or WINDOW (WINDOW requires filter to be selected; avoids count query overhead) |
| filter | None | JPA Specification filter for GET_LIST |

### Auto-detected (no questions)

| Option | Source |
|--------|--------|
| patchStrategy | ObjectMapper |
| language | `get_project_summary` |
| bootVersion | `get_project_summary` |

**Smart defaults:** If user says "use defaults", "all defaults", "default settings",
or similar -- skip ALL questions where "Always ask?" = NO. Only ask mandatory questions.

**Smart answer recognition:** When user provides a value instead of choosing from a numbered
list, accept it directly. Examples:
- Question "Which entity?" --> user answers "Product" --> this IS the entity, don't re-ask
- Question "Repository?" --> user answers "ProductRepository" --> this IS the choice
- If user provides multiple answers in one message --> accept all, skip answered questions
- NEVER ask a question that the user already answered (even implicitly)

**Batch questions:** When multiple questions must be asked (i.e. cannot be
resolved by principles 1–2 of the Decision-making principle), group them
into a single `AskUserQuestion` call (up to 4 questions per call) when they:
- Belong to the same logical section
- Don't depend on each other's answers

Rules:
- Maximum **3-4 questions** per `AskUserQuestion` call
- Mark the recommended option with `(Recommended)` and place it first
- Never batch questions from DIFFERENT decision branches
- The primary branching question (entity selection) is always asked ALONE
- Prefer `AskUserQuestion` for choices; fall back to plain text lists only if the tool is unavailable

---

## Decision-making principle — context first, then ask

Before asking the user **any** question, attempt to derive the answer from
the context already gathered: project summary, module dependencies, entity
details, existing files in the package, prior turns of this conversation,
and the user's original prompt. Only ask when the context yields **no
clear default** or when the choice is genuinely user-specific (e.g. which
entity, which repository).

Hierarchy of decisions:

1. **Context is unambiguous → decide silently, do NOT ask.**
   Examples: language and JDK from `get_project_summary`; repository when
   there is exactly one for the entity; controller package from existing
   controllers; basePath from existing endpoints; Jackson version from
   `list_module_dependencies`; pagination type when project already uses
   one consistently.

2. **Context gives a strong signal → state the decision + alternatives in one line, let the user override or stay silent.**
   Format:
   ```
   Will create `ProductController` at `/rest/products` with Page pagination, no DTO.
   Alternatives: use DTO, Window pagination, custom paths. OK?
   ```
   The user can answer "ok" / "yes" / silence → accept; or name an
   alternative → switch. This is **not** the same as the numbered question
   format — it is a single confirmation line.

3. **Context yields no clear default → ask with `AskUserQuestion` (preferred) or its analogue, with the recommended option first.**
   When `AskUserQuestion` is available, use it with the recommended
   option marked `(Recommended)` and placed first. If no interactive
   choice tool is available, fall back to a plain text list.
   **Never** ask iteratively ("which entity?" → user picks → "repository?" → …)
   when one batched call would do.

4. **Context is fully empty for a critical input → ask plainly.**
   This applies to: which entity (when not mentioned), the user's intent
   itself.

### How to ask — prefer `AskUserQuestion`

When a question must be asked, prefer the **`AskUserQuestion`** tool (or
its analogue) over writing a numbered list in the response body. Fall back
to plain text only if no interactive choice tool is available.

Rules for `AskUserQuestion` calls in this skill:

- Each call may contain up to **4 questions** that are independent of each
  other (the tool will render them together). Use this to batch related
  decisions in one round-trip.
- Each question has **2–4 options**. The tool auto-adds an "Other" choice
  for free-form input — never include it manually.
- Mark the recommended option by putting it **first** with `(Recommended)`
  appended to the label.
- `header` is a 12-char chip label (e.g. "Entity", "DTO mode", "Paths").
- Each option has a `description` explaining what the choice means.

When `AskUserQuestion` is **not** the right tool:
- Free-form input where there is no enumerable set of options
  (e.g. arbitrary class name, arbitrary path) — ask in plain text.
- The "single confirmation line" form from principle 2 — that is a plain
  question with an obvious yes/no, not an enumerated choice.

The screen-driven question lists in Steps 2–4 below are a **fallback** for
case 4. They are NOT a script to execute top-to-bottom. If a question's
answer is already determined by principles 1–3, **skip the question**.

---

## Step 0 -- Conversation context first (REQUIRED, no tool calls)

Before any MCP call, before any question, **re-read the user's prompt and
the prior turns of this conversation** and extract whatever is already
stated. This step costs nothing and prevents the most common failure mode
of this skill — asking the user something they already said.

Build a mental checklist of inputs and tick off everything the user has
already provided, explicitly or implicitly:

| Input | Look for in the prompt / context |
|---|---|
| **entity** | a class name (`Product`, `Order`, `Vet`); "for X"; an open file in the IDE; a file path; a recently discussed entity in this conversation |
| **repository** | "use `ProductRepository`", "with `OrderRepo`"; or implied — if only one repository exists for the entity |
| **DTO mode** | "with DTO", "without DTO", "use entity directly", "map to `ProductDto`" |
| **controller name** | "name it `ProductResource`", "class `FooController`" |
| **paths** | "at `/api/products`", "base path `/rest`", "resource path `/items`" |
| **pagination** | "with pagination", "no pagination", "use Window", "use Page" |
| **filter** | "with filter", "no filter", "use JPA Specification" |
| **methods** | "all CRUD", "only read", "read-only", "without delete", "GET + CREATE", "full CRUD" |
| **smart defaults** | "use defaults", "all defaults", "default settings", "as usual" |
| **prior project facts** | language, JDK, dependencies — already known if discussed earlier in this conversation; do not re-fetch |

For every input that is **explicitly or strongly implicitly answered**:
mark it as decided and skip the corresponding question in Steps 2–4. Do
NOT ask "which entity?" if the user wrote "create CRUD controller for
Product" — `Product` is the answer. Do NOT ask "which repository?" if
there is exactly one repository for the entity.

For every input that is **not** answered: defer to the Decision-making
principle above — try to derive it from project context first (Step 1),
and only then ask.

Step 0 is mental, not a tool call. Do not announce it to the user. Do not
write "Step 0 done". Just internalize what the user already said before
proceeding to Step 1.

---

## Step 1 -- Gather minimal project context (automatic, no questions)

Call only the MCP tools whose result is **actually consumed** by a later
step. Do not pre-fetch "in case we need it" — every variable here must
have a concrete downstream user.

| Tool | Variable | Used for |
|------|----------|----------|
| `get_project_summary` | `language`, `bootMajor`, `moduleName`, `buildFile`, `mainPackage` | language → reference file selection (java vs kotlin); bootMajor → jakarta vs javax, Jackson version; moduleName → multi-module disambiguation; buildFile → Step 6 dependency injection; mainPackage → controller package fallback |
| `list_module_dependencies(moduleName)` | `presentDeps` | derived flags below — SpringDoc/Validation/Jackson feature gates, Step 6 dependency check |
| `list_project_endpoints` | `endpoints` | Step 4 path conflict detection, existing basePath/naming convention detection |

That is the entire Step 1. **Do NOT** fetch:
- `list_all_domain_entities` — needed only if the user did not name an
  entity in their prompt. Defer to Step 2 as a lazy fallback.
- `list_entity_repositories` — depends on knowing the entity, which
  happens in Step 2. Defer to Step 3 as a lazy fallback.
- `get_entity_details` — depends on knowing the entity. Defer to Step 2.
- `list_entity_dtos` / `list_entity_mappers` — depend on knowing the
  entity AND the DTO decision. Defer to Step 4.

After calling `get_project_summary`, determine:
- `language`: Java or Kotlin
- `bootMajor`: 2, 3 or 4 (for jakarta vs javax, Jackson version)
- Check if `org.springdoc:springdoc-openapi` is in `presentDeps` --> `hasSpringDoc`
- Check if `org.hibernate.validator:hibernate-validator` or `org.springframework.boot:spring-boot-starter-validation` is in `presentDeps` --> `hasValidation`
- **Jackson version detection:** Check `presentDeps` for `tools.jackson:jackson-databind` or `tools.jackson:jackson-core` --> `jacksonMajor = 3`. Otherwise if `com.fasterxml.jackson:jackson-databind` --> `jacksonMajor = 2`. Spring Boot 4.x uses Jackson 3.x (`tools.jackson` package), Spring Boot 2.x/3.x use Jackson 2.x (`com.fasterxml.jackson` package).
  - If `jacksonMajor = 3`: `JsonNodeFqn` = `tools.jackson.databind.JsonNode`, `ObjectMapperFqn` = `tools.jackson.databind.ObjectMapper`
  - If `jacksonMajor = 2`: `JsonNodeFqn` = `com.fasterxml.jackson.databind.JsonNode`, `ObjectMapperFqn` = `com.fasterxml.jackson.databind.ObjectMapper`
- **PagedModel detection:** `PagedModel` (`org.springframework.data.web.PagedModel`) is available in Spring Data 3.2+. If `bootMajor >= 3` (Spring Boot 3.2+) or `bootMajor = 4`, use `PagedModel` wrapper for GET_LIST with Page pagination. Otherwise return `Page<Entity>` directly.

If multi-module project (multiple modules in `get_project_summary`):
Ask which module to use. Then re-call module-specific MCP tools with that module.

---

## Step 2 -- Select entity

By Step 0 you should already know the entity if the user mentioned it.
Most common case: the user wrote "create CRUD controller for Product" →
entity is `Product`, skip the question, go straight to the fetch below.

**Lazy fallback — only when entity is unknown:** call
`list_all_domain_entities(moduleName)` → `entities`, then ask via
`AskUserQuestion` (options = entity names from the list, max 4; if more
than 4 entities, use the 4 most likely candidates based on context and
add a note that the user can type a different name via "Other").

This is the only place `list_all_domain_entities` should be called. If
the user named the entity in their prompt, do NOT call it.

After the entity FQN is known, call `get_entity_details(entityFqn)` to get:
- `IdType` -- entity ID field type (e.g. `java.lang.Long`, `java.util.UUID`)
- `entityVar` -- decapitalized entity name (e.g. `product`)
- `entityVarPlural` -- pluralized decapitalized name (e.g. `products`)
- `EntityName` -- simple class name (e.g. `Product`)
- `EntityNamePlural` -- pluralized simple name (e.g. `Products`)
- Whether entity has validation annotations --> `entityHasValidation`

---

## Step 3 -- Select repository

By Step 0 you may already know the repository if the user mentioned it.

Call `list_entity_repositories(entityFqn)` → `repos`. Then apply the
Decision-making principle:
- If exactly one repository exists → select it silently (principle 1).
- If the user named a specific repository → use it directly, skip the question.
- If multiple repositories exist → ask via `AskUserQuestion` with the
  options from `repos`.
- If no repository exists → inform the user and suggest creating one
  manually or using a separate skill.

After selection:
- `repoFieldName` -- decapitalized repository class name (e.g. `productRepository`)
- `RepoFqn` -- FQN of repository class

---

## Step 4 -- DTO and customization questions

By Step 0 you may already know the DTO mode if the user mentioned it
(e.g. "with DTO", "without DTO", "use entity directly", "map to ProductDto").
If so, skip the question and proceed.

If unknown, ask via `AskUserQuestion`:

| Question | Header | Options (first = recommended) |
|----------|--------|-------------------------------|
| Use DTO for mapping? | DTO mode | Yes, use DTO (Recommended): select existing or create new via `dto-creator` / No, use entity directly |

### If DTO selected:

Call `list_entity_dtos(entityFqn)` and `list_entity_mappers(entityFqn)`.

**If existing DTO + mapper found:** ask user to select them. Extract:
- `DtoFqn` -- DTO class FQN
- `dtoVar` -- decapitalized DTO name
- `mapperFieldName` -- decapitalized mapper class name
- `toDtoMethodName` -- mapper method entity-->DTO (e.g. `toDto`)
- `toEntityMethodName` -- mapper method DTO-->entity (e.g. `toEntity`)
- `updateMethodName` -- mapper method for update (e.g. `updateWithNullValues`)

Warn: CREATE and PATCH operations require the mapper to have `toEntity` and `updateWithNull` methods.

**If no DTO exists for the entity:** delegate to `dto-creator` to create
one. The `dto-creator` skill will handle DTO generation and will
automatically delegate to `mapper-creator` for the mapper (since the
DTO is for a REST controller — conversion is inevitable). After both
are created, return here and continue with Step 4 path settings.

**If DTO exists but no mapper:** delegate to `mapper-creator` to create
one. After the mapper is created, return here and continue.

### Path, pagination & filter settings

Apply the **Decision-making principle**. These settings almost always have
good defaults derivable from context — use principle 2 (one-line
confirmation) unless the user explicitly asked for customization:

```
Will create `{EntityName}Controller` in `{controllerPackage}` at `{basePath}/{entityVarPlural}`, Page pagination, no filter. OK?
```

The user can answer "ok" / "yes" / silence → accept all defaults; or
override specific values → apply only those overrides.

Only fall back to individual questions when the user explicitly asked for
fine-grained control ("custom paths", "configure pagination") or when
context yields no clear defaults.

If the user selects a filter: ask to select existing filter class. Extract
`FilterFqn` and `toSpecificationMethodName`. If the repository does not
extend `JpaSpecificationExecutor`, add it (WA-repo-enhance).

### Method selection

By Step 0 you may already know which methods the user wants (e.g. "read-only",
"only GET and CREATE", "full CRUD"). If so, skip the question.

If unknown, ask via `AskUserQuestion`:

| Question | Header | Options (first = recommended) |
|----------|--------|-------------------------------|
| Which CRUD methods to generate? | Methods | Full CRUD (Recommended): GET_LIST, GET_ONE, GET_MANY, CREATE, PATCH, PATCH_MANY, DELETE, DELETE_MANY / Standard CRUD: GET_LIST, GET_ONE, CREATE, PATCH, DELETE / Read-only: GET_LIST, GET_ONE / Custom: select individual methods |

Store the selected method set as `selectedMethods`. Step 5.3 generates only
these methods, skipping the rest.

---

## Step 5 -- Generate code

### 5.1 Create controller class (WA1)

Read `examples/_skeletons/java.md` or `examples/_skeletons/kotlin.md` based on `language`.

Apply variable substitutions:
- `{packageName}` --> controllerPackage
- `{className}` --> controllerName
- `{requestPath}` --> basePath + resourcePath

Use **Write** tool to create `src/main/{java,kotlin}/{package-path}/{controllerName}.{java,kt}`.

### 5.2 Add bean injection (WA2)

Read `examples/_beans/injection/java.md` or `kotlin.md`.

Add constructor parameter for repository. If DTO with mapper: also inject mapper bean.
Inject `${ObjectMapperFqn}` field (resolved in Step 1: `tools.jackson.databind.ObjectMapper` for Jackson 3.x, `com.fasterxml.jackson.databind.ObjectMapper` for Jackson 2.x).

Use **Edit** tool to modify the controller class.

### 5.3 Add CRUD methods (WA3-WA11)

For each method in `selectedMethods` (from Step 4 method selection):

1. Determine the example file path:
   `examples/_methods/{method-name}/{language}.md`

2. Read the example file

3. Select the correct code variant based on:
   - DTO mode (no DTO vs with DTO)
   - For GET_LIST: pagination (PAGE / WINDOW / none) and filter (with/without)
   - For PATCH/PATCH_MANY: ObjectMapper vs ObjectPatcher, and DTO vs no-DTO (4 combinations)
   - For CREATE: with/without @Valid

4. Apply variable substitutions (ONLY variables declared in the Variables section)

5. **FQN handling (CRITICAL):** examples contain FQNs (e.g. `org.springframework.web.bind.annotation.GetMapping`,
   `org.springframework.data.domain.Pageable`, entity/DTO/repository FQNs). When
   writing the final file, you MUST:
   1. Replace every FQN in the body with its **short name**
   2. Collect every FQN you shortened and emit a corresponding `import` line
      right after the `package` statement, sorted, no duplicates
   3. Classes from the same package as the controller must NOT be imported
   4. Types from `java.lang` must NOT be imported
   5. Kotlin: same rules — shorten in the body and add `import` lines at the top

6. Use **Edit** tool to insert the method into the controller class body

### 5.4 Add JpaSpecificationExecutor if needed (WA-repo-enhance)

If filter is selected and repository does not already extend `JpaSpecificationExecutor`:

Read `examples/_beans/repo-enhance/java.md` or `kotlin.md`.
Use **Edit** tool to add `org.springframework.data.jpa.repository.JpaSpecificationExecutor<{EntityFqn}>` to the repository's extends list.

---

## Step 6 -- Dependencies & properties (automatic)

1. Read `examples/_dependencies/dependencies.md`
2. For each artifact NOT in `presentDeps`:
   - Use `buildFile` from Step 1
   - Edit the build file to add the dependency
3. Call `refresh_build_system_model`
4. No properties are written for this skill (controller has no application.properties entries)
5. Report: "Created controller {controllerName} with CRUD endpoints for {EntityName}. Added dependencies: [list]."

---

## Anti-hallucination checklist

Before writing ANY code, verify:
- [ ] The code comes from an examples/ file (cite which one)
- [ ] Only declared variables were substituted
- [ ] No framework API calls were added "from knowledge"
- [ ] Import list covers all FQNs shortened in the body (no missing imports, no extras)
- [ ] Method signatures match the example exactly
- [ ] No comments or convenience methods were added
- [ ] FQNs from examples are shortened in the body AND corresponding `import` lines were added after `package` (IDE will NOT do this for you)
- [ ] `@ParameterObject` annotation is only added when `hasSpringDoc = true`
- [ ] `@Valid` annotation is only added when `hasValidation = true` AND entity has validation annotations
- [ ] PATCH and PATCH_MANY methods use ObjectMapper (fallback) or ObjectPatcher depending on project dependencies
- [ ] getId() in PATCH_MANY matches entity's actual ID accessor
- [ ] Jackson FQNs use `tools.jackson.databind.*` for Jackson 3.x (Spring Boot 4.x) and `com.fasterxml.jackson.databind.*` for Jackson 2.x (Spring Boot 2.x/3.x). NEVER hardcode one or the other — always resolve `${JsonNodeFqn}` and `${ObjectMapperFqn}` from Step 1.
- [ ] GET_LIST with Page pagination uses `PagedModel<Entity>` wrapper (Spring Data 3.2+ / Boot 3.2+/4.x) or returns `Page<Entity>` directly (older versions). Select the correct example variant.
