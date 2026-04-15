# Controller class skeleton (Kotlin)

## Code

```kotlin
package {packageName}

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping("{requestPath}")
class {className}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{packageName}` | project context | mainPackage |
| `{className}` | user choice | `{EntityName}Resource` |
| `{requestPath}` | basePath + resourcePath | `/rest/admin-ui/{entityVarPlural}` |
