# Custom toDto method (Java)

## Insert Point
Static method in custom mapper class body.

## Code

```defaults
Always generated for Custom mapper.
```

### Standard form

Extract every **direct** entity property into a local first,
then call the DTO constructor with the locals (and any chained accessors)
in positional order.

```java
public static {dtoClassFqn} {methodName}({entityClassFqn} {entityParamName}) {
    {dtoFieldType1} {entityParamName}{field1Cap} = {entityParamName}.get{field1Cap}();
    {dtoFieldType2} {entityParamName}{field2Cap} = {entityParamName}.get{field2Cap}();
    {dtoClassFqn} {dtoParamName} = new {dtoClassFqn}(
        {entityParamName}{field1Cap},
        {entityParamName}{field2Cap}
    );
    return {dtoParamName};
}
```

### Local-variable rule

For **direct** property access (`entity.getName()`) extract a local. For
**chained** access through a flat ToOne association
(`entity.getType().getId()`) inline the expression directly into the
constructor call — do **not** introduce a `petType.id` local.
Example with one direct field and one flat field:

```java
public static PetDto toPetDto(Pet pet) {
    String petName = pet.getName();
    PetDto petDto = new PetDto(petName, pet.getType().getId());
    return petDto;
}
```

If you would rather always extract locals (slightly more uniform, slightly
more lines) that is functionally identical and acceptable — but the canonical
form inlines the chained call.

### Java Record DTO

The `toDto` side does **not** depend on whether the DTO is a record vs
class — both are constructed via the canonical constructor. The same
extract-locals-then-construct shape works unchanged. (Records change only
the `toEntity` side, where DTO values are read via component accessors —
see `custom-to-entity/java.md`.)

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{methodName}` | naming pattern `to${DTO_NAME}` | e.g. `toOrderDto`, `toPetDto` |
| `{entityParamName}` | decapitalized entity short name | e.g. `pet` |
| `{dtoParamName}` | decapitalized DTO short name | e.g. `petDto` |
| `{dtoClassFqn}` | DTO class FQN | — |
| `{entityClassFqn}` | entity class FQN | — |
| `{fieldNCap}` | capitalized field names for getter calls | — |
| `{dtoFieldTypeN}` | DTO field types | — |
