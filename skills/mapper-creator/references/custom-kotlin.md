# Custom Mapper -- Kotlin

## Skeleton
Read `examples/_skeletons/custom-kotlin.md`. Write the file to `src/main/kotlin/{packagePath}/{className}.kt`.

Note: Kotlin custom mapper is a **file with extension functions**, not a class.

## Generation order

1. Create file from skeleton (just package declaration)
2. Add toDto extension function from `examples/_fragments/custom-to-dto/kotlin.md`
   - Extension on entity class
   - Returns DTO via named constructor arguments: `DtoFqn(field1 = this.field1, field2 = this.field2)`
3. Add toEntity extension function from `examples/_fragments/custom-to-entity/kotlin.md`
   - Extension on DTO class
   - Returns `EntityFqn().also { it.field1 = this.field1; ... }`
4. If UPDATE_WITH_NULL_VALUES requested: add from `examples/_fragments/custom-update-with-null/kotlin.md`
   - Extension on entity class with DTO parameter
   - Uses `apply { field1 = dtoParam.field1; ... }`

## Kotlin-specific patterns

- **toDto**: `fun EntityFqn.toOrderDto() = DtoFqn(field1 = this.field1, field2 = this.field2)`
- **toEntity**: `fun DtoFqn.toEntity() = EntityFqn().also { it.field1 = this.field1; it.field2 = this.field2 }`
- **updateWithNull**: `fun EntityFqn.updateWithNull(orderDto: DtoFqn) = apply { field1 = orderDto.field1; ... }`

## Method naming

Same defaults as MapStruct variant (see `references/mapstruct-java.md`).
