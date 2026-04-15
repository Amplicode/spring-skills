# Logout DSL fragment (Kotlin)

## Insert Point
Inside filterChain method body, before `return http.build()`

## Code

```kotlin
// defaults: enabled by Spring Security automatically
// When all options are default — skip this fragment entirely

// When disabled=true:
http.logout { it.disable() }

// With options:
http.logout { logout ->
    logout.logoutUrl("{logoutUrl}")                                               // if logoutUrl is not blank
    logout.logoutRequestMatcher({logoutRequestMatcherBean})                       // if logoutRequestMatcher bean is set (overrides logoutUrl)
    logout.logoutSuccessUrl("{logoutSuccessUrl}")                                  // if logoutSuccessUrl is not blank
    logout.clearAuthentication({clearAuthentication})                              // if explicitly set
    logout.invalidateHttpSession({invalidateHttpSession})                          // if explicitly set
    logout.deleteCookies("{cookie1}", "{cookie2}")                                 // if deleteCookies is not empty
    logout.logoutSuccessHandler({logoutSuccessHandlerBean})                        // if bean is set
    logout.addLogoutHandler({logoutHandlerBean})                                   // for each handler bean
    logout.permitAll()                                                             // if logoutUrl is customized
}

// With STATUS handler (uses HttpStatusReturningLogoutSuccessHandler, NOT a lambda):
http.logout { logout ->
    logout.logoutSuccessHandler(org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler(org.springframework.http.HttpStatus.{logoutSuccessStatus}))
}

// OIDC variant:
http.logout { logout ->
    logout.logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler())
}
```

## Variables
Same as Java variant.
