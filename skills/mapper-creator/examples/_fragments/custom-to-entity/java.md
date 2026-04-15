# Custom toEntity method (Java)

## Insert Point
Static method in custom mapper class body.

## Code

```defaults
Always generated for Custom mapper.
```

### Standard form

Extract every DTO value into a local first, then construct
the entity and apply setters. This mirrors `custom-to-dto` and is the
canonical style. **Use this form by default.**

```java
public static {entityClassFqn} {methodName}({dtoClassFqn} {dtoParamName}) {
    {dtoFieldType1} {dtoParamName}{dtoField1Cap} = {dtoParamName}.{dtoField1Accessor};
    {dtoFieldType2} {dtoParamName}{dtoField2Cap} = {dtoParamName}.{dtoField2Accessor};
    {entityClassFqn} {entityParamName} = new {entityClassFqn}();
    {entityParamName}.set{field1Cap}({dtoParamName}{dtoField1Cap});
    {entityParamName}.set{field2Cap}({dtoParamName}{dtoField2Cap});
    return {entityParamName};
}
```

### `{dtoFieldNAccessor}` — record vs class

- DTO is a regular **class** → use the getter: `getName()`, `getTypeId()`, …
- DTO is a Java **record** → use the **record component accessor**, no
  `get` prefix: `name()`, `typeId()`, …

Use the bare-component form for records and the
`getX()` form for classes. The choice is determined statically
from the DTO declaration: a `public record PetDto(...)` always uses the
component-accessor form.

### Flat ToOne — JPA stub pattern

When the DTO contains **flat fields** from a ToOne association (e.g. only
the id, or id+name extracted from `Pet.type`), do NOT call a repository or
fetch the association. Build a **stub** of the association entity, set
ONLY the flat fields, and assign it. JPA treats the stub as a known-id
reference; loading the real row is the persistence layer's job.

```java
public static Pet toEntity(PetDto petDto) {
    String petDtoName = petDto.getName();
    PetType petType = new PetType();
    petType.setId(petDto.getTypeId());
    Pet pet = new Pet();
    pet.setName(petDtoName);
    pet.setType(petType);
    return pet;
}
```

If multiple flat fields come from the same association (`typeId` and
`typeName`), set them on the same stub:

```java
PetType petType = new PetType();
petType.setId(petDto.getTypeId());
petType.setName(petDto.getTypeName());
pet.setType(petType);
```

### Java Record + flat ToOne (combined)

```java
public static Pet toEntity(PetDto petDto) {
    String petDtoName = petDto.name();
    PetType petType = new PetType();
    petType.setId(petDto.typeId());
    Pet pet = new Pet();
    pet.setName(petDtoName);
    pet.setType(petType);
    return pet;
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{methodName}` | naming pattern | `toEntity` |
| `{dtoParamName}` | decapitalized DTO short name | e.g. `petDto` |
| `{entityParamName}` | decapitalized entity short name | e.g. `pet` |
| `{dtoClassFqn}` | DTO class FQN | — |
| `{entityClassFqn}` | entity class FQN | — |
| `{fieldNCap}` | capitalized entity field names for setter | — |
| `{dtoFieldNCap}` | capitalized DTO field names (used in local var name) | — |
| `{dtoFieldNAccessor}` | `getX()` for class DTO, `x()` for record DTO | — |
| `{dtoFieldTypeN}` | DTO field types | — |
