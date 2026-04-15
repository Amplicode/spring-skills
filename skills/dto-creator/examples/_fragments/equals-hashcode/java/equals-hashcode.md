# equals() and hashCode() (Java)

## Insert Point
As methods in class body, after getters/setters.

## Code

```defaults
// Default: generated (isEqualsHashCode=true by default).
// Use the plain variant unless Hibernate is on the classpath AND the user
// asked for proxy-aware equals -- then use the Hibernate-proxy variant from
// equals-hashcode-hibernate.md.
// Indentation: Use the project's detected indent unit (see `SKILL.md` § Indentation).
```

```java
@Override
public boolean equals(Object o) {
	if (this == o) return true;
	if (o == null || getClass() != o.getClass()) return false;
	{className} entity = ({className}) o;
	return java.util.Objects.equals(this.{field1}, entity.{field1}) &&
		java.util.Objects.equals(this.{field2}, entity.{field2});
}

@Override
public int hashCode() {
	return java.util.Objects.hash({field1}, {field2});
}
```

**When no fields:**
```java
@Override
public boolean equals(Object o) {
	if (this == o) return true;
	if (o == null || getClass() != o.getClass()) return false;
	return true;
}

@Override
public int hashCode() {
	return java.util.Objects.hash();
}
```

## Formatting rules
- Cast variable name is always `entity`.
- The `&&` chain hangs on the right side of the previous line; each
  continuation line is indented one extra tab beyond `return`.
- Methods are separated by a single blank line.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | DTO class name | -- |
| `{fieldN}` | field names from selected entity attributes | -- |
