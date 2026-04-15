# Fluent setters (Java)

## Insert Point
As methods in class body, after **all** getters (when fluent setters are
chosen, getters are NOT interleaved with setters — they form their own
block first, then all fluent setters follow).

## Code

```defaults
// Default: skip (only generated when isMutable=true && isFluentSetters=true).
// Indentation: Use the project's detected indent unit (see `SKILL.md` § Indentation).
```

For each field:
```java
public {className} set{Name}({Type} {fieldName}) {
	this.{fieldName} = {fieldName};
	return this;
}
```

## Formatting rules
- Multi-line, body indented with one unit (see `SKILL.md` § Indentation).
- One blank line between consecutive fluent setters.
- Return type is the DTO class name (NOT `void`).

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | DTO class name | -- |
| `{Name}` | capitalized field name | -- |
| `{fieldName}` | field name | -- |
| `{Type}` | field type | -- |
