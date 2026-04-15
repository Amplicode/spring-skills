# LDAP Variant

Authentication type: `LDAP_STATEFUL`

## Fragments Used

1. Common DSL (see `references/common-dsl.md`) — all blocks (headers, anonymous, csrf always generated with `Customizer.withDefaults()`)
2. LDAP Context Source bean (`_beans/ldap-context-source/{lang}.md`) — **real @Bean**, not helper method
3. LDAP Authorities Populator bean — **real @Bean** (NullLdapAuthoritiesPopulator for "No authorities")
4. LDAP Auth Manager bean (`_beans/ldap-auth-manager/{lang}.md`) — **real @Bean**

## Dependencies

- `_dependencies/base.md` (always)
- `_dependencies/ldap.md`

## Properties

- `_properties/ldap/properties.md`

## Questions

### Connection Settings (batch)

```
LDAP connection settings:
1. **Host?** []
2. **Port?** [389]
3. **Use SSL (ldaps)?** [yes]
4. **Base DN?** []
```

### Access Type Question

```
LDAP access type?
1. Anonymous (default)
2. With manager credentials
```

If authenticated:
```
Manager credentials:
1. **Manager DN?** []
2. **Manager Password?** []
```

### Authentication Type Question

```
LDAP authentication type?
1. BIND (default) -- verify by binding
2. PASSWORD -- password comparison
```

If PASSWORD: ask for password encoder bean reference.

### User DN Patterns Question

```
User DN Patterns? (e.g., uid={0},ou=people) []
```

### Authorities Question

```
Authorities source?
1. No authorities (default) -- NullLdapAuthoritiesPopulator
2. By group membership -- DefaultLdapAuthoritiesPopulator
3. By group membership with hierarchy -- NestedLdapAuthoritiesPopulator
4. Custom -- custom bean
```

If GROUP or GROUP_WITH_HIERARCHY: ask for group search base.

## Generation

**LDAP DSL order: authorizeHttpRequests → headers → anonymous → csrf** (same as Custom — NO sessionManagement, formLogin, or logout!)

1. Read skeleton from `_skeletons/{lang}.md`
2. Add `@Value` field injection for LDAP properties (use `appPrefix` from Step 1):
   - `@Value("${appPrefix}.ldap.url") private String ldapUrl;`
   - `@Value("${appPrefix}.ldap.userDnPatterns") private String ldapUserDnPatterns;`
   - Optionally: managerDn, managerPassword, groupSearchBase
3. Insert `http.authorizeHttpRequests(...)` fragment (FIRST in filterChain)
4. Insert `http.headers(Customizer.withDefaults())`
5. Insert `http.anonymous(Customizer.withDefaults())`
6. Insert `http.csrf(Customizer.withDefaults())`
7. Add `@Bean contextSource()` — returns `DefaultSpringSecurityContextSource(ldapUrl)`
8. Add `@Bean ldapAuthoritiesPopulator()` — returns appropriate populator:
   - No authorities → `NullLdapAuthoritiesPopulator`
   - Group member → `DefaultLdapAuthoritiesPopulator`
   - Group member with hierarchy → use role hierarchy
   - Custom → user-provided bean
9. Add `@Bean ldapAuthenticationManager()` — uses `LdapBindAuthenticationManagerFactory` (for Bind auth) or `LdapPasswordComparisonAuthenticationManagerFactory` (for Password auth)
10. Add dependencies from `_dependencies/base.md` + `_dependencies/ldap.md`
11. Write LDAP properties to application.properties (see `_properties/ldap/properties.md`)

## Notes

- **Property key prefix uses project artifact name**: e.g., `spring-petclinic.ldap.url` (not `spring.ldap.*`)
- **Field injection with `@Value`** — unlike OIDC which uses constructor injection, LDAP uses field injection
- **All LDAP beans are real `@Bean` methods** — unlike JWT/OIDC helper methods, these are proper Spring beans
- Despite having "Http Session Authentication (stateful, LDAP)" as auth type, the generated config does NOT include sessionManagement, formLogin, or logout
- The LDAP URL format is `ldaps://host:port/baseDn` (when Secure is checked) or `ldap://host:port/baseDn`

## Advanced Options

- **Authorities mapper**: optional bean reference for custom `GrantedAuthoritiesMapper`
- **User details context mapper**: optional bean reference for custom `UserDetailsContextMapper`
