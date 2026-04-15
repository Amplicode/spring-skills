# Headers DSL fragment (Kotlin)

## Insert Point
Inside filterChain method body, before `return http.build()`

## Code

```kotlin
// defaults: enabled, not disabled, all defaults active
// When all options are default — skip this fragment entirely

// When disabled=true:
http.headers { it.disable() }

// When disableDefaults=true (selective headers):
http.headers { headers ->
    headers
        .defaultsDisabled()
        .contentTypeOptions(org.springframework.security.config.Customizer.withDefaults())    // if contentTypeOptions=true
        .xssProtection(org.springframework.security.config.Customizer.withDefaults())         // if xssProtection=true
        .cacheControl(org.springframework.security.config.Customizer.withDefaults())          // if cacheControl=true
        .httpStrictTransportSecurity(org.springframework.security.config.Customizer.withDefaults()) // if hsts=true
        .frameOptions { frameOptions -> frameOptions.{frameOptionMethod}() }                  // if frameOptions != DISABLED
}

// Default mode (disableDefaults=false), with non-default options:
http.headers { headers ->
    headers
        .frameOptions { frameOptions -> frameOptions.{frameOptionMethod}() }                  // if frameOptions is not DENY
        .referrerPolicy { referrerPolicy -> referrerPolicy.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.{POLICY_ENUM}) } // if refererPolicy is not NO_REFERRER
        .permissionsPolicy { permissionsPolicy -> permissionsPolicy.policy("{permissionPolicyValue}") } // if permissionPolicy is not blank
        .addHeaderWriter({headerWriterBean})                                                  // for each headerWriter bean reference (repeated)
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{frameOptionMethod}` | user choice | `deny` |
| `{POLICY_ENUM}` | user choice | `NO_REFERRER` |
| `{permissionPolicyValue}` | user input | empty (skip if empty) |
| `{headerWriterBean}` | user input (bean ref) | skip if no headerWriters; repeated for each |
