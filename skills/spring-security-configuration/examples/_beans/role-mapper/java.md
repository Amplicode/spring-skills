# Role Mapper method (Java)

## Insert Point
As a public method in the configuration class body.

**Keycloak variant**: **NOT a @Bean** — called directly from oauth2Login DSL via `.userAuthoritiesMapper(userAuthoritiesMapper())`.
**Generic variant**: **@Bean** — annotated with `@Bean`, registered as a Spring bean (NOT called from DSL directly).

## Code

```java
// defaults: not generated unless OIDC_STATEFUL
// Two variants: generic (all providers) and Keycloak-specific

// Generic variant (with @Bean):
@org.springframework.context.annotation.Bean
public org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper userAuthoritiesMapper() {
    return (authorities) -> {
        java.util.Set<org.springframework.security.core.GrantedAuthority> mappedAuthorities = new java.util.HashSet<>();
        authorities.forEach(authority -> {
                //TODO Map roles
//              if (authority instanceof OidcUserAuthority){
//                  OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
//                  JSONArray keycloakRoles = (JSONArray) oidcUserAuthority.getAttributes().get("roles");
//                  keycloakRoles.forEach(role -> mappedAuthorities.add(new SimpleGrantedAuthority((String) role)));
//              } else {
//                  mappedAuthorities.add(authority);
//              }
        });
        return mappedAuthorities;
    };
}

// Keycloak variant (no @Bean, called from DSL):
public org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper userAuthoritiesMapper() {
    return authorities -> {
        java.util.Set<org.springframework.security.core.GrantedAuthority> mappedAuthorities = new java.util.HashSet<>();
        authorities.forEach( authority -> {
//                TODO: Do not forget to enable "Add to userinfo" in Keycloak (Realm | Client scopes | roles | Mappers | client roles)
//                if (!(authority instanceof OidcUserAuthority oidcUserAuthority)) {
//                	return;
//                }
//
//                // noinspection unchecked
//                Optional.ofNullable(oidcUserAuthority.getAttributes().get("resource_access"))
//                	.map(ra -> ((Map<String, ?>) ra).get("{clientId}"))
//                	.map(sbLegacy -> ((Map<String, ?>) sbLegacy).get("roles"))
//                	.ifPresent(roles -> ((List<String>) roles).stream()
//                		.map(r -> new SimpleGrantedAuthority("ROLE_" + r))
//                		.forEach(mappedAuthorities::add));
        });
        return mappedAuthorities;
    };
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{clientId}` | from OAuth provider clientId | — (only for Keycloak variant) |
