# Bean injection into controller (Java)

## Insert Point
Field and constructor parameter in the controller class body.

## Code

### defaults
Constructor injection (single bean -- repository):
```java
private final ${BEAN_FQN} ${fieldName};

public ${ControllerName}(${BEAN_FQN} ${fieldName}) {
    this.${fieldName} = ${fieldName};
}
```

### multiple beans (repository + mapper)
```java
private final ${BEAN_FQN_1} ${fieldName1};
private final ${BEAN_FQN_2} ${fieldName2};

public ${ControllerName}(${BEAN_FQN_1} ${fieldName1}, ${BEAN_FQN_2} ${fieldName2}) {
    this.${fieldName1} = ${fieldName1};
    this.${fieldName2} = ${fieldName2};
}
```

### field injection (alternative)
```java
@org.springframework.beans.factory.annotation.Autowired
private ${BEAN_FQN} ${fieldName};
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${BEAN_FQN}` | FQN of the bean class (repository, mapper, ObjectPatcher, ObjectMapper) | -- |
| `${fieldName}` | decapitalized class name | -- |
| `${ControllerName}` | controller class name | -- |
