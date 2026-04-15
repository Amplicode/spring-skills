# Java Security Configuration class + filterChain method

## Code

```java
package {packageName};

@org.springframework.context.annotation.Configuration
@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
public class {className} {

    @org.springframework.context.annotation.Bean
    public org.springframework.security.web.SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        // DSL calls inserted here
        return http.build();
    }
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{packageName}` | project context | — |
| `{className}` | user choice | `SecurityConfiguration` |
