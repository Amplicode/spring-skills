# Add JpaSpecificationExecutor to repository (Kotlin)

## Insert Point
Add `org.springframework.data.jpa.repository.JpaSpecificationExecutor<${EntityFqn}>` to the repository interface extends list.

## Code

### defaults
Skip this fragment -- only applied when ALL of: (1) filter is selected, (2) entity is a JPA entity (has @Entity annotation), (3) repo does not already extend JpaSpecificationExecutor.

### when filter selected
```kotlin
interface ${RepoName} : org.springframework.data.jpa.repository.JpaRepository<${EntityFqn}, ${IdType}>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<${EntityFqn}>
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${RepoName}` | repository interface name | -- |
| `${EntityFqn}` | entity FQN | -- |
| `${IdType}` | entity ID type | -- |
