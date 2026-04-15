# JWT Resource Server DSL fragment (Kotlin)

## Insert Point
Inside filterChain method body, before `return http.build()`

## Code

```kotlin
// defaults: no customization
// Default output:
http.oauth2ResourceServer { oauth2ResourceServer ->
    oauth2ResourceServer.jwt(org.springframework.security.config.Customizer.withDefaults())
}

// With options:
http.oauth2ResourceServer { oauth2ResourceServer ->
    oauth2ResourceServer.jwt { jwt ->
        jwt
            .jwkSetUri({jwkSetUriField})
            .jwtAuthenticationConverter({converterBeanRef})
            .decoder({decoderBeanRef})
    }
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{jwkSetUriField}` | field name from @Value injection | skip if jwkSetUriType=SPRING |
| `{converterBeanRef}` | bean reference | skip if not set |
| `{decoderBeanRef}` | bean reference | skip if not set |
