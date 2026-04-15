# Detect Entity Conventions

Follow substeps 1.1 → 1.2 → 1.3 → 1.4 → 1.5 in order. Do not skip or reorder them.

---

## Step 1.1: Find existing entities

Call `list_all_domain_entities` to get a list of entities in the project. Pick 2–3 representative ones and read their source files.

---

## Step 1.2: Score each convention

For each convention below, determine the answer from the code and assign a confidence score (1–100):

General conventions:

- **ID strategy** — check existing `@Id` fields: is it `Long` with `@GeneratedValue(strategy = SEQUENCE)` (sequence), `Long` with `@GeneratedValue(strategy = IDENTITY)` (identity/autoincrement), or `UUID` with `@GeneratedValue(strategy = UUID)` (client-generated)? Default: database-generated (`Long` + `SEQUENCE`)
- **Sequence scope** — if SEQUENCE is used, check `@SequenceGenerator`: is there a dedicated sequence per table (each entity has its own `generator` name), a shared sequence across all tables (one common generator), or is `@SequenceGenerator` omitted entirely (Hibernate default sequence)? Default: dedicated sequence per table
- **Annotation placement** — are `@Column`, `@Id`, etc. on fields or getter methods? Default: on fields
- **`serialVersionUID`** — does any entity declare `private static final long serialVersionUID`? Default: not generated
- **FetchType on @ManyToOne / @OneToOne** — check the `fetch` attribute. Default: `LAZY`
- **Fluent setters** — do setters return `this` or `void`? Default: `void`
- **Field access modifier** — are fields `private` or `protected`? Default: `private`
- **Naming strategy** — does the project rely on JPA Implicit Naming Strategy (names omitted from `@Table`/`@Column`), or are all names explicit? Default: explicit — always name JPA objects explicitly so that no Implicit Naming Strategy affects the code
- **Table name template** — check `@Table(name=...)` values: case (lower/upper/as-is), prefix, postfix, underscores, pluralized? Default: lower case, underscore, no prefix/postfix, not pluralized
- **Column name template** — check `@Column(name=...)` values: case (lower/upper/as-is), prefix, postfix, underscores? Default: lower case, underscore, no prefix/postfix
- **Entity class name convention** — is the Java class name transformed in any way (prefix/postfix)? Default: as-is, no transformation
- **Index/constraint name case** — check `@Index(name=...)`, `@UniqueConstraint(name=...)`. Default: `lower`

Constants Generation conventions (score separately):

- **Constants generated?** — does any entity have `public static final String` constants for entity/table/column names? Default: no
- **What is generated** — entity name constant, table name constant, column name constants? Default: all three if constants are used
- **Where constants are placed** — in the same class, or in a separate nested/inner class? Default: same class

Lombok conventions (score separately — only relevant if Lombok is on the classpath):

- **Lombok used?** — are any Lombok annotations present on entities? Default: yes
- **`@Getter` and `@Setter`** — is `@Getter`/`@Setter` on class level? Default: yes
- **`@Builder`** — is `@Builder` used? Default: no
- **`@AllArgsConstructor`** — is `@AllArgsConstructor` used? Default: no
- **`@NoArgsConstructor`** — is `@NoArgsConstructor` used? Default: no
- **`@ToString`** — is `@ToString` used? Default: no
- **`@ToString(onlyExplicitlyIncluded = true)`** — is this variant used? Default: no

equals & hashCode conventions (score separately):

- **equals/hashCode style** — check existing `equals`/`hashCode` implementations: is it manual with `HibernateProxy` check (proxy-safe pattern), or generated via Lombok `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` on specific fields (e.g. `id`)? Default: manual with HibernateProxy. If no `equals`/`hashCode` implementations are found in the project — confidence is high (90), use the default without asking.
- **`@EqualsAndHashCode` fields** — if Lombok is used, which fields are included via `@EqualsAndHashCode.Include`? Default: `id` only

toString conventions (score separately):

- **toString style** — check existing `toString()` implementations: is it manual (overridden `toString()` method in the class body), or generated via Lombok `@ToString`? Default: manual. If no `toString()` implementations are found in the project — confidence is high (90), use the default without asking.
- **toString fields** — if manual, which fields are included? Check that no related entity fields are accessed (would trigger lazy loading). Default: all local (non-relation) fields
- **`@ToString(onlyExplicitlyIncluded = true)`** — if Lombok `@ToString` is used, is `onlyExplicitlyIncluded = true` set with `@ToString.Include` on specific fields? Default: no

---

## Step 1.3: Collect uncertain conventions

Score confidence only for conventions where the code contains relevant examples but the pattern is ambiguous or inconsistent. If a convention is simply absent from the code (e.g. no `@ManyToOne` exists yet, no Lombok annotations anywhere) — confidence is high, use the default without asking.

Collect all conventions where confidence < 80. For each, formulate a question with explicit answer options; put the default value first (marked as "Recommended").

---

## Step 1.4: Ask developer

If there are any uncertain conventions from step 1.3, ask the developer using `AskUserQuestion` — combine all questions into a single tool call.

Example call — general uncertain convention + Lombok block:
```json
{
  "questions": [
    {
      "header": "Constraint case",
      "question": "What case is used for index and constraint names?",
      "multiSelect": false,
      "options": [
        { "label": "lower (Recommended)", "description": "e.g. idx_loan_due_date" },
        { "label": "UPPER", "description": "e.g. IDX_LOAN_DUE_DATE" }
      ]
    },
    {
      "header": "Lombok used?",
      "question": "Is Lombok used in entity classes?",
      "multiSelect": false,
      "options": [
        { "label": "Yes (Recommended)", "description": "Lombok annotations are present on entities" },
        { "label": "No", "description": "No Lombok — explicit getters/setters/constructors only" }
      ]
    },
    {
      "header": "Lombok features",
      "question": "Which Lombok annotations are used on entity classes?",
      "multiSelect": true,
      "options": [
        { "label": "@Getter and @Setter (Recommended)", "description": "Generate getters and setters for all fields" },
        { "label": "@Builder", "description": "Generate builder pattern" },
        { "label": "@AllArgsConstructor", "description": "Generate constructor with all fields" },
        { "label": "@NoArgsConstructor", "description": "Generate no-args constructor" }
      ]
    }
  ]
}
```

Note: ask about "Lombok features" only if "Lombok used?" was confirmed as Yes. Ask about "Constants what" and "Constants where" only if "Constants used?" was confirmed as Yes. Ask about "@EqualsAndHashCode fields" only if equals/hashCode style = Lombok. Ask about "toString fields" only if toString style = manual.

Example questions for toString:
```json
{
  "questions": [
    {
      "header": "toString style",
      "question": "How is toString() implemented in entity classes?",
      "multiSelect": false,
      "options": [
        { "label": "Manual (Recommended)", "description": "Overridden toString() method listing local fields only" },
        { "label": "Lombok @ToString", "description": "Generated via @ToString annotation" }
      ]
    },
    {
      "header": "toString fields",
      "question": "Which fields are included in toString()?",
      "multiSelect": false,
      "options": [
        { "label": "All local fields (Recommended)", "description": "All fields except relations (@ManyToOne, @OneToMany, etc.)" },
        { "label": "Explicit subset", "description": "Only specific fields marked with @ToString.Include or listed manually" }
      ]
    }
  ]
}
```

Example questions for equals & hashCode:
```json
{
  "questions": [
    {
      "header": "equals/hashCode style",
      "question": "How is equals/hashCode implemented in entity classes?",
      "multiSelect": false,
      "options": [
        { "label": "Manual with HibernateProxy (Recommended)", "description": "Proxy-safe pattern using HibernateProxy check in equals/hashCode" },
        { "label": "Lombok @EqualsAndHashCode", "description": "Generated via @EqualsAndHashCode(onlyExplicitlyIncluded = true) on selected fields" }
      ]
    },
    {
      "header": "@EqualsAndHashCode fields",
      "question": "Which fields are included in @EqualsAndHashCode?",
      "multiSelect": true,
      "options": [
        { "label": "id (Recommended)", "description": "Only the primary key field" },
        { "label": "business key fields", "description": "Natural key fields (e.g. email, code)" }
      ]
    }
  ]
}
```

Example questions for Constants Generation:
```json
{
  "questions": [
    {
      "header": "Constants used?",
      "question": "Are string constants generated for entity/table/column names?",
      "multiSelect": false,
      "options": [
        { "label": "No (Recommended)", "description": "No constants — names are inlined directly in annotations" },
        { "label": "Yes", "description": "e.g. public static final String TABLE_NAME = \"loans\"" }
      ]
    },
    {
      "header": "Constants what",
      "question": "Which name constants are generated?",
      "multiSelect": true,
      "options": [
        { "label": "Entity name", "description": "Constant for the entity class simple name" },
        { "label": "Table name", "description": "Constant for the @Table name value" },
        { "label": "Column names", "description": "Constant per each @Column name value" }
      ]
    },
    {
      "header": "Constants where",
      "question": "Where are name constants placed?",
      "multiSelect": false,
      "options": [
        { "label": "Same class (Recommended)", "description": "Constants declared directly in the entity class" },
        { "label": "Nested class", "description": "Constants declared in a static nested class inside the entity" },
        { "label": "Separate class", "description": "Constants declared in a dedicated companion class" }
      ]
    }
  ]
}
```

---

## Step 1.5: Summarize resolved conventions

Before writing any code, output a single consolidated list of **all conventions from Step 1.2**, with each value filled in from what you actually found in the code or what the developer confirmed in Step 1.4. Do not copy the example below — construct your own list based on the real project.

Every convention from Step 1.2 must appear in the list, including Lombok and Constants Generation items. Each value must reflect the actual project state, not the defaults.

**Example format** (values here are illustrative only — replace with what you discovered):
```
- Annotation placement: on fields
- serialVersionUID: not generated
- FetchType on @ManyToOne / @OneToOne: LAZY
- Fluent setters: void
- Field access modifier: private
- Naming strategy: explicit
- Table name template: lower case, underscore, no prefix/postfix, not pluralized
- Column name template: lower case, underscore, no prefix/postfix
- Entity class name convention: as-is
- Index/constraint name case: lower
- Constants generated: no
- Lombok used: yes — @Getter, @Setter
- equals/hashCode style: manual with HibernateProxy
- toString style: manual — all local fields
```

This list is your working contract for all code written in this session.