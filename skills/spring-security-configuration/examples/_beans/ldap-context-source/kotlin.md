# LDAP Context Source bean (Kotlin)

## Insert Point
As @Bean method in the configuration class body.

## Code

```kotlin
// defaults: anonymous access

// Anonymous access:
@org.springframework.context.annotation.Bean
fun contextSource(): org.springframework.ldap.core.support.BaseLdapPathContextSource =
    org.springframework.security.ldap.DefaultSpringSecurityContextSource({ldapUrlField})

// Authenticated access (with manager credentials):
@org.springframework.context.annotation.Bean
fun contextSource(): org.springframework.ldap.core.support.BaseLdapPathContextSource {
    val contextSource: org.springframework.security.ldap.DefaultSpringSecurityContextSource = org.springframework.security.ldap.DefaultSpringSecurityContextSource({ldapUrlField})
    contextSource.setUserDn({ldapManagerDnField})
    contextSource.setPassword({ldapManagerPasswordField})
    return contextSource
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{ldapUrlField}` | @Value field, property key: `{appPrefix}.ldap.url` | — |
| `{ldapManagerDnField}` | @Value field | skip if anonymous access |
| `{ldapManagerPasswordField}` | @Value field | skip if anonymous access |
