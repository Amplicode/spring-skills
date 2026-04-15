# JWT Authentication Converter helper method (Java)

## Insert Point
As a package-private helper method in the configuration class body.
**NOT a @Bean** — called directly from filterChain via `.jwtAuthenticationConverter(jwtAuthenticationConverter())`.

## Code

```java
// defaults: not generated (generateConverter=false)
// Only generated when generateConverter=true AND provider is KEYCLOAK or AWS_COGNITO

org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthenticationConverter() {
    org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthoritiesClaimName("{claimName}");
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthenticationConverter = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

    return jwtAuthenticationConverter;
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{claimName}` | derived from provider | `"roles"` for KEYCLOAK, `"cognito:groups"` for AWS_COGNITO |
