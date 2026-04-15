# Anonymous DSL fragment (Kotlin)

## Insert Point
Inside filterChain method body, before `return http.build()`

## Code

```kotlin
// defaults: enabled, no options
// When all options are default — skip this fragment entirely

// When disabled=true:
http.anonymous { it.disable() }

// With options:
http.anonymous { anonymous ->
    anonymous
        .key("{keyValue}")                             // if key is not blank
        .authorities("{auth1}", "{auth2}")              // if authorities is not empty
        .authenticationProvider({providerBean})          // if authenticationProvider is set
        .authenticationFilter({filterBean})              // if authenticationFilter is set
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{keyValue}` | user input | skip if empty |
| `{auth1}`, `{auth2}` | user input | skip if empty |
| `{providerBean}` | user input (bean ref) | skip if not set |
| `{filterBean}` | user input (bean ref) | skip if not set |
