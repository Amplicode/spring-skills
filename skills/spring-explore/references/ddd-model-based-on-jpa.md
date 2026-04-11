# DDD Aggregate Model Analyser (JPA)

This skill inspects **JPA** entity relationships and returns the domain model in DDD terms:
which entities are **aggregate roots**, which are **members**, and the **ownership chain**
from root to each member.

**Prerequisite:** the project must use JPA (`@Entity`, `@OneToMany`, `@ManyToOne`, etc.).
For non-JPA persistence (Spring Data JDBC, MongoDB, R2DBC, etc.) this skill does not apply —
aggregate boundaries must be determined by other means.

The output can be used wherever aggregate boundaries matter: designing REST URLs, shaping DTOs,
planning cascade strategies, writing queries, or simply understanding the domain model.

---

## Step 0 — Resolve entity FQN (if unknown)

If you only know the simple class name (e.g. `Owner`) but not the fully qualified name, resolve it first:
- Call `list_all_domain_entities` via MCP with `regexPattern=<SimpleName>` to find the FQN.

```
list_all_domain_entities projectPath=<PATH> regexPattern=Owner
```

Use the `qualifiedName` from the result in all subsequent steps.
Skip this step if the FQN is already known.

---

## Step 1 — Collect entity details

For each entity relevant to the task, call `get_entity_details` (MCP tool) and collect the
`relationships` array. You need these fields per relationship:

- `cascadeTypes` — list of cascade operations declared on the association
- `fetchType` — `LAZY` or `EAGER`
- `targetEntity` — the class on the other end of the relationship

For each entity relevant to the task:
- Call `list_all_domain_entities` via MCP to find entity qualified names (filter with `regexPattern` if needed).
- Call `get_entity_details` via MCP to get its `relationships` array with `cascadeTypes`, `fetchType`, and `targetEntity`.
- Repeat `get_entity_details` for each related entity that appears in the relationships.

---

## Step 2 — Classify each relationship

For every relationship found in step 1, apply this rule:

| Condition | Classification |
|---|---|
| `cascadeTypes` contains `ALL` | **Member** — lifecycle fully owned by the parent |
| `fetchType` is `EAGER` (and no `cascade=ALL`) | **Member** — always loaded as part of the parent |
| Neither condition holds | **Independent aggregate root** |

The rule reflects DDD aggregate semantics: a member entity cannot exist or be accessed
meaningfully without its root. `cascade=ALL` means the root controls creation and deletion
(the clearest signal). `EAGER` without cascade means the root always loads it — tight enough
coupling to treat it as a member.

`@ManyToMany` without `cascade=ALL` is almost always an **independent root** — the junction
is just a cross-reference, not ownership.

---

## Step 3 — Build the aggregate tree

Starting from each entity that is referenced by no other entity as a member, mark it as an
**aggregate root**. Then walk its relationships transitively: members of members are also
members of the same root.

Build a tree using the output format defined below. At this step, cross-refs are not yet marked —
just establish roots and members:

```
Owner     [root]
  Pet     [member]
    Visit [member]

Vet       [root]   ← Specialty is @ManyToMany without cascade=ALL → independent root, not a member

Specialty [root]
PetType   [root]
```

---

## Step 4 — Classify cross-aggregate references

Any relationship that crosses aggregate boundaries (i.e. the target is an independent root)
is a **cross-aggregate reference**. Add `→ ref:` lines to the tree from step 3:

```
Owner                  [root]
  Pet                  [member]
    → ref: PetType     [independent root, DTO: typeId]
    Visit              [member]
```

Cross-aggregate references matter for consumers of this output:
- In DTOs: carry only the **ID** of the referenced root, never the full nested object
- In cascades: do not cascade across aggregate boundaries

---

## Output format

Return a single aggregate tree. Each aggregate root is a top-level entry; its members are
indented beneath it. Cross-aggregate references are shown inline with `→ ref:`.

```
Owner                          [root]
  Pet                          [member]
    → ref: PetType             [independent root, DTO: typeId]
    Visit                      [member]

Vet                            [root]
  → ref: Specialty             [independent root, @ManyToMany, DTO: specialtyIds[]]

Specialty                      [root]
PetType                        [root]
```

One tree — no separate lists. Everything visible at a glance:
- indentation = ownership depth
- `[root]` = independent aggregate root
- `[member]` = owned by the parent above
- `→ ref:` = cross-aggregate reference (carry ID only in DTOs)

---

## Common mistakes to avoid

**Mistake: treating a @ManyToMany target as a member**
`Vet.specialties` is `@ManyToMany` — no `cascade=ALL`. `Specialty` is an independent root,
not a member of `Vet`.

**Mistake: promoting a cascade=ALL child to its own root**
`Pet.visits` is `@OneToMany(cascade=ALL)` — `Visit` is a member of `Owner` (via `Pet`),
not an independent root.

**Mistake: treating LAZY fetch without cascade as independent**
Check cascade first. If `cascade=ALL` is present, the entity is a member regardless of fetch type.
