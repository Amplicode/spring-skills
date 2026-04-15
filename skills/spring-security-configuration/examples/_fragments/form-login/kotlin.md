# FormLogin DSL fragment (Kotlin)

## Insert Point
Inside filterChain method body, before `return http.build()`

## Code

```kotlin
// defaults: enabled, handlerType=STATUS, no URLs set
// Default output (no customization):
http.formLogin(org.springframework.security.config.Customizer.withDefaults())

// When disabled=true:
http.formLogin { it.disable() }

// With STATUS handlers:
http.formLogin { formLogin ->
    formLogin
        .loginPage("{loginUrl}")
        .usernameParameter("{usernameParam}")
        .passwordParameter("{passwordParam}")
        .failureUrl("{failureUrl}")
        .defaultSuccessUrl("{defaultSuccessUrl}")
        .loginProcessingUrl("{loginProcessingUrl}")
        .failureForwardUrl("{failureForwardUrl}")
        .successForwardUrl("{successForwardUrl}")
        .authenticationDetailsSource({authDetailsSourceBean})                         // if bean is set
        .successHandler { _, response, _ -> response.status = org.springframework.http.HttpStatus.{successStatus}.value() }
        .failureHandler { _, response, _ -> response.status = org.springframework.http.HttpStatus.{failureStatus}.value() }
        .permitAll()
}

// With BEAN handlers:
http.formLogin { formLogin ->
    formLogin
        .successHandler({successHandlerBean})
        .failureHandler({failureHandlerBean})
        .permitAll()
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{loginUrl}` | user input | skip if empty |
| `{usernameParam}` | user input | skip if empty |
| `{passwordParam}` | user input | skip if empty |
| `{failureUrl}` | user input | skip if empty |
| `{defaultSuccessUrl}` | user input | skip if empty |
| `{loginProcessingUrl}` | user input | skip if empty |
| `{failureForwardUrl}` | user input | skip if empty |
| `{successForwardUrl}` | user input | skip if empty |
| `{successStatus}` | user choice | skip if not set |
| `{failureStatus}` | user choice | skip if not set |
| `{authDetailsSourceBean}` | user input (bean ref) | skip if not set |
| `{successHandlerBean}` | user input (bean ref) | skip if not set |
| `{failureHandlerBean}` | user input (bean ref) | skip if not set |
