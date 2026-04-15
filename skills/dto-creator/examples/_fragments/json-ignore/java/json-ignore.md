# @JsonIgnoreProperties (Java only)

## Insert Point
As annotation on class declaration.

## Code

```defaults
// Default: skip (isJsonIgnoreUnknownProperties=false by default)
```

```java
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class {className} {
```

Note: This annotation is Java-only. It is NOT generated for Kotlin DTOs. If user selects Kotlin + JsonIgnoreProperties, skip silently.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | DTO class name | -- |
