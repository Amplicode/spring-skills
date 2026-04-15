# @AfterMapping method (Java)

## Insert Point
Default method in mapper interface body.

## Code

```defaults
skip this fragment (only added when entity has non-owner OneToOne/OneToMany with mappedBy + sub-DTO)
```

### OneToMany association

```java
@org.mapstruct.AfterMapping
default void link{entityAttrNameCapitalized}(@org.mapstruct.MappingTarget {entityClassFqn} {entityParamName}) {
    {entityParamName}.get{entityAttrNameCapitalized}().forEach({unpluralizedAttrName} -> {unpluralizedAttrName}.set{inverseAttributeNameCapitalized}({entityParamName}));
}
```

### OneToOne association

```java
@org.mapstruct.AfterMapping
default void link{entityAttrNameCapitalized}(@org.mapstruct.MappingTarget {entityClassFqn} {entityParamName}) {
    {attrTypeEntityFqn} {entityAttrName} = {entityParamName}.get{entityAttrNameCapitalized}();
    if ({entityAttrName} != null) {
        {entityAttrName}.set{inverseAttributeNameCapitalized}({entityParamName});
    }
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{entityAttrName}` | entity association attribute name | e.g. `children`, `address` |
| `{entityAttrNameCapitalized}` | capitalized | e.g. `Children`, `Address` |
| `{entityParamName}` | decapitalized entity short name | e.g. `order` |
| `{entityClassFqn}` | entity class FQN | — |
| `{inverseAttributeNameCapitalized}` | capitalized mappedBy attribute | e.g. `Parent`, `Order` |
| `{unpluralizedAttrName}` | unpluralized attr name (OneToMany only) | e.g. `child` |
| `{attrTypeEntityFqn}` | association target entity FQN (OneToOne only) | — |
