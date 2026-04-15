# Custom updateWithNull method (Java)

## Insert Point
Static method in custom mapper class body.

## Code

```defaults
skip by default (only generated when user explicitly requests full update method)
```

### When UPDATE_WITH_NULL_VALUES requested

```java
public static {entityClassFqn} {methodName}({dtoClassFqn} {dtoParamName}, {entityClassFqn} {entityParamName}) {
    {entityParamName}.set{field1Cap}({dtoParamName}.get{dtoField1Cap}());
    {entityParamName}.set{field2Cap}({dtoParamName}.get{dtoField2Cap}());
    return {entityParamName};
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{methodName}` | naming pattern | `updateWithNull` |
| `{dtoParamName}` | decapitalized DTO short name | — |
| `{entityParamName}` | decapitalized entity short name | — |
| `{dtoClassFqn}` | DTO class FQN | — |
| `{entityClassFqn}` | entity class FQN | — |
| `{fieldNCap}` | capitalized entity field names | — |
| `{dtoFieldNCap}` | capitalized DTO field names | — |
