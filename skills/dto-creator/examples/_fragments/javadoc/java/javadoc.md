# Javadoc with entity link (Java)

## Insert Point
Immediately above the class/record declaration (top-level) OR immediately above
a `public static class …` / `public record …` declaration (nested).

## Code

```defaults
// Default: generated for every Java class, every Java record, every Java
// nested class and every Java nested record created for an entity.
// IMPORTANT: format is ALWAYS multi-line (3 lines), never one-liner.
```

**Top-level Java class** — short name + import:
```java
import {entityFqn};
/**
 * DTO for {@link {entityShortName}}
 */
public class {className} {
```

**Top-level Java record** — short name + import (same form as class):
```java
import {entityFqn};
/**
 * DTO for {@link {entityShortName}}
 */
public record {className}(...) {
```

**Nested static class OR nested record** — short name + import (same):
```java
/**
 * DTO for {@link {subEntityShortName}}
 */
public static class {subDtoName} { … }
```
or
```java
/**
 * DTO for {@link {subEntityShortName}}
 */
public record {subDtoName}(...) { }
```

## FQN handling — UNIFORM

Generate the **same** rule everywhere: **short name +
import**. This applies to all shapes: top-level Java class, top-level Java
record, nested static class, nested record. There is no asymmetry.

| File shape | `{@link …}` form | `import` for entity |
|---|---|---|
| Top-level **Java class** | short name `{@link X}` | **Add** `import …X;` |
| Top-level **Java record** | short name `{@link X}` | **Add** `import …X;` |
| **Nested** static class | short name `{@link X}` | **Add** `import …X;` |
| **Nested** record | short name `{@link X}` | **Add** `import …X;` |
| **Separate-file** sub-DTO (`NEW_CLASS`, both class and record) | short name `{@link X}` | **Add** `import …X;` |

If the entity is in the **same package** as the DTO, no import is needed
(standard Java import rules), and the short name is used directly.


## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{entityFqn}` | full FQN of the source entity (used in `import` line only) | -- |
| `{entityShortName}` | short class name of the source entity | -- |
| `{subEntityShortName}` | short class name of the sub-entity (nested / NEW_CLASS) | -- |
| `{className}` | DTO class/record name | -- |
| `{subDtoName}` | nested or separate-file DTO name | -- |
