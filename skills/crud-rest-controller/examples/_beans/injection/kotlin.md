# Bean injection into controller (Kotlin)

## Insert Point
Primary constructor parameter in the controller class declaration.

## Code

### defaults
Constructor injection (single bean -- repository):
```kotlin
class ${ControllerName}(
    private val ${fieldName}: ${BEAN_FQN}
)
```

### multiple beans (repository + mapper)
```kotlin
class ${ControllerName}(
    private val ${fieldName1}: ${BEAN_FQN_1},
    private val ${fieldName2}: ${BEAN_FQN_2}
)
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${BEAN_FQN}` | FQN of the bean class (repository, mapper, ObjectPatcher, ObjectMapper) | -- |
| `${fieldName}` | decapitalized class name | -- |
| `${ControllerName}` | controller class name | -- |
