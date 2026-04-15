# MapStruct Mapper -- Java

## Skeleton
Read `examples/_skeletons/mapstruct-java.md`. Write the file to `src/main/java/{packagePath}/{className}.java`.

## Generation order

1. Create file from skeleton
2. Add `@Mapper` annotation from `examples/_fragments/mapper-annotation/java.md`
   - Determine componentModel: any `spring-boot-starter*` or `spring-context` in `presentDeps` → SPRING; otherwise → DEFAULT.
   - CDI is **not** auto-detected. Use the CDI variant only when the user explicitly asked for `componentModel = "cdi"`.
   - For SPRING, use `MappingConstants.ComponentModel.SPRING` form (modern MapStruct).
3. If componentModel = DEFAULT: add MAPPER factory field from `examples/_fragments/factory-field/java.md`
4. If user selected parent interface: modify declaration from `examples/_fragments/parent-interface/java.md`
5. Add toEntity method from `examples/_fragments/to-entity-method/java.md`
   - For each DTO field that maps to a different entity field name, add `@Mapping(source, target)` annotation
   - If DTO field has SubDtoType = ID (association ID only), add `@Mapping(source = "dtoField", target = "entityAssoc.id")`
   - If DTO field has SubDtoType = FLAT for a **ToOne** association (single object, e.g. `typeId`/`typeName` from `Pet.type`):
     emit one `@Mapping(source = "flatDtoField", target = "assoc.nestedField")` per flat field — **dot-notation, no helper method, no Java-expression**. MapStruct synthesizes the intermediate `new PetType()` and the setters itself.
   - If DTO field has SubDtoType = FLAT for a **ToMany** association (collection), use the helper-method form from `examples/_fragments/flat-expression/java.md` (collections cannot use dot-notation).
6. Add toDto method from `examples/_fragments/to-dto-method/java.md`
   - **Single `@Mapping`** on `toEntity` → emit the symmetric `@Mapping(source, target)` on `toDto`. Duplication is one cheap line and refactor-safe.
   - **Two or more invertible `@Mapping`s** on `toEntity` (the typical multi-flat-ToOne case) → emit `@InheritInverseConfiguration(name = "{toEntityMethodName}")` on `toDto`. This removes the duplication that grows linearly with flat fields.
   - **Mixed invertible / non-invertible mappings** on `toEntity` (any `expression`, `constant`, `defaultValue`, `ignore = true`, `qualifiedBy`, condition) → fall back to explicit duplicated `@Mapping`s on `toDto`. `@InheritInverseConfiguration` silently breaks in this case.
   - If entity has AggregateReference attrs with non-ID sub-DTO, add `@Context` parameter
7. If PARTIAL_UPDATE requested: add from `examples/_fragments/partial-update-method/java.md`
   - Pass `{strategyEnum}` from the user's `partialUpdateNullStrategy` answer (default `SET_TO_NULL`).
   - Always emit ONE method with one `@BeanMapping(nullValuePropertyMappingStrategy = ...)` — never two methods, never `@InheritConfiguration`.
8. If UPDATE_WITH_NULL_VALUES requested: add from `examples/_fragments/full-update-method/java.md`
9. Check entity associations for @AfterMapping need:
   - For each non-owner OneToOne or OneToMany with `mappedBy` where DTO uses sub-DTO (not ID, not FLAT):
   - Add method from `examples/_fragments/after-mapping/java.md`
10. If entity has AggregateReference attrs with ID sub-DTO:
    - Add `mapToAggregateReference` from `examples/_fragments/aggregate-ref-to/java.md`
    - Add `mapFromAggregateReference` from `examples/_fragments/aggregate-ref-from/java.md`
11. If DTO has flat collection attributes (SubDtoType = FLAT + collection):
    - Add flat expression method from `examples/_fragments/flat-expression/java.md`
    - The toDto method gets `@Mapping(target = "flatField", expression = "java(functionName(entity.getAssoc()))")` instead of regular mapping
    - **Known limitation — one-way only.** The flat collection helper is
      generated only in the entity → DTO direction. The reverse
      (`flatField` → `entityAssoc`) is **not** generated, and
      `unmappedTargetPolicy = IGNORE` will silently leave the collection
      empty on `toEntity`. Loading entities by id is the responsibility of
      the calling service via the JPA repository — do **not** inject a
      repository or `EntityManager` into the mapper. If the user needs the
      reverse, point them at the service layer instead of trying to
      generate it.
12. If entity has AggregateReference attrs with non-ID sub-DTO:
    - Add context record + helper methods from `examples/_fragments/context-class/java.md`
13. **`uses = {...}` is rarely needed.** MapStruct generates private nested-mapping methods **implicitly** when both the source and target sub-types are visible to the same mapper interface. Do **not** emit `uses` for NEW_CLASS / NEW_NESTED_CLASS sub-DTOs — mapping will be handled implicitly.
    - **Default: do not add `uses`.** Do not call `list_entity_mappers` and do not generate a separate sub-mapper file for the association.
    - **Exceptions** that DO warrant `uses = {SomeMapper.class}`:
      - the user explicitly asks for an existing sibling mapper to be reused (e.g. complex per-association logic already lives there);
      - the association mapping needs a `@Named` qualifier from another mapper;
      - circular dependencies between mappers that you cannot resolve via implicit generation.
    - When you DO need it, use `examples/_fragments/uses-attribute/java.md`.

## @Mapping annotation rules

For each DTO field, determine mapping type based on entity-DTO field comparison:
- **Direct match** (same name and type): no `@Mapping` needed
- **Different name**: `@Mapping(source = "dtoFieldName", target = "entityFieldName")`
- **SubDtoType = ID**: `@Mapping(source = "dtoField", target = "entityAssoc.id")`
- **SubDtoType = FLAT**: `@Mapping(source = "dtoField", target = "assoc.nestedField")` or expression for collections
- **SubDtoType = NEW_CLASS/NEW_NESTED_CLASS/EXIST_CLASS**: MapStruct handles via sub-mapper in `uses`

## Method naming

Default patterns:
- toEntity: `"toEntity"`
- toDto: `"to{DtoShortName}"` (e.g. `toOrderDto`)
- partialUpdate: `"partialUpdate"`
- updateWithNull: `"updateWithNull"`
- Collection methods: same as single (LIKE_SINGLE strategy)
