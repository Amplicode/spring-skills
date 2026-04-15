---
name: dto-creator
description: >
  Creates a DTO (Data Transfer Object) class for an entity.
  Use this skill when a DTO class needs to be created, either standalone or as part of a larger task
  (e.g. REST controller, service layer, replacing entity usage with a DTO).
---

# DTO Creator

Creates a DTO class (Java class, Java record, Java+Lombok, or Kotlin data class) for an entity with selected attributes, constructors, getters/setters, equals/hashCode, toString, and optional features.

---

> **CRITICAL: Code ONLY from examples/ files. If no matching example -- STOP and ask user.**
> **CRITICAL: For questions with a fixed set of choices, prefer `AskUserQuestion` > its analogue > plain text list. Plain numbered text lists are the last resort when no interactive tool is available.**
> **CRITICAL: Read the conversation context BEFORE running Step 1.** Half the questions in Steps 2–7 may already be answered by the user's prompt and prior turns. Re-asking what was already said is the #1 reason this skill feels slow.
> **Step 7 (mapper) is automatic when conversion is needed.** If from context it is clear that the DTO will be used in code that converts entities to/from DTOs (controller, service, endpoint replacement, etc.), the skill MUST delegate to `mapper-creator` — never write manual mapping code inline. The `mapper-creator` skill decides the implementation (MapStruct, Custom, adding dependencies) — this skill just delegates.

---

## Defaults

| Option | Default | Always ask? | Notes |
|--------|---------|-------------|-------|
| entity | -- | YES | main branching: which entity to create DTO for |
| attributes | all entity fields | YES | which fields to include |
| className | `{EntityName}Dto` | NO | suggest, confirm only |
| language | from `get_project_summary` | NO | auto-detected |
| variant | auto from language + deps | YES | Java class / Java record / Java+Lombok / Kotlin |
| mutable | false | NO | skip unless user wants customization |
| allArgsConstructor | true | NO | Java plain only |
| equalsHashCode | true | NO | Java plain only |
| toString | true | NO | Java plain only |
| fluentSetters | false | NO | only when mutable=true, Java only |
| jsonIgnoreUnknownProperties | false | NO | Java only, when Jackson on classpath |
| serializableType | NoSerializable | NO | rarely needed |
| packageName | same package as entity | NO | auto-detected |
| subDtoType (per ToOne association) | FLAT with id-only sub | YES | Four options available: New Class / New Nested Class / Existing Class / Flat. "Only ID" does NOT exist as a separate option — it is Flat with only the sub-entity `id` checked. See `references/sub-dto.md`. |
| subDtoType (per ToMany / collection) | NEW_NESTED_CLASS | YES | same 4 options as ToOne. Flat IS supported for collections and produces composite plural fields like `Set<Integer> specialtyIds` — see `references/sub-dto.md`. |
| fieldNameOverride (per field) | none | NO | per-field rename |
| extraValidations (per field) | none | NO | user-added jakarta validators on top of inherited ones |
| removedValidations (per field) | none | NO | constraints inherited from entity that the user wants dropped |
| indent | from `.editorconfig` (fallback 4-space) | NO | see § Indentation below |

**Smart defaults:** If user says "use defaults", "all defaults", "default settings",
or similar -- skip ALL questions where "Always ask?" = NO. Only ask mandatory questions.

**Smart answer recognition:** When user provides a value instead of choosing from a numbered
list, accept it directly. Examples:
- Question "Which entity?" -> user answers "Order" -> this IS the entity, don't re-ask
- Question "Variant?" -> user answers "record" -> this IS Java record, don't show options
- If user provides multiple answers in one message -> accept all, skip answered questions
- NEVER ask a question that the user already answered (even implicitly)

**Batch questions:** Group closely related questions into a single
`AskUserQuestion` call (up to 4 questions per call) when they:
- Belong to the same logical section (e.g. both are Java method generation settings)
- Don't depend on each other's answers
- Have obvious defaults that the user can skip

Rules:
- Maximum **3-4 questions** per `AskUserQuestion` call
- Mark the recommended option with `(Recommended)` and place it first
- Never batch questions from DIFFERENT decision branches
- The primary branching question (entity selection, variant) is always asked ALONE
- Prefer `AskUserQuestion` for choices; fall back to plain text lists only if the tool is unavailable

---

## Decision-making principle — context first, then ask

Before asking the user **any** question, attempt to derive the answer from
the context already gathered: project summary, module dependencies, entity
details, existing files in the package, prior turns of this conversation,
and the user's original prompt. Only ask when the context yields **no
clear default** or when the choice is genuinely user-specific (e.g. which
entity, which fields).

Hierarchy of decisions:

1. **Context is unambiguous → decide silently, do NOT ask.**
   Examples: language and JDK from `get_project_summary`; Lombok / Jackson /
   Hibernate Validator from `list_module_dependencies`; package from the
   entity FQN; className from `{Entity}Dto`; back-reference `@ManyToOne`
   filtering; auto-selection of sub-entity scalars; record vs. plain class
   when JDK ≥ 16 and no Lombok and the user did not request otherwise.
   **Exception: subDtoType is NEVER decided silently** — it always requires
   at minimum principle 2 (one-line confirmation), unless the USER explicitly
   stated the shape in their own message (not in ARGUMENTS).

2. **Context gives a strong signal → state the decision + alternatives in one line, let the user override or stay silent.**
   Format:
   ```
   Will create `ScheduleTemplateDto` as a Java record with nested `SlotDto` (record).
   Alternatives: plain class, Lombok, separate file for SlotDto. OK?
   ```
   The user can answer "ok" / "yes" / silence → accept; or name an
   alternative → switch. This is **not** the same as the numbered question
   format — it is a single confirmation line.

3. **Context yields no clear default → ask with `AskUserQuestion` (preferred) or its analogue, with the recommended option first.**
   When `AskUserQuestion` is available, use it with `preview` fields so the
   user sees the concrete code shape for each option. Mark the recommended
   option with `(Recommended)` in its label and place it first. If no
   interactive choice tool is available, fall back to a plain text list.
   **Never** ask iteratively ("which variant?" → user picks → "which fields?" → …)
   when one batched call would do.

4. **Context is fully empty for a critical input → ask plainly.**
   This applies to: which entity, which fields (when not "all"), the
   user's intent itself.

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
- Use `multiSelect: true` for "which fields to include" or "which
  validators to add" — anything where multiple answers are valid.
- `header` is a 12-char chip label (e.g. "Variant", "Sub-DTO", "Fields").
- Each option has a `description` explaining what the choice means or its
  consequence (one short sentence).

When `AskUserQuestion` is **not** the right tool:
- Free-form input where there is no enumerable set of options
  (e.g. arbitrary class name, arbitrary field rename) — ask in plain text.
- The "single confirmation line" form from principle 2 — that is a plain
  question with an obvious yes/no, not an enumerated choice.

The screen-driven question lists in Steps 2–5 below are a **fallback** for
case 4. They are NOT a script to execute top-to-bottom. If a question's
answer is already determined by principles 1–3, **skip the question**.
Plugin-wizard fidelity is not a goal — generation correctness is. The
plugin's UI exists because a screen can show many controls at once; a
chat cannot, so the skill must compensate by deciding more on its own.

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
| **entity** | a class name (`Vet`, `Owner`, `ScheduleTemplate`); "for X"; an open file in the IDE; a file path; a recently discussed entity in this conversation |
| **purpose** | "for REST", "for controller", "for API", "for mapping", "projection", "for service" — drives field selection and the mapper question |
| **fields** | "all fields", "only id and name", "without password", "with associations", "flat" |
| **variant** | "record", "plain class", "Lombok", "data class" — also implied by language: Kotlin → data class, no question |
| **mapper** | "and mapper", "with mapper", "only DTO", "no mapper" |
| **className** | "name it `OwnerSummaryDto`", "class `Foo`" |
| **package** | "in package `…`", "next to controller" |
| **smart defaults** | "use defaults", "all defaults", "default settings", "as usual" |
| **sub-DTO shape** | "nested", "separate class", "only id", "flat" |
| **prior project facts** | language, JDK, dependencies — already known if discussed earlier in this conversation; do not re-fetch |

For every input that is **explicitly or strongly implicitly answered**:
mark it as decided and skip the corresponding question in Steps 2–7. Do
NOT ask "what entity?" if the user wrote "create DTO for Vet" — `Vet` is
the answer. Do NOT ask "Java record or class?" if the user wrote "make
record DtoX" — record is the answer.

For every input that is **not** answered: defer to the Decision-making
principle below — try to derive it from project context first (Step 1),
and only then ask.

**CRITICAL: ARGUMENTS ≠ user intent.** The ARGUMENTS block at the bottom
of this prompt is written by the invoking assistant, NOT by the user. It
may contain the assistant's own analysis, assumptions, or field-level
details that the user never stated. When determining what the user
"already said", look ONLY at the actual user messages in the conversation
history — never treat ARGUMENTS as a substitute for user input. In
particular, do NOT skip asking about sub-DTO shape just because ARGUMENTS
describes one.

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
| `get_project_summary` | `language`, `jdkVersion`, `moduleName` | language → variant selection (Step 4) and reference file (java vs kotlin); jdkVersion → `canUseRecords = jdkVersion >= 16`; moduleName → multi-module disambiguation + parameter for `list_module_dependencies` |
| `list_module_dependencies(moduleName)` | `presentDeps` | derived flags below — Lombok/Jackson/Validation feature gates |

That is the entire Step 1. **Do NOT** fetch:
- `springBootVersion` / `bootMajor` — no branching depends on it
- `buildFile` — DTO generation does not edit the build file (no
  dependencies to add); a DTO needs only language constructs
- `mainPackage` — the DTO's package is derived from the entity's FQN,
  not from the project root
- `list_application_properties_files` / `propsFile` — DTO writes nothing
  to `application.properties`
- `list_all_domain_entities` — needed only if the user did not name an
  entity in their prompt. Defer to Step 2 as a lazy fallback.
- `get_entity_details` / `list_entity_dtos` — depend on knowing the
  entity, which happens in Step 2. Defer to Step 2.

Derived variables (all from `presentDeps` and `get_project_summary`):
- `hasLombok` = `presentDeps` contains `lombok`
- `hasJackson` = `presentDeps` contains `jackson-databind` or `jackson-core`
- `hasValidation` = `presentDeps` contains `hibernate-validator` or `jakarta.validation-api` or `spring-boot-starter-validation`
- `canUseRecords` = `language == JAVA` AND `jdkVersion >= 16`

If multi-module project (multiple modules in `get_project_summary`):
Ask which module to use. Then re-call `list_module_dependencies` for
that module.

---

## Step 2 -- Entity selection

By Step 0 you should already know the entity if the user mentioned it.
Most common case: the user wrote "create DTO for `Vet`" → entity is
`Vet`, skip the question, go straight to the parallel fetch below.

**Lazy fallback — only when entity is unknown:** call
`list_all_domain_entities(moduleName)` → `entities`, then ask via
`AskUserQuestion` (options = entity names from the list, max 4; if more
than 4 entities, use the 4 most likely candidates based on context and
add a note that the user can type a different name via "Other").

This is the only place `list_all_domain_entities` should be called. If
the user named the entity in their prompt, do NOT call it.

After the entity FQN is known, call (in parallel):

| Tool | Variable | Used for |
|------|----------|----------|
| `get_entity_details(entityFqn)` | `entityDetails` | Step 3 attribute selection, association analysis, validation inheritance, generation order in every variant reference |

`list_entity_dtos(entityFqn)` for the **parent** entity is NOT called
here — it has no downstream consumer. Sub-entity calls
(`list_entity_dtos(subEntityFqn)`) are still needed for `EXIST_CLASS`
detection in `references/sub-dto.md`, but they are lazy and per-association,
fired only when the user is offered the EXIST_CLASS option. See
`references/sub-dto.md` for the exact place.

DTO **name collision** is handled by a different lazy call —
`list_existing_classes(package)` — fired right before generation in
Step 6 (see anti-hallucination checklist). It is not part of Step 2.

---

## Step 3 -- Attribute selection

**Default: include every scalar attribute and every association** (with the
sub-DTO defaults from `references/sub-dto.md`). Ask only when context
signals that the user wants something narrower.

### Decide from context (preferred over asking)

Use the **purpose** captured in Step 0 to pick a sensible default set:

| Purpose signal | Default field set |
|---|---|
| "for REST", "for controller", "for API" | all scalars + all associations expanded (NEW_NESTED_CLASS for ToMany, FLAT id for ToOne) — the user wants the response shape, including related data |
| "projection", "summary", "list view", "for list" | scalars only, ToOne associations as Flat id, ToMany excluded |
| "for mapping", "for service", "DTO for storage" | all scalars + all associations expanded |
| "only id and name" / explicit field list | exactly what the user named, nothing else |
| no purpose signal | all scalars + all associations expanded (richest reasonable default) |

If the chosen default matches the user's apparent intent, **do not ask**.
Just generate. State the choice in the one-line confirmation form
(principle 2) at most.

### When to ask

Ask only if:
- The user explicitly said "choose fields" / "ask about fields" / "fine-tune
  settings", OR
- The entity has many fields and the purpose signal is ambiguous AND the
  user did not say "use defaults".

When asking, use `AskUserQuestion` with `multiSelect: true`:

| Question | Header | Options (first = recommended) |
|----------|--------|-------------------------------|
| Which fields to include in DTO `{Entity}Dto`? | Fields | All fields + associations (Recommended) / Scalars only / Only id and name / Specify manually |

For each **association** included, apply the sub-DTO defaults from
`references/sub-dto.md`. Do NOT ask per-association unless the user
explicitly requested fine-grained control.

---

## Step 4 -- Variant selection

Apply the **Decision-making principle** above. The variant is almost always
derivable from context — explicit asking should be the exception, not the
default.

**If language=KOTLIN:** route to `references/kotlin.md`. No question.

**If language=JAVA:** decide as follows.

| Context | Action | Variant |
|---|---|---|
| `hasLombok = true` AND user prompt explicitly mentions Lombok | decide silently | `references/java-lombok.md` |
| `canUseRecords = true` AND `hasLombok = false` AND user did not ask for mutability/setters/Lombok | decide silently, mention in the one-line confirmation from principle 2 | `references/java-record.md` |
| `canUseRecords = false` AND `hasLombok = false` | decide silently | `references/java-plain.md` |
| `hasLombok = true` AND `canUseRecords = true` AND user gave no signal | use the one-line confirmation form (principle 2): "Will create as Java record (JDK ≥ 16, no Lombok). Alternatives: plain class, Lombok. OK?" | depends on answer |
| User explicitly said "plain class" / "mutable" / "with setters" | decide silently | `references/java-plain.md` |
| User explicitly said "record" | decide silently | `references/java-record.md` |
| User explicitly said "Lombok" | decide silently (only if `hasLombok = true`; otherwise warn and fall back) | `references/java-lombok.md` |

Only fall back to the full numbered question when **none** of the rows
above matches AND the user has not said "use defaults". Even then, prefer the
"all variants with default marked" format from principle 3 over an
iterative question.

Map answer to variant:
- plain → `references/java-plain.md`
- record → `references/java-record.md`
- Lombok → `references/java-lombok.md`

---

## Step 5 -- Variant-specific questions

Follow the variant-specific questions from the selected reference file.
Only ask if user did NOT say "all defaults".

---

## Step 6 -- Generate code

1. Determine target path:
   `src/main/{java|kotlin}/{packagePath}/{className}.{java|kt}`

2. Follow the **Generation Order** from the selected reference file.

3. For each step:

   **If skeleton (new file):**
   - Read the skeleton `.md` from `examples/_skeletons/`
   - Apply variable substitutions
   - Use Write tool to create the file

   **If fragment:**
   - Read the fragment `.md` from `examples/_fragments/`
   - Read the Insert Point to know WHERE to insert
   - Use Edit tool to insert code at the specified point
   - Apply variable substitutions

4. Variable substitution rules:
   - `{packageName}` -> from entity package or user choice
   - `{className}` -> from user or default `{EntityName}Dto`
   - Field-level variables -> from entity details
   - **NEVER substitute anything not listed in Variables**
   - **NEVER add imports, methods, or code not in the example**
   - **FQN handling (CRITICAL):** examples contain FQNs (e.g. `java.util.Objects`,
     `java.util.List`, `jakarta.validation.constraints.NotNull`). When writing the
     final file, you MUST:
     1. Replace every FQN in the body of the class with its **short name**
        (e.g. `java.util.Objects.hash(...)` -> `Objects.hash(...)`,
        `java.util.List<Integer>` -> `List<Integer>`,
        `@jakarta.validation.constraints.NotNull` -> `@NotNull`).
     2. Collect every FQN you shortened and emit a corresponding `import` line
        right after the `package` statement, sorted, no duplicates.
     3. Types from `java.lang` (`String`, `Integer`, `Object`, ...) must NOT be
        imported and must appear as short names.
     4. Classes from the same package as the DTO must NOT be imported.
     5. **Javadoc `{@link ...}` references — UNIFORM:** Generate **short name + import** in `{@link …}` for **every** shape:
        top-level Java class, top-level Java record, nested static class,
        nested record, and separate-file sub-DTO (NEW_CLASS). There is no
        asymmetry. Always shorten the entity reference and always add the
        corresponding `import` line (unless the entity is in the same
        package).
     6. **Group imports** in two blocks separated by ONE blank line:
        - **Block 1** — all third-party / project imports together:
          `jakarta.*`, `com.fasterxml.*`, `org.springframework.*`,
          `org.hibernate.*`, project packages, etc. (alphabetical inside the
          block).
        - **(blank line)**
        - **Block 2** — `java.*` and `javax.*` (alphabetical).
        Do NOT split block 1 into per-package sub-blocks.
     The final file must contain short names in the body (including every
     Javadoc `{@link …}`) and a clean, grouped import block at the top.

5. For **sub-DTOs** (subDtoType=NEW_CLASS): create a separate file by repeating Steps 6.1-6.4 recursively for the sub-entity.

6. For **nested classes** (subDtoType=NEW_NESTED_CLASS): add inner class to the parent DTO file, then fill it following the same fragment rules.

7. **Nested in a record parent — MANDATORY:** when the parent DTO is a Java
   record, every `NEW_NESTED_CLASS` association MUST be emitted as a nested
   `public record` inside the parent record's body. The skill MUST NOT
   silently fall back to `NEW_CLASS` (separate file) just because the
   record-form fragment looks shorter. The full procedure is in
   `references/java-record.md` Step 6 and
   `examples/_fragments/nested-class/java/nested-class.md` ("Java record"
   variant). If those instructions seem ambiguous to you, that is a bug in
   the skill — fix the docs, do NOT work around it by changing the
   `subDtoType`.

---

## Step 7 -- Mapper (automatic when conversion is needed)

Decide whether a mapper is needed based on context, then act:

| Signal | Action |
|---|---|
| User said "only DTO" / "no mapper" / "without mapper" | Skip Step 7 entirely. Do NOT mention the mapper. |
| User explicitly asked for a mapper ("and mapper", "with mapper", "create mapper too") | Delegate to `mapper-creator` immediately. |
| From context it is clear that entity↔DTO conversion will happen (user asked to replace entity with DTO in a controller/service/endpoint, user asked to convert/map/transform, DTO is for REST API, etc.) | Delegate to `mapper-creator` immediately. The conversion is inevitable — creating the DTO without a mapper would force manual inline mapping code, which is never acceptable. |
| Context is silent — no signal about how the DTO will be used | Skip Step 7. Do not mention the mapper. |

**CRITICAL:** Never write manual mapping code (inline `toDto`/`fromDto` methods in controllers, services, or anywhere else). If conversion is needed, always delegate to `mapper-creator`. That skill decides the implementation strategy (MapStruct, Custom mapper, adding dependencies) — this skill just delegates.

Never ask `AskUserQuestion` for the mapper. If delegating, invoke the
`mapper-creator` skill with the DTO and entity information directly —
do not ask the user to confirm the delegation.

---

## Indentation

The skill MUST detect the project's indentation style — never hardcode tabs
or spaces. Detection order:

1. **`.editorconfig`** at the project root (or any parent of the target
   file's directory). For Java files, look up the `[*.java]` or `[*]`
   section and read `indent_style` (`tab` or `space`) and `indent_size` /
   `tab_width`.
2. **Sample existing Java files** in the same package (or the nearest
   ancestor package that contains Java files). Detect whether the leading
   whitespace on indented lines uses `\t` or spaces, and how many.
3. **Default to 4-space** if neither source is conclusive. (Tabs are also
   acceptable as a default if the developer explicitly prefers them, but
   the skill must never silently assume one over the other.)

Whatever style is chosen, apply it **uniformly** to every line of every
generated fragment (fields, constructors, getters/setters, equals/hashCode,
toString, nested classes/records). Never mix tabs and spaces inside the
same file.

## Per-field options

The skill must support the following per-field controls:

- **Field rename** (`fieldNameOverride`): the DTO field name can differ from
  the entity attribute name. Mapper generation still maps it from the
  original attribute.
- **Add validations** (`extraValidations`): the user can add jakarta /
  hibernate-validator constraints on top of the ones inherited from the
  entity. The list of allowed constraints depends on the field type — see
  `references/validation.md`.
- **Remove inherited validations** (`removedValidations`): the user can drop
  any constraint that came from the entity field.
- **Edit annotation parameters** (`message`, `min`, `max`, `regexp`, …): all
  parameters of every constraint are editable.

These options never appear unless the user explicitly asks for "fine-tune
field settings", "per-field validation" or similar. By default the skill
just inherits everything from the entity.

## Anti-hallucination checklist

Before writing ANY code, verify:
- [ ] The code comes from an examples/ file (cite which one)
- [ ] Only declared variables were substituted
- [ ] No framework API calls were added "from knowledge"
- [ ] Import list matches the example exactly
- [ ] Method signatures match the example exactly
- [ ] No comments or convenience methods were added
- [ ] FQNs from examples are shortened in the body AND corresponding `import` lines were added after `package`
- [ ] **Every** Javadoc shape — top-level class, top-level record, nested static class, nested record, separate-file sub-DTO — uses short name `{@link Pet}` AND adds a matching `import` (unless the entity is in the same package). Uniform rule, no asymmetry.
- [ ] If a sub-DTO name (separate file or nested) would collide with an existing class in the target package, **auto-suffix with a number** (e.g. `PetDto1`). The skill must do the same — call `list_existing_classes(package)` before generating, and append `1`, `2`, … until the name is free.
- [ ] When generating a **Java record**, the inner class for any `NEW_NESTED_CLASS` association is also a **record** (not a static class). All component validators are inlined onto the record component parameters (e.g. `@NotBlank String firstName`), not on separate field declarations.
- [ ] When `isJavaRecord = true`, the skill MUST NOT emit `equals()`, `hashCode()`, `toString()`, mutable setters, or fluent setters — records auto-generate these and these options should not be available for record DTOs.
- [ ] Class-level Javadoc is **multi-line** (`/**\n * DTO for {@link …}\n */`), never collapsed to one line.
- [ ] Getters and setters are **multi-line** (signature line, indented body, closing brace), never one-liners.
- [ ] In `mutable=true && fluentSetters=false` mode, getters and setters are **interleaved** (`getX, setX, getY, setY, …`), not grouped.
- [ ] In `mutable=true` mode, **both** the no-args constructor and the all-args constructor are emitted.
- [ ] The skill does NOT offer "Only ID" as a separate option — this option does not exist (neither for ToOne nor for ToMany). The "association id only" effect is produced by **Flat with only the sub-entity `id` checked**, and the skill must implement it that way.
- [ ] For collection associations (`List<X>`, `Set<X>`), Flat **is** offered and produces composite plural fields (`Set<Integer> specialtyIds`, `List<Integer> petIds`), provided the skill auto-checks the sub-entity scalars.
- [ ] Back-reference `@ManyToOne` fields are filtered out of the attribute list (e.g. `Pet.owner` is not offered when creating `PetDto`).
- [ ] Imports are grouped with a blank line between `jakarta.*` / `com.*` / `org.*` / `java.*` blocks.
- [ ] Indentation matches the project's detected style (`.editorconfig` first, then sampling existing files in the same package, then 4-space default). Never hardcode tabs or spaces. Apply uniformly across every fragment in the file.
- [ ] @JsonIgnoreProperties is NOT added for Kotlin (Java-only feature)
- [ ] Validation annotations use `@field:` prefix in Kotlin
- [ ] Java types are converted to Kotlin types for Kotlin DTOs
