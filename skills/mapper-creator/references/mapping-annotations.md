# @Mapping Annotation Rules

## Determining field mappings

Compare entity fields (from `get_entity_details`) with DTO fields (from `list_class_members`).

For each DTO field, determine the mapping type:

### Direct match
Same name and compatible type in both entity and DTO. No `@Mapping` annotation needed.

### Different name
DTO field name differs from entity field name.
```java
@org.mapstruct.Mapping(source = "{sourceField}", target = "{targetField}")
```

### SubDtoType = ID (association mapped to ID only)
DTO has an ID field for an entity association (e.g. `Long customerId` for `Customer customer`).
```java
@org.mapstruct.Mapping(source = "{dtoIdField}", target = "{entityAssocField}.id")
```
For toDto (reverse): `@Mapping(source = "{entityAssocField}.id", target = "{dtoIdField}")`

### SubDtoType = FLAT (flattened association fields)
DTO has individual fields extracted from a nested entity (e.g. `String customerName` from `customer.name`).
- For **non-collection (ToOne)**: dot-notation in BOTH directions. Note that on `toEntity` the dot is on the **target** side, on `toDto` it's on the **source** side. MapStruct synthesizes the intermediate `new {Assoc}()` itself.
  - toEntity: `@Mapping(source = "{dtoFlatField}", target = "{entityAssocField}.{nestedField}")`
  - toDto: `@Mapping(source = "{entityAssocField}.{nestedField}", target = "{dtoFlatField}")` — or use `@InheritInverseConfiguration` (see below) when there are 2+ flat mappings.
- For **collection (ToMany)**: uses `expression` with helper method (see flat-expression fragments). Dot-notation does not work for collections.

### SubDtoType = NEW_CLASS / NEW_NESTED_CLASS / EXIST_CLASS
DTO uses another DTO for the association. **MapStruct handles this implicitly** by generating private nested-mapping methods inside the same mapper interface — no `uses=` and no separate sub-mapper file are needed by default. Do not emit `uses` for these cases — mapping will be handled implicitly. Add `uses` only when a real sibling mapper must be reused or a `@Named` qualifier is required.

## @InheritInverseConfiguration — opt-in only, NOT the default

When toEntity is generated first (always true by default), it is technically
possible for toDto to use:
```java
@org.mapstruct.InheritInverseConfiguration(name = "toEntity")
```
This inherits all `@Mapping` annotations in reverse, avoiding duplication.

**However, this is NOT the default in this skill.** Reasons to prefer
duplicated `@Mapping(source, target)` pairs on toDto:

1. **Robust under refactoring.** `@InheritInverseConfiguration` silently
   breaks (or starts producing wrong code) the moment `toEntity` gains any
   non-invertible mapping: `ignore = true`, `expression = "..."`,
   `constant = "..."`, `defaultValue`, `qualifiedBy`, `dateFormat`,
   `numberFormat`. None of these have a meaningful inverse.
2. **Locally readable.** A reviewer reading toDto sees its mappings inline;
   they do not have to scroll up to toEntity and mentally invert each line.
3. **One fewer import** (`org.mapstruct.InheritInverseConfiguration`).
4. **Order-independent.** If a future edit reorders steps 5 and 6 of the
   generation order, the duplicated `@Mapping` form keeps working;
   `@InheritInverseConfiguration(name = "toEntity")` would resolve to a
   forward reference.

**Exception — multi-flat-ToOne case.** When
`toEntity` carries **2+** plain `@Mapping(source, target)` annotations
(typically the dot-notation flat-ToOne case from
`to-entity-method/java.md`) and ALL of them are losslessly invertible,
prefer `@InheritInverseConfiguration(name = "toEntity")` on `toDto`.
Worked example:

```java
@Mapping(source = "typeName", target = "type.name")
@Mapping(source = "typeId", target = "type.id")
Pet toEntity(PetDto petDto);

@InheritInverseConfiguration(name = "toEntity")
PetDto toPetDto(Pet pet);
```

For the single-`@Mapping` case the duplication is so cheap that either
form is fine — default to duplication for refactor-safety. For 2+ mappings,
default to inheritance to reduce duplication.

## partialUpdate has its OWN @BeanMapping — no @InheritConfiguration

The `partialUpdate` method is generated independently and carries exactly
ONE annotation:

```java
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.{strategy})
{Entity} partialUpdate({Dto} dto, @MappingTarget {Entity} entity);
```

`{strategy}` is one of `SET_TO_NULL` (default) / `IGNORE` /
`SET_TO_DEFAULT`, picked by the user. **Do not** add
`@InheritConfiguration(name = "toEntity")` —
chasing inherited mappings here only creates surprising failures when
`toEntity` later acquires non-invertible mappings.

## Kotlin wrapping rule

In Kotlin, multiple `@Mapping` annotations must be wrapped:
```kotlin
@org.mapstruct.Mappings(value = [
    org.mapstruct.Mapping(source = "field1", target = "field2"),
    org.mapstruct.Mapping(source = "field3", target = "field4")
])
```
Single `@Mapping` can be used directly without wrapping.
