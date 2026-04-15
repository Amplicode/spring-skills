# Custom Mapper -- Java

## Skeleton
Read `examples/_skeletons/custom-java.md`. Write the file to `src/main/java/{packagePath}/{className}.java`.

## Generation order

1. Create file from skeleton (plain class)
2. Add toDto static method from `examples/_fragments/custom-to-dto/java.md`
   - For each DTO field: extract value from entity via getter into a local variable
   - Create DTO via constructor call with all extracted variables
   - Return the DTO
3. Add toEntity static method from `examples/_fragments/custom-to-entity/java.md`
   - For each direct DTO field: extract value into a local using the DTO accessor (`getX()` for class DTOs, `x()` for record DTOs)
   - Create new entity instance
   - Call entity setters with the locals
   - For flat ToOne fields: build a JPA stub of the association entity (`new PetType(); petType.setId(...);`) and pass it to the parent setter
   - Return the entity
4. If UPDATE_WITH_NULL_VALUES requested: add from `examples/_fragments/custom-update-with-null/java.md`
   - Takes both DTO and entity as parameters
   - Calls entity setters with DTO getter values
   - Returns the entity

## Method body construction

**toDto method:**
- Init expressions: `{DtoFieldType} {entityParam}{FieldCap} = {entityParam}.get{FieldCap}();` for each DTO field
- Result: `new {DtoClassFqn}({entityParam}{Field1Cap}, {entityParam}{Field2Cap}, ...)`

**toEntity method:**
- For each direct DTO field: `{DtoFieldType} {dtoParam}{DtoFieldCap} = {dtoParam}.{accessor};`
  - `{accessor}` = `get{DtoFieldCap}()` for class DTO, `{dtoFieldName}()` for record DTO
- Init: `{EntityClassFqn} {entityParam} = new {EntityClassFqn}();`
- For each direct field: `{entityParam}.set{FieldCap}({dtoParam}{DtoFieldCap});`
- For each flat ToOne assoc with fields {f1, f2, ...} from `{Assoc}`:
  - `{Assoc} {assocVar} = new {Assoc}();`
  - `{assocVar}.set{F1Cap}({dtoParam}.{f1Accessor});`
  - `{assocVar}.set{F2Cap}({dtoParam}.{f2Accessor});`
  - `{entityParam}.set{AssocCap}({assocVar});`
- Return: `{entityParam}`

**updateWithNull method:**
- Body: same setter pattern as toEntity, but entity is a parameter (not new)
- Return: `{entityParam}`

## Method naming

Same defaults as MapStruct variant (see `references/mapstruct-java.md`).
