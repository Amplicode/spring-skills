# Lombok annotations (Java)

## Insert Point
As annotations on class declaration.

## Code

```defaults
// Default: @lombok.Value (when all default options: equalsHashCode=true, allArgsConstructor=true, toString=true, mutable=false)
```

**Case 1: @Value** (equalsHashCode=true AND allArgsConstructor=true AND toString=true AND mutable=false):
```java
@lombok.Value
public class {className} {
```

**Case 2: @Data** (equalsHashCode=true AND toString=true, but NOT all conditions for @Value):
```java
@lombok.Data
public class {className} {
```

When additionally allArgsConstructor=true AND mutable=true:
```java
@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class {className} {
```

**Case 3: Individual annotations** (when NOT (equalsHashCode=true AND toString=true)):
```java
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Getter
@lombok.Setter
@lombok.EqualsAndHashCode
@lombok.ToString
public class {className} {
```

Rules for Case 3:
- `@lombok.AllArgsConstructor` -- when allArgsConstructor=true
- `@lombok.NoArgsConstructor` -- when allArgsConstructor=true AND mutable=true
- `@lombok.Getter` -- always
- `@lombok.Setter` -- when mutable=true
- `@lombok.EqualsAndHashCode` -- when equalsHashCode=true
- `@lombok.ToString` -- when toString=true

**Additional (all cases), when mutable=true AND fluentSetters=true:**
```java
@lombok.experimental.Accessors(chain = true)
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | DTO class name | -- |
