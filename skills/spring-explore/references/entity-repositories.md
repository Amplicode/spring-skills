# Entity Repositories

Finds Spring Data repositories associated with a given JPA entity.

---

## Steps

For each entity you need to find repositories for:
- Call `list_entity_repositories` via MCP to get all repositories linked to the entity.

From the result collect:
- Repository class FQN
- Supported query methods (if returned)

---

## Output format

```
Owner
  OwnerRepository — ru.example.owner.OwnerRepository

Pet
  PetRepository — ru.example.owner.PetRepository
```

If no repository is found for an entity, note it as a gap — the entity may lack a repository
or use a non-standard persistence approach.