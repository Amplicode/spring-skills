# Java Record DTO

## Conditions
- language = JAVA
- isJavaRecord = true
- JDK >= 16

## Generation Order

1. **Skeleton:** Read `examples/_skeletons/java/record.md` -- create file
2. **Class Javadoc:** Read `examples/_fragments/javadoc/java/javadoc.md`
   ("Top-level Java record" variant) -- insert immediately above the
   `public record {className}(...) {` line. Always generated.
   **Uses short name + import** (same as the top-level class form) — there
   is no special FQN form for records.
3. **Record components:** Read `examples/_fragments/fields/java-record.md` -- replace empty `()` with components.
   **Validators are inlined onto each component parameter**, not on
   separate field declarations: `@NotBlank String firstName,
   @Pattern(regexp="…") @NotBlank String telephone, …`. Multiple validators
   on the same component are space-separated.
4. **@JsonIgnoreProperties** (when jsonIgnoreUnknownProperties=true AND Jackson on classpath): Read `examples/_fragments/json-ignore/java/json-ignore.md` -- add annotation on record
5. **Serializable** (when serializableType != NoSerializable): Read `examples/_fragments/serializable/java/serializable.md` -- modify record declaration (use `implements`)
6. **Nested records** (for each attribute with subDtoType=NEW_NESTED_CLASS):
   Read `examples/_fragments/nested-class/java/nested-class.md`, "Java record"
   variant. The skill MUST emit a `public record {SubDtoName}(...) { }`
   (NOT a `public static class`, NOT a separate file). Falling back to
   `NEW_CLASS` because the record form looks underspecified is a bug —
   the record form is fully specified in the fragment.

   **Recursive component generation for the nested record:**
   - Auto-select all scalar attributes of the sub-entity (per
     `references/sub-dto.md`, "Auto-selection"). Filter out back-reference
     associations.
   - For each selected scalar, generate a record component using the rules
     from Step 3 above (`examples/_fragments/fields/java-record.md`):
     `{Type} {fieldName}` with all inherited validators inlined onto the
     parameter, space-separated.
   - The sub-entity's own associations are NOT expanded recursively. Only
     scalars enter the nested record. If the user wants deeper nesting,
     they must request it explicitly.
   - The nested record does NOT need its own skeleton, `@JsonIgnoreProperties`,
     or `Serializable` — those apply only to the top-level DTO.
   - The inner record's Javadoc uses **short name + import** (the "Nested
     class" variant of `examples/_fragments/javadoc/java/javadoc.md`).
   - The parent record's component for this association uses the nested
     `{SubDtoName}` as its type (e.g. `List<SlotDto> slots`) — this
     substitution already happens in Step 3 (`fields/java-record.md`); no
     additional editing is needed in Step 6 beyond inserting the inner
     record itself.
7. **Sub-DTO name collision avoidance:** if the natural sub-DTO short name
   already exists in the target package (e.g. `PetDto` already present),
   auto-suffix with a number: `PetDto1`, `PetDto2`, …. The
   skill must check `list_existing_classes(package)` and apply the same
   suffix rule before generating either a separate-file `NEW_CLASS` or a
   `NEW_NESTED_CLASS` inside a record.

## Variant-Specific Questions

No additional questions needed for records. Records are immutable by design:
- allArgsConstructor, equalsHashCode, toString -- all built-in to records, do NOT ask
- mutable=false, fluentSetters=false -- forced

Only ask (prefer `AskUserQuestion`; fall back to plain text if unavailable):

| Question | Header | Options (first = default) |
|----------|--------|--------------------------|
| @JsonIgnoreProperties(ignoreUnknown=true)? | JsonIgnore | No (Recommended) / Yes |

## Notes
- When isJavaRecord=true, allArgsConstructor, equalsHashCode, toString, mutable are disabled (records have them built-in)
- Record components do NOT have `private final` prefix -- just `Type name`
