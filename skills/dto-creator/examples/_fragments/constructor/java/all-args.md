# All-args constructor (Java)

## Insert Point
As constructor(s) in class body, after fields.

## Code

```defaults
// Default: all-args constructor only (when isMutable=false).
// When isMutable=true, BOTH a no-args and an all-args constructor are
// emitted. The skill MUST generate both even if the user did not explicitly
// ask for the no-args one.
// Indentation: Use the project's detected indent unit (see `SKILL.md` § Indentation).
```

**Immutable (isMutable=false) -- all-args constructor only:**
```java
public {className}({Type1} {field1}, {Type2} {field2}) {
	this.{field1} = {field1};
	this.{field2} = {field2};
}
```

**Mutable (isMutable=true) -- BOTH no-args and all-args constructors:**
```java
public {className}() {
}

public {className}({Type1} {field1}, {Type2} {field2}) {
	this.{field1} = {field1};
	this.{field2} = {field2};
}
```

## Formatting rules
- One blank line between the no-args and the all-args constructor (mutable case).
- Constructor parameters all on a single line — no per-parameter wrapping,
  even for many fields.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | DTO class name | -- |
| `{fieldN}` | field names from selected entity attributes | -- |
| `{TypeN}` | field types from selected entity attributes | -- |
