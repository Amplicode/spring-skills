# Kotlin Security Configuration class + filterChain method

## Code

```kotlin
package {packageName}

@org.springframework.context.annotation.Configuration
@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
class {className} {

    @org.springframework.context.annotation.Bean
    fun filterChain(http: org.springframework.security.config.annotation.web.builders.HttpSecurity): org.springframework.security.web.SecurityFilterChain {
        // DSL calls inserted here
        return http.build()
    }
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{packageName}` | project context | — |
| `{className}` | user choice | `SecurityConfiguration` |
