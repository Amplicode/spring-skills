# LDAP Authentication Manager bean (Kotlin)

## Insert Point
As @Bean method in the configuration class body.

## Code

```kotlin
// defaults: BIND authentication, NO_AUTHORITIES

// BIND authentication type:
@org.springframework.context.annotation.Bean
fun ldapAuthenticationManager(): org.springframework.security.authentication.AuthenticationManager {
    val factory: org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory = org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory(contextSource())
    factory.setUserDnPatterns({ldapUserDnPatternsField})
    factory.setLdapAuthoritiesPopulator({ldapAuthoritiesPopulatorRef})   // if authorities populator is set
    factory.setAuthoritiesMapper({authoritiesMapperRef})                 // if authoritiesMapper is set
    factory.setUserDetailsContextMapper({userDetailsContextMapperRef})   // if userDetailsContextMapper is set
    return factory.createAuthenticationManager()
}

// PASSWORD authentication type:
@org.springframework.context.annotation.Bean
fun ldapAuthenticationManager(): org.springframework.security.authentication.AuthenticationManager {
    val factory: org.springframework.security.config.ldap.LdapPasswordComparisonAuthenticationManagerFactory = org.springframework.security.config.ldap.LdapPasswordComparisonAuthenticationManagerFactory(contextSource(), {passwordEncoderRef})
    factory.setUserDnPatterns({ldapUserDnPatternsField})
    factory.setLdapAuthoritiesPopulator({ldapAuthoritiesPopulatorRef})
    return factory.createAuthenticationManager()
}
```

Authorities populator variants (bean body):
```kotlin
// NO_AUTHORITIES:
return org.springframework.security.ldap.authentication.NullLdapAuthoritiesPopulator()

// GROUP:
return org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator(contextSource(), {ldapGroupSearchBaseField})

// GROUP_WITH_HIERARCHY:
return org.springframework.security.ldap.userdetails.NestedLdapAuthoritiesPopulator(contextSource(), {ldapGroupSearchBaseField})

// CUSTOM:
return LdapAuthoritiesPopulator { userData, username ->
    //TODO: add granted authorities
    emptyList()
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{ldapUserDnPatternsField}` | @Value field, property key: `{appPrefix}.ldap.userDnPatterns` | — |
| `{ldapGroupSearchBaseField}` | @Value field, property key: `{appPrefix}.ldap.groupSearchBase` | only for GROUP/GROUP_WITH_HIERARCHY |
| `{passwordEncoderRef}` | bean reference | only for PASSWORD type |
| `{ldapAuthoritiesPopulatorRef}` | bean method reference | depends on authoritiesSelector |
| `{authoritiesMapperRef}` | bean reference | skip if not set |
| `{userDetailsContextMapperRef}` | bean reference | skip if not set |
