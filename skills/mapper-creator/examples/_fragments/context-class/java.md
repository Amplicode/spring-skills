# Context inner record + helper methods (Java)

## Insert Point
Inner record in mapper interface body + helper methods.

## Code

```defaults
skip this fragment (only when entity has AggregateReference attributes with non-ID sub-DTO type)
```

### Context inner record

```java
record {dtoName}LoadedContext({type1} {field1}, java.util.Collection<{type2}> {field2s}) {
}
```

Context helper methods are generated per aggregate reference attribute. Their exact form depends on whether the attribute is a collection, child of collection, etc. Each attribute generates one helper method that uses the context record fields.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{dtoName}` | DTO short class name | e.g. `OrderDto` |
| `{fieldN}` | from attribute type class name, decapitalized | — |
| `{typeN}` | attribute type FQN | — |
