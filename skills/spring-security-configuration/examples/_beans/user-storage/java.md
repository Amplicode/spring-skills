# User Storage bean (Java)

## Insert Point
As method in the configuration class body. Called from filterChain via `http.userDetailsService(...)`.

## Code

```java
// IN_MEMORY variant (default for HTTP Session):
// NOTE: This is NOT a @Bean — it is a plain method called inline from filterChain.
// The filterChain must include: http.userDetailsService(inMemoryUserDetailsService());

public org.springframework.security.core.userdetails.UserDetailsService inMemoryUserDetailsService() {
    org.springframework.security.core.userdetails.User.UserBuilder users = org.springframework.security.core.userdetails.User.builder();
    org.springframework.security.provisioning.InMemoryUserDetailsManager userDetailsManager = new org.springframework.security.provisioning.InMemoryUserDetailsManager();
    userDetailsManager.createUser(users.username("{username1}")
        .password("{noop}{password1}")
        .roles("{role1}")
        .build());
    // repeat for each user:
    userDetailsManager.createUser(users.username("{username2}")
        .password("{noop}{password2}")
        .roles("{role2}")
        .build());
    return userDetailsManager;
}

// JDBC variant:
// Requires DataSource bean to be available in the context.
// The filterChain must include: http.userDetailsService(jdbcUserDetailsService());

public org.springframework.security.core.userdetails.UserDetailsService jdbcUserDetailsService() {
    org.springframework.security.provisioning.JdbcUserDetailsManager userDetailsManager = new org.springframework.security.provisioning.JdbcUserDetailsManager({dataSourceField});
    return userDetailsManager;
}

// JPA variant:
// Requires a custom UserDetailsService implementation.
// The user is asked for the bean reference of their UserDetailsService.
// The filterChain must include: http.userDetailsService({jpaUserDetailsServiceBean});
// No method is generated — the bean is assumed to exist.

// CUSTOM variant:
// The user is asked for the bean reference.
// The filterChain must include: http.userDetailsService({customUserDetailsServiceBean});
// No method is generated — the bean is assumed to exist.
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{username1}`, `{password1}`, `{role1}` | from user table | `admin` / `admin` / `ADMIN` |
| `{username2}`, `{password2}`, `{role2}` | from user table | `user` / `user` / `USER` |
| `{dataSourceField}` | autowired DataSource field | `dataSource` |
| `{jpaUserDetailsServiceBean}` | user input (bean ref) | — |
| `{customUserDetailsServiceBean}` | user input (bean ref) | — |

## Notes

- For IN_MEMORY: passwords are prefixed with `{noop}` for plain-text encoding (development only)
- For JDBC: needs `javax.sql.DataSource` autowired as constructor parameter
- For JPA/CUSTOM: the user provides their own `UserDetailsService` bean
- The `http.userDetailsService(...)` call in filterChain is REQUIRED — without it, the UserDetailsService is not wired into the security chain
