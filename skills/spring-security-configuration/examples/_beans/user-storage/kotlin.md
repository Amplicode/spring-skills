# User Storage bean (Kotlin)

## Insert Point
As method in the configuration class body. Called from filterChain via `http.userDetailsService(...)`.

## Code

```kotlin
// IN_MEMORY variant:
fun inMemoryUserDetailsService(): org.springframework.security.core.userdetails.UserDetailsService {
    val users = org.springframework.security.core.userdetails.User.builder()
    val userDetailsManager = org.springframework.security.provisioning.InMemoryUserDetailsManager()
    userDetailsManager.createUser(users.username("{username1}")
        .password("{noop}{password1}")
        .roles("{role1}")
        .build())
    userDetailsManager.createUser(users.username("{username2}")
        .password("{noop}{password2}")
        .roles("{role2}")
        .build())
    return userDetailsManager
}

// JDBC variant:
fun jdbcUserDetailsService(): org.springframework.security.core.userdetails.UserDetailsService {
    return org.springframework.security.provisioning.JdbcUserDetailsManager({dataSourceField})
}

// JPA variant:
// No method generated — the bean is assumed to exist.
// The filterChain must include: http.userDetailsService({jpaUserDetailsServiceBean})

// CUSTOM variant:
// No method generated — the bean is assumed to exist.
// The filterChain must include: http.userDetailsService({customUserDetailsServiceBean})
```

## Variables
Same as Java variant.
