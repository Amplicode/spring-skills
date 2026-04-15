# MapStruct Mapper -- Kotlin

## Skeleton
Read `examples/_skeletons/mapstruct-kotlin.md`. Write the file to `src/main/kotlin/{packagePath}/{className}.kt`.

## Generation order

1. Create file from skeleton
2. Add `@Mapper` annotation from `examples/_fragments/mapper-annotation/kotlin.md`
   - Determine componentModel: any `spring-boot-starter*` or `spring-context` in `presentDeps` → SPRING; otherwise → DEFAULT.
   - CDI is **not** auto-detected. Use the CDI variant only when the user explicitly asked for `componentModel = "cdi"`.
3. If componentModel = DEFAULT: add MAPPER companion object from `examples/_fragments/factory-field/kotlin.md`
4. If user selected parent interface: modify declaration from `examples/_fragments/parent-interface/kotlin.md`
5. Add toEntity method from `examples/_fragments/to-entity-method/kotlin.md`
   - Multiple `@Mapping` annotations MUST be wrapped in `@Mappings(value = [...])`
   - For each DTO field that maps to a different entity field, add `Mapping(source, target)` inside `@Mappings`
6. Add toDto method from `examples/_fragments/to-dto-method/kotlin.md`
   - If toEntity was created and has `@Mapping` annotations, use `@InheritInverseConfiguration(name = "toEntity")`
   - Otherwise add individual `@Mapping`/`@Mappings` annotations
   - If entity has AggregateReference attrs with non-ID sub-DTO, add `@Context` parameter
7. If PARTIAL_UPDATE requested: add from `examples/_fragments/partial-update-method/kotlin.md`
8. If UPDATE_WITH_NULL_VALUES requested: add from `examples/_fragments/full-update-method/kotlin.md`
9. Check entity associations for @AfterMapping need:
   - For each non-owner OneToOne or OneToMany with `mappedBy` where DTO uses sub-DTO:
   - Add method from `examples/_fragments/after-mapping/kotlin.md`
10. If entity has AggregateReference attrs with ID sub-DTO:
    - Add `mapToAggregateReference` from `examples/_fragments/aggregate-ref-to/kotlin.md`
    - Add `mapFromAggregateReference` from `examples/_fragments/aggregate-ref-from/kotlin.md`
11. If DTO has flat collection attributes (SubDtoType = FLAT + collection):
    - Add flat expression function from `examples/_fragments/flat-expression/kotlin.md`
12. If entity has AggregateReference attrs with non-ID sub-DTO:
    - Add context data class + helper methods from `examples/_fragments/context-class/kotlin.md`
13. Add sub-mapper references to `@Mapper(uses = ...)` from `examples/_fragments/uses-attribute/kotlin.md`
    - **Lazy fetch:** for each association entity used by the DTO via a sub-DTO type (NEW_CLASS / NEW_NESTED_CLASS / EXIST_CLASS), call `list_entity_mappers(associationEntityFqn)` **at this step** — not in Step 1 of `SKILL.md`. The call needs the association entity FQN, which is only known after `entityDetails` is fetched in Step 2.
    - Add found mapper FQNs to the `uses` attribute. Skip associations with no existing mapper. If no association needs a sub-mapper, skip the `uses` attribute entirely.

## Kotlin-specific differences from Java

- Mapper is `abstract class`, not `interface`
- Methods are `abstract fun`, not interface abstract methods
- Helper methods (afterMapping, aggregateRef) are regular functions, not `default` methods
- Multiple `@Mapping` annotations MUST be wrapped: `@Mappings(value = [Mapping(...), Mapping(...)])`
- MAPPER factory uses `companion object` with `@JvmStatic`
- Parent interface uses `: ParentFqn<Dto, Entity>()` (with `()`)
- Custom extension functions use `this.field` access
- `@MappingTarget` goes before param name: `@MappingTarget entity: EntityFqn`

## @Mapping annotation rules

Same as Java (see `references/mapstruct-java.md`), but annotations wrapped in `@Mappings(value = [...])` when multiple.

## Method naming

Same as Java (see `references/mapstruct-java.md`).
