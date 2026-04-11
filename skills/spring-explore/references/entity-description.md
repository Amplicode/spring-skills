# Entity Description

Gets detailed information about one or more JPA entities: fields, annotations, relationships,
and parent class.

---

## Step 0 — Resolve entity FQN (if unknown)

If you only know the simple class name (e.g. `Pet`) but not the fully qualified name, resolve it first:
- Call `list_all_domain_entities` via MCP with `regexPattern=<SimpleName>`.

```
list_all_domain_entities projectPath=<PATH> regexPattern=Pet
```

Use the `qualifiedName` from the result in the next step.
Skip this step if the FQN is already known.

---

## Step 1 — Get entity details

For each entity, call `get_entity_details` via MCP:

```
get_entity_details projectPath=<PATH> entityFqn=com.example.Pet
```

Collect from the result:
- Fields: name, type, column name, nullable, annotations
- Relationships: type (`@OneToMany`, `@ManyToOne`, etc.), `targetEntity`, `fetchType`, `cascadeTypes`
- Parent class (if any)

Repeat for each entity that needs to be described.

---

## Output format

```
**Pet**
- id: Long [@Id]
- name: String [@Column(name="name"), not null]
- birthDate: LocalDate [@Column(name="birth_date")]
- type: PetType [@ManyToOne, EAGER]
- visits: Set<Visit> [@OneToMany(cascade=ALL), LAZY]
- owner: Owner [@ManyToOne, LAZY]
```