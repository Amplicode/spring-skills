# JWT Properties

## application.properties

```properties
# When jwkSetUriType=SPRING:
spring.security.oauth2.resourceserver.jwt.issuer-uri={issuerUri}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri={jwkSetUri}

# When jwkSetUriType=CUSTOM:
{jwkSetCustomUriPropertyKey}={jwkSetCustomUri}

# When jwsAlgorithm != RS256:
spring.security.oauth2.resourceserver.jwt.jws-algorithms={jwsAlgorithm}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{issuerUri}` | user input | skip if empty |
| `{jwkSetUri}` | user input | skip if empty |
| `{jwkSetCustomUriPropertyKey}` | user input | only for CUSTOM type |
| `{jwkSetCustomUri}` | user input | only for CUSTOM type |
| `{jwsAlgorithm}` | user choice | `RS256` (default, skip if RS256) |
