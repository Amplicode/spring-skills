# JWT Authentication Converter helper method (Kotlin)

## Insert Point
As a private/internal helper method in the configuration class body.
**NOT a @Bean** — called directly from filterChain via `.jwtAuthenticationConverter(jwtAuthenticationConverter())`.

## Code

```kotlin
// defaults: not generated (generateConverter=false)
// Only generated when generateConverter=true AND provider is KEYCLOAK or AWS_COGNITO

// NOT @Bean — helper method called directly from filterChain
fun jwtAuthenticationConverter(): org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter {
    val grantedAuthoritiesConverter: org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter = org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter()
    grantedAuthoritiesConverter.setAuthoritiesClaimName("{claimName}")
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_")

    val jwtAuthenticationConverter: org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter = org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter()
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)

    return jwtAuthenticationConverter
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{claimName}` | derived from provider | `"roles"` for KEYCLOAK, `"cognito:groups"` for AWS_COGNITO |
