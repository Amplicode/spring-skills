# Entity Description (Spring Data JDBC)

Gets detailed information about one or more Spring Data JDBC entities: fields, annotations,
aggregate structure, cross-aggregate references, and parent class.

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

For each entity, call `get_jdbc_entity_details` via MCP:

```
get_jdbc_entity_details projectPath=<PATH> entityFqn=com.example.Pet
```

Collect from the result:
- Fields: name, type, column name, nullable, annotations (`@Id`, `@Column`, `@Embedded`)
- Owned children: fields annotated `@MappedCollection(idColumn=...)` — part of the same aggregate
- Cross-aggregate links: fields typed `AggregateReference<Target, IdType>` — point at another aggregate root by id
- Aggregate metadata: `aggregateRootFqn` (null when the entity *is* the root), `aggregates` (recursive owned children tree), `referencedBy` (other aggregates linking here via `AggregateReference`)
- Parent class (if any)

Repeat for each entity that needs to be described.

---

## Output format

```
**Pet**
- id: Long [@Id]
- name: String [@Column("name"), not null]
- birthDate: LocalDate [@Column("birth_date")]
- typeId: AggregateReference<PetType, Long>
- visits: Set<Visit> [@MappedCollection(idColumn="pet_id")]

Aggregate:
- root: true
- referencedBy: Owner.pets
```
