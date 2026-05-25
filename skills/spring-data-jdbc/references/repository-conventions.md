# Detect Repository Conventions

Follow substeps 1.1 → 1.2 → 1.3 → 1.4 → 1.5 in order. Do not skip or reorder them.

---

## Step 1.1: Find existing repositories

Call `list_entity_repositories` (parameters: `entityFqn` optional, `moduleName` optional — there is no built-in JDBC filter) to get the list of repositories. When you already know the target entity, pass `entityFqn` so the result is scoped to it. Otherwise omit `entityFqn` and filter the result manually: for each returned `entityFqn`, only keep those whose entity class is annotated with `@Table` from `org.springframework.data.relational.core.mapping` (open the file or call `get_jdbc_entity_details` — JPA `@Entity` classes will simply fail the latter).

Pick 2–3 representative JDBC repositories and read their source files. Skip Spring Data JPA repositories — they belong to a different skill.

A Spring Data JDBC repository is a `*Repository` interface whose target entity is annotated with `@Table` from `org.springframework.data.relational.core.mapping`. It typically extends one of:

- `org.springframework.data.repository.CrudRepository<T, ID>`
- `org.springframework.data.repository.ListCrudRepository<T, ID>`
- `org.springframework.data.repository.PagingAndSortingRepository<T, ID>`
- `org.springframework.data.repository.ListPagingAndSortingRepository<T, ID>`

`@Query` annotations on JDBC repositories come from `org.springframework.data.jdbc.repository.query.Query` — **not** from `jakarta.persistence` or `org.springframework.data.jpa.repository.Query`.

---

## Step 1.2: Score each convention

General conventions:

- **Base interface** — which Spring Data interface is extended: `CrudRepository`, `ListCrudRepository`, `PagingAndSortingRepository`, `ListPagingAndSortingRepository`? Default: `ListCrudRepository` (returns `List` instead of `Iterable` and is friendlier to call sites).
- **Repository naming** — does the project use `<Entity>Repository` (e.g. `OrderRepository`) or some other template? Default: `<Entity>Repository`.
- **Repository location** — same package as the entity, a `repository` sub-package, or a separate `infrastructure` module? Default: same package as the entity.
- **Generic ID parameter** — does the repository's `ID` generic match the entity's `@Id` type exactly (no widening)? Default: yes, exact match.

Query conventions:

- **Query style** — when a finder cannot be expressed as a derived method name, is it written as (a) `@Query("SELECT ...")` with named parameters (`:name`), or (b) `@Query("SELECT ... WHERE x = ?1")` with positional parameters? Default: named parameters.
- **SQL dialect** — are queries written in standard SQL or in vendor-specific dialect (Postgres functions, MySQL hints)? Default: standard SQL.
- **Result mapping** — for `@Query` projections, does the project use (a) DTO mapping via `record` / class, (b) interface projections, or (c) plain `Map<String, Object>`? Default: DTO via record.
- **Pagination shape** — when pagination is needed, do methods take `Pageable` and return `Page<T>`/`Slice<T>`, or just `Sort` + `Limit`? Default: `Pageable` + `Page<T>`.

Modifying queries:

- **`@Modifying` usage** — are UPDATE/DELETE statements always annotated with `@Modifying` (from `org.springframework.data.jdbc.repository.query.Modifying`)? Default: yes — required.
- **Modifying return type** — `void`, `int` (row count), or `boolean`? Default: `int`.

Transaction conventions:

- **Transactional layer** — where are `@Transactional` boundaries declared: on repository methods, service methods, or controller methods? Default: on service methods. Spring Data already wraps each save/find in its own transaction; broader boundaries belong on the service layer.

---

## Step 1.3: Collect uncertain conventions

Score confidence only where the code contains relevant examples but the pattern is ambiguous. If a convention is absent (e.g. no `@Query` anywhere, no pagination, no `@Modifying`) — confidence is high, use the default without asking.

Collect all conventions where confidence < 80. For each, formulate a question with explicit answer options; put the default value first (marked as "Recommended").

---

## Step 1.4: Ask developer

If there are any uncertain conventions, ask the developer. In Claude Code, use `AskUserQuestion` and combine all questions into a single tool call. In runtimes without that primitive (Codex / OpenCode / plain CLI), render the same questions inline — see the "Harness compatibility" section in `SKILL.md`. The JSON payload below is both the `AskUserQuestion` payload and a template for the inline rendering.

Example call:

```json
{
  "questions": [
    {
      "header": "Base interface",
      "question": "Which Spring Data interface should JDBC repositories extend?",
      "multiSelect": false,
      "options": [
        { "label": "ListCrudRepository (Recommended)", "description": "Returns List<T> from findAll/findAllById — easier to consume than Iterable" },
        { "label": "CrudRepository", "description": "Returns Iterable<T>; pre-Spring-Data 3 default" },
        { "label": "ListPagingAndSortingRepository", "description": "Adds Pageable/Sort to ListCrudRepository" }
      ]
    },
    {
      "header": "Query style",
      "question": "How should @Query parameters be referenced?",
      "multiSelect": false,
      "options": [
        { "label": "Named (:name) (Recommended)", "description": "@Query(\"... WHERE x = :foo\") + @Param(\"foo\") — survives parameter reordering" },
        { "label": "Positional (?1)", "description": "@Query(\"... WHERE x = ?1\") — terser but brittle on refactors" }
      ]
    }
  ]
}
```

---

## Step 1.5: Summarize resolved conventions

Output a consolidated list of every convention from Step 1.2 with its resolved value:

```
- Base interface: ListCrudRepository
- Repository naming: <Entity>Repository
- Repository location: same package as the entity
- Generic ID parameter: matches entity's @Id type exactly
- Query style: named parameters (:name)
- SQL dialect: standard SQL
- Result mapping: DTO via record
- Pagination shape: Pageable + Page<T>
- @Modifying usage: required on every UPDATE/DELETE
- Modifying return type: int
- Transactional layer: service methods
```

This list is your working contract for all repository code written in this session.
