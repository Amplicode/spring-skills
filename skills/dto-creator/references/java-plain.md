# Java Plain Class DTO

## Conditions
- language = JAVA
- isJavaRecord = false
- Lombok NOT on classpath OR useLombokDto = false

## Generation Order

1. **Skeleton:** Read `examples/_skeletons/java/class.md` -- create file
2. **Class Javadoc:** Read `examples/_fragments/javadoc/java/javadoc.md`
   ("Top-level class" variant) -- insert immediately above the
   `public class {className} {` line. Always generated.
3. **Fields:** Read `examples/_fragments/fields/java.md` -- add fields to class body
4. **Constructors:** Read `examples/_fragments/constructor/java/all-args.md` -- add after fields.
   - When `isMutable=false` AND `allArgsConstructor=true` -> only the all-args constructor.
   - When `isMutable=true` -> **BOTH** the no-args constructor AND the all-args constructor are emitted, even if the user did not explicitly ask for the no-args one.
5. **Getters + setters:**
   - When `isMutable=false`: read `examples/_fragments/getters-setters/java/getters.md` and emit getters in field order, after the constructor(s).
   - When `isMutable=true AND fluentSetters=false`: emit getters and setters **interleaved** (`getX, setX, getY, setY, …`) — see `examples/_fragments/getters-setters/java/setters.md` for the layout. Do NOT emit all getters first and then all setters.
   - When `isMutable=true AND fluentSetters=true`: emit getters as a single block (not interleaved), then all fluent setters from `examples/_fragments/fluent-setters/java/fluent-setters.md`.
6. **equals/hashCode** (when equalsHashCode=true):
   - If `useHibernateProxyEquals=true` (Hibernate on classpath AND user opted in):
     Read `examples/_fragments/equals-hashcode/java/equals-hashcode-hibernate.md`.
   - Otherwise: Read `examples/_fragments/equals-hashcode/java/equals-hashcode.md`.
   Add after getters/setters.
7. **toString** (when toString=true): Read `examples/_fragments/tostring/java/tostring.md` -- add after equals/hashCode
8. **@JsonIgnoreProperties** (when jsonIgnoreUnknownProperties=true AND Jackson on classpath): Read `examples/_fragments/json-ignore/java/json-ignore.md` -- add annotation on class
9. **Serializable** (when serializableType != NoSerializable): Read `examples/_fragments/serializable/java/serializable.md` -- modify class declaration
10. **Nested classes** (for each attribute with subDtoType=NEW_NESTED_CLASS):
    - Read `examples/_fragments/nested-class/java/nested-class.md` -- add inner class.
    - Apply `examples/_fragments/javadoc/java/javadoc.md` ("Nested class" variant)
      above the `public static class ...` line — short name + import.
    - Recursively generate the inner class contents using the same Generation
      Order (Javadoc, fields, constructor, getters, equals, toString, ...).

## Variant-Specific Questions

Prefer `AskUserQuestion` for all questions below; fall back to plain text
only if the tool is unavailable. Batch independent questions into one
`AskUserQuestion` call (up to 4 questions per call).

**Batch 1** — basic settings (3 questions):

| Question | Header | Options (first = default) |
|----------|--------|--------------------------|
| Mutable DTO (with setters)? | Mutable | No (Recommended) / Yes |
| Fluent setters (return this)? | Fluent | No (Recommended) / Yes |
| Constructor with all fields? | Constructor | Yes (Recommended) / No |

Note: fluent setters question only if mutable=true.

**Batch 2** — standard methods (3 questions):

| Question | Header | Options (first = default) |
|----------|--------|--------------------------|
| equals() and hashCode()? | Equals | Yes (Recommended) / No |
| toString()? | ToString | Yes (Recommended) / No |
| @JsonIgnoreProperties(ignoreUnknown=true)? | JsonIgnore | No (Recommended) / Yes |

**Batch 3** — only if `presentDeps` contains `hibernate-core` AND
equalsHashCode=true (1 question):

| Question | Header | Options (first = default) |
|----------|--------|--------------------------|
| equals/hashCode with Hibernate proxy support? | Hibernate | No (Recommended) / Yes |

(Sets `useHibernateProxyEquals`. Skip entirely when Hibernate is not on
the classpath.)

## Validation Rules
- At least one of allArgsConstructor or mutable must be true (otherwise fields cannot be initialized)
- fluentSetters only available when mutable=true
- `useHibernateProxyEquals=true` requires `equalsHashCode=true` AND
  `hibernate-core` in `presentDeps`
