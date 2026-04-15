# Flat expression method (Kotlin)

## Insert Point
Function in mapper abstract class body. Added via postProcess.

## Code

```defaults
skip this fragment (only for DTOs with flat collection attributes where subDtoType == FLAT)
```

### Flat collection mapping function

```kotlin
fun {functionName}({mappedFieldName}: Collection<{mappedEntityFqn}>): {collectionFqn}<{attrKotlinType}?> {
    return {mappedFieldName}.mapNotNull{ it.{mappedAttrName} }{aggregateReferenceMapper}.toMutable{collectionClassName}()
}
```

The parameter type is always `Collection` — not the concrete collection
type from the entity field. The method only iterates, so the concrete
type is irrelevant. This also avoids mismatches when the entity getter
returns a different collection type than the field declaration.

Where `{aggregateReferenceMapper}`:
- `.map { it.id }` if mapped attr is AggregateReference
- empty otherwise

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{functionName}` | `{mappedFieldName}To{capitalize(attrName)}` | — |
| `{mappedFieldName}` | from unFlatName before `.` | — |
| `{mappedEntityFqn}` | association target entity FQN | — |
| `{mappedAttrName}` | attribute name after `.` | — |
| `{collectionFqn}` | `kotlin.collections.MutableList` or `kotlin.collections.MutableSet` | — |
| `{collectionClassName}` | `List` or `Set` | — |
| `{attrKotlinType}` | DTO field Kotlin type | — |
