# Role Mapper bean (Kotlin)

## Insert Point
As a method in the configuration class body.

**Keycloak variant**: **NOT a @Bean** — called directly from oauth2Login DSL via `.userAuthoritiesMapper(userAuthoritiesMapper())`.
**Generic variant**: **@Bean** — annotated with `@Bean`, registered as a Spring bean (NOT called from DSL directly).

## Code

```kotlin
// defaults: not generated unless OIDC_STATEFUL
// Two variants: generic (all providers) and Keycloak-specific

// Generic variant (with @Bean):
@org.springframework.context.annotation.Bean
fun userAuthoritiesMapper(): org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper {
    return org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper { authorities ->
        authorities.flatMap { authority ->
            TODO("Map authorities")
//              if (authority is OidcUserAuthority){
//                  val keycloakRoles = authority.attributes?.get("roles") as JSONArray?
//                  keycloakRoles?.map { role -> SimpleGrantedAuthority(role as String?) }?: emptyList()
//              } else {
//                  listOf(authority)
//              }
        }
    }
}

// Keycloak variant (no @Bean, called from DSL):
fun userAuthoritiesMapper() = org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper { authorities ->
    val mappedAuthorities = hashSetOf<org.springframework.security.core.GrantedAuthority>()
    authorities.filterIsInstance<org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority>().forEach { authority ->
//            TODO: Do not forget to enable "Add to userinfo" in Keycloak (Realm | Client scopes | roles | Mappers | client roles)
//            // noinspection unchecked
//            val resourceAccess = authority.attributes["resource_access"] ?: return@forEach
//            val client = (resourceAccess as (Map<*, *>))["{clientId}"] ?: return@forEach
//            val roles = (client as Map<*, *>)["roles"]
//            (roles as List<*>)
//                .map { r -> SimpleGrantedAuthority("ROLE_$r") }
//                .forEach(mappedAuthorities::add)
    }

    mappedAuthorities
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{clientId}` | from OAuth provider clientId | — (only for Keycloak variant) |
