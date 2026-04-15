# Remember Me DSL fragment (Kotlin)

## Insert Point
Inside filterChain method body, before `return http.build()`

## Code

```kotlin
// defaults: not included

// Default:
http.rememberMe { }

// With options:
http.rememberMe { rememberMe ->
    rememberMe.key("{rememberMeKey}")
    rememberMe.tokenValiditySeconds({tokenValiditySeconds})
    rememberMe.rememberMeParameter("{rememberMeParameter}")
    rememberMe.rememberMeCookieName("{rememberMeCookieName}")
    rememberMe.useSecureCookie({useSecureCookie})
    rememberMe.userDetailsService({userDetailsServiceBean})
    rememberMe.tokenRepository({tokenRepositoryBean})
    rememberMe.alwaysRemember({alwaysRemember})
    rememberMe.rememberMeCookieDomain("{cookieDomain}")                           // if cookieDomain is not blank
    rememberMe.authenticationSuccessHandler({authSuccessHandlerBean})             // if bean is set
    rememberMe.rememberMeServices({rememberMeServicesBean})                       // if bean is set (overrides default)
}
```

## Variables
Same as Java variant.
