# Deep Domain Model (JPA)

Traverses JPA entity relationships to a given depth and returns the domain model structure:
entity names, fields, and how they relate to each other.

**Prerequisite:** the project must use JPA (`@Entity`, `@OneToMany`, `@ManyToOne`, etc.).

Use this reference when you need to understand the shape of the domain model beyond a flat list
of entities — for example, to know which fields an entity has, what it references, and how deep
the graph goes.

---

## Step 0 — Resolve entity FQN (if unknown)

If you only know the simple class name (e.g. `Order`) but not the fully qualified name, resolve it first:
- Call `list_all_domain_entities` via MCP with `regexPattern=<SimpleName>` to find the FQN.

```
list_all_domain_entities projectPath=<PATH> regexPattern=Order
```

Use the `qualifiedName` from the result in all subsequent steps.
Skip this step if the FQN is already known.

---

## Step 1 — Collect entity details

Start from the entities identified in the main exploration (step 2 of the skill).

For each entity:
- Call `get_entity_details` via MCP to get its fields and `relationships` array.
- From `relationships`, collect all `targetEntity` values — these are the next level of entities.
- Repeat for each discovered entity until the desired depth is reached or no new entities appear.

Collect per relationship:
- `relationshipType` — `@OneToMany`, `@ManyToOne`, `@ManyToMany`, etc.
- `targetEntity` — the class on the other end
- `fetchType` — `LAZY` or `EAGER`
- `cascadeTypes` — list of cascade operations

---

## Step 2 — Build the entity graph

Lay out all collected entities as a tree, indented by traversal depth. For each entity show
its key fields and outgoing relationships.

```
Owner
  fields: firstName, lastName, address, telephone
  pets: Pet [@OneToMany, LAZY]
    fields: name, birthDate
    type: PetType [@ManyToOne, EAGER]
      fields: name
    visits: Visit [@OneToMany, cascade=ALL]
      fields: date, description

Vet
  fields: firstName, lastName
  specialties: Specialty [@ManyToMany]
    fields: name
```

Stop expanding a branch when:
- The target entity was already expanded at a higher level (avoid cycles)
- The desired depth limit is reached

---

## Output format

Present the graph as an indented tree. For each entity show:
- Entity name
- Key scalar fields (skip technical fields like `id`, `version`, `createdAt` unless relevant)
- Each relationship on its own line: `fieldName: TargetEntity [type, fetchType, cascade if any]`

Keep it readable — the goal is a quick structural overview, not a full schema dump.