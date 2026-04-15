# JWT Resource Server DSL fragment (Java)

## Insert Point
Inside filterChain method body, before `return http.build();`

## Code

```java
// defaults: no customization
// Default output:
http.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
    .jwt(org.springframework.security.config.Customizer.withDefaults())
);

// With options:
http.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
    .jwt(jwt -> jwt
        .jwkSetUri({jwkSetUriField})                                         // if jwkSetUriType=CUSTOM (field injected via @Value)
        .jwtAuthenticationConverter({converterBeanRef})                       // if authenticationConverter is set OR generateConverter=true
        .decoder({decoderBeanRef})                                           // if decoder is set
    )
);
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{jwkSetUriField}` | field name from @Value injection | skip if jwkSetUriType=SPRING |
| `{converterBeanRef}` | bean reference | skip if not set |
| `{decoderBeanRef}` | bean reference | skip if not set |
