# LDAP Authentication Manager bean (Java)

## Insert Point
As @Bean method in the configuration class body.

## Code

```java
// defaults: BIND authentication, NO_AUTHORITIES

// BIND authentication type:
@org.springframework.context.annotation.Bean
org.springframework.security.authentication.AuthenticationManager ldapAuthenticationManager() {
    org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory factory = new org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory(contextSource());
    factory.setUserDnPatterns({ldapUserDnPatternsField});
    factory.setLdapAuthoritiesPopulator({ldapAuthoritiesPopulatorRef});   // if authorities populator is set
    factory.setAuthoritiesMapper({authoritiesMapperRef});                 // if authoritiesMapper is set
    factory.setUserDetailsContextMapper({userDetailsContextMapperRef});   // if userDetailsContextMapper is set
    return factory.createAuthenticationManager();
}

// PASSWORD authentication type:
@org.springframework.context.annotation.Bean
org.springframework.security.authentication.AuthenticationManager ldapAuthenticationManager() {
    org.springframework.security.config.ldap.LdapPasswordComparisonAuthenticationManagerFactory factory = new org.springframework.security.config.ldap.LdapPasswordComparisonAuthenticationManagerFactory(contextSource(), {passwordEncoderRef});
    factory.setUserDnPatterns({ldapUserDnPatternsField});
    factory.setLdapAuthoritiesPopulator({ldapAuthoritiesPopulatorRef});
    return factory.createAuthenticationManager();
}
```

Authorities populator variants (bean body):
```java
// NO_AUTHORITIES:
return new org.springframework.security.ldap.authentication.NullLdapAuthoritiesPopulator();

// GROUP:
return new org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator(contextSource(), {ldapGroupSearchBaseField});

// GROUP_WITH_HIERARCHY:
return new org.springframework.security.ldap.userdetails.NestedLdapAuthoritiesPopulator(contextSource(), {ldapGroupSearchBaseField});

// CUSTOM:
return (userData, username) -> {
    //TODO: add granted authorities
    return java.util.Collections.emptyList();
};
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
