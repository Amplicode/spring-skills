# @AfterMapping method (Kotlin)

## Insert Point
Function in mapper abstract class body.

## Code

```defaults
skip this fragment (only added when entity has non-owner OneToOne/OneToMany with mappedBy + sub-DTO)
```

### OneToMany association

```kotlin
@org.mapstruct.AfterMapping
fun link{entityAttrNameCapitalized}(@org.mapstruct.MappingTarget {entityParamName}: {entityClassFqn}) {
    {entityParamName}.{entityAttrName}.forEach { it.{inverseAttributeName} = {entityParamName} }
}
```

### OneToOne association

```kotlin
@org.mapstruct.AfterMapping
fun link{entityAttrNameCapitalized}(@org.mapstruct.MappingTarget {entityParamName}: {entityClassFqn}) {
    {entityParamName}.{entityAttrName}?.{inverseAttributeName} = {entityParamName}
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{entityAttrName}` | entity association attribute name | e.g. `children`, `address` |
| `{entityAttrNameCapitalized}` | capitalized | e.g. `Children`, `Address` |
| `{entityParamName}` | decapitalized entity short name | e.g. `order` |
| `{entityClassFqn}` | entity class FQN | — |
| `{inverseAttributeName}` | mappedBy attribute name | e.g. `parent`, `order` |
