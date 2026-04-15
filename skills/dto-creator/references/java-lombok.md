# Java + Lombok DTO

## Conditions
- language = JAVA
- isJavaRecord = false
- Lombok on classpath AND useLombokDto setting enabled

## Generation Order

1. **Skeleton:** Read `examples/_skeletons/java/class.md` -- create file
2. **Class Javadoc:** Read `examples/_fragments/javadoc/java/javadoc.md`
   ("Top-level class" variant) -- insert immediately above the
   `public class {className} {` line. Always generated.
   Uses the uniform **short name + import** rule (see `_fragments/javadoc/java/javadoc.md`).
3. **Lombok annotations:** Read `examples/_fragments/lombok/lombok.md` -- add annotations on class
4. **Fields:** Read `examples/_fragments/fields/java-lombok.md` -- add fields to class body (NO `private`/`final` modifiers -- Lombok handles them)
5. **@JsonIgnoreProperties** (when jsonIgnoreUnknownProperties=true AND Jackson on classpath): Read `examples/_fragments/json-ignore/java/json-ignore.md` -- add annotation on class
6. **Serializable** (when serializableType != NoSerializable): Read `examples/_fragments/serializable/java/serializable.md` -- modify class declaration
7. **Nested classes** (for each attribute with subDtoType=NEW_NESTED_CLASS):
   Read `examples/_fragments/nested-class/java/nested-class.md`. Apply
   `examples/_fragments/javadoc/java/javadoc.md` ("Nested class" variant)
   above the inner class — short name + import.

## Lombok Field Rules

When Lombok is active, field generation changes:
- `private` modifier is OMITTED (Lombok generates access)
- `final` modifier is OMITTED when @Value is used (Lombok makes fields final)
- NO getters, setters, constructors, equals/hashCode, or toString are generated manually -- Lombok handles them

## Variant-Specific Questions

Prefer `AskUserQuestion` for all questions below; fall back to plain text
only if the tool is unavailable. Batch independent questions into one
`AskUserQuestion` call (up to 4 questions per call).

**Batch 1** — basic settings (3 questions):

| Question | Header | Options (first = default) |
|----------|--------|--------------------------|
| Mutable DTO (with setters)? | Mutable | No (Recommended) / Yes |
| Fluent setters (@Accessors(chain=true))? | Fluent | No (Recommended) / Yes |
| Constructor with all fields? | Constructor | Yes (Recommended) / No |

Note: fluent setters question only if mutable=true.

**Batch 2** — standard methods (3 questions):

| Question | Header | Options (first = default) |
|----------|--------|--------------------------|
| equals() and hashCode()? | Equals | Yes (Recommended) / No |
| toString()? | ToString | Yes (Recommended) / No |
| @JsonIgnoreProperties(ignoreUnknown=true)? | JsonIgnore | No (Recommended) / Yes |

## Lombok Annotation Selection

Decision tree (from `examples/_fragments/lombok/lombok.md`):

1. equalsHashCode=true AND allArgsConstructor=true AND toString=true AND mutable=false -> `@lombok.Value`
2. equalsHashCode=true AND toString=true (but NOT all @Value conditions) -> `@lombok.Data` (+ `@lombok.AllArgsConstructor` + `@lombok.NoArgsConstructor` if allArgsConstructor=true AND mutable=true)
3. Otherwise -> individual annotations: `@lombok.Getter`, `@lombok.Setter` (if mutable), `@lombok.AllArgsConstructor` (if allArgsConstructor), etc.
4. Additionally: mutable=true AND fluentSetters=true -> add `@lombok.experimental.Accessors(chain = true)`
