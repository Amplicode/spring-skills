# Controller class skeleton (Java)

## Code

```java
package {packageName};

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("{requestPath}")
public class {className} {

}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{packageName}` | project context | mainPackage |
| `{className}` | user choice | `{EntityName}Resource` |
| `{requestPath}` | basePath + resourcePath | `/rest/admin-ui/{entityVarPlural}` |
