# Standard setters (Java)

## Insert Point
**Interleaved with getters**, NOT in a separate block at the end. Emit methods in the order:

```
getField1
setField1
<blank line>
getField2
setField2
<blank line>
...
```

i.e. for each selected field a `get` then a `set` then a blank line, in the
same order as the fields. There is no "all getters first, then all setters"
section.

## Code

```defaults
// Default: skip (only generated when isMutable=true && isFluentSetters=false).
// Format: multi-line, interleaved with getters.
```

For each field:
```java
public void set{Name}({Type} {fieldName}) {
	this.{fieldName} = {fieldName};
}
```

## Formatting rules
- **Multi-line**: signature on its own line, body indented one level, closing
  `}` on its own line. Never collapse to one line.
- **No blank line** between a getter and its paired setter; **one blank line**
  before the next field's getter.
- Indentation: Use the project's detected indent unit (see `SKILL.md` § Indentation).

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{Name}` | capitalized field name | -- |
| `{fieldName}` | field name | -- |
| `{Type}` | field type | -- |
