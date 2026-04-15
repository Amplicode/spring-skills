# toString() (Java)

## Insert Point
As method in class body, after equals/hashCode.

## Code

```defaults
// Default: generated (isToString=true by default).
// Format: separator ", " is appended at the end of each non-last line,
// NOT prepended at the start of the next one.
// Indentation: Use the project's detected indent unit (see `SKILL.md` § Indentation).
```

```java
@Override
public String toString() {
	return getClass().getSimpleName() + "(" +
		"{field1} = " + {field1} + ", " +
		"{field2} = " + {field2} + ", " +
		"{fieldLast} = " + {fieldLast} + ")";
}
```

**Single field:**
```java
@Override
public String toString() {
	return getClass().getSimpleName() + "(" +
		"{field1} = " + {field1} + ")";
}
```

**No fields:**
```java
@Override
public String toString() {
	return getClass().getSimpleName() + "()";
}
```

## Formatting rules
- Every field line except the LAST one ends with `+ ", " +`.
- The LAST field line ends with `+ ")";`.
- The opening `"("` stays on the `return ...` line.
- Continuation lines are indented one extra tab beyond `return`.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{fieldN}` | field names from selected entity attributes | -- |
| `{fieldLast}` | last selected field name | -- |
