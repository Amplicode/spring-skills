# LDAP Context Source bean (Java)

## Insert Point
As @Bean method in the configuration class body.

## Code

```java
// defaults: anonymous access
// Two variants: anonymous and authenticated (with manager credentials)

// Anonymous access:
@org.springframework.context.annotation.Bean
org.springframework.ldap.core.support.BaseLdapPathContextSource contextSource() {
    return new org.springframework.security.ldap.DefaultSpringSecurityContextSource({ldapUrlField});
}

// Authenticated access (with manager credentials):
@org.springframework.context.annotation.Bean
org.springframework.ldap.core.support.BaseLdapPathContextSource contextSource() {
    org.springframework.security.ldap.DefaultSpringSecurityContextSource contextSource = new org.springframework.security.ldap.DefaultSpringSecurityContextSource({ldapUrlField});
    contextSource.setUserDn({ldapManagerDnField});
    contextSource.setPassword({ldapManagerPasswordField});
    return contextSource;
}
```

`{ldapUrlField}`, `{ldapManagerDnField}`, `{ldapManagerPasswordField}` are fields injected via `@org.springframework.beans.factory.annotation.Value` from application properties.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{ldapUrlField}` | @Value field, property key: `{appPrefix}.ldap.url` | value: `ldap(s)://{host}:{port}/{baseDn}` |
| `{ldapManagerDnField}` | @Value field, property key: `{appPrefix}.ldap.managerDn` | skip if anonymous access |
| `{ldapManagerPasswordField}` | @Value field, property key: `{appPrefix}.ldap.managerPassword` | skip if anonymous access |
