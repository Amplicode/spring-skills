# Exception Handling (Access Denied) DSL fragment (Kotlin)

## Insert Point
Inside filterChain method body, before `return http.build()`

## Code

```kotlin
// defaults (when Access Denied Handling is enabled but no custom settings):
http.exceptionHandling { }

// When disabled=true:
http.exceptionHandling { it.disable() }

// With accessDeniedPage:
http.exceptionHandling { exceptionHandling ->
    exceptionHandling.accessDeniedPage("{accessDeniedPage}")
}

// With accessDeniedHandler bean:
http.exceptionHandling { exceptionHandling ->
    exceptionHandling.accessDeniedHandler({accessDeniedHandlerBean})
}

// With both accessDeniedHandler and authenticationEntryPoint (bean):
http.exceptionHandling { exceptionHandling ->
    exceptionHandling.accessDeniedHandler({accessDeniedHandlerBean})
    exceptionHandling.authenticationEntryPoint({authenticationEntryPointBean})
}

// With authenticationEntryPoint as HTTP status:
http.exceptionHandling { exceptionHandling ->
    exceptionHandling.authenticationEntryPoint(org.springframework.security.web.authentication.HttpStatusEntryPoint(org.springframework.http.HttpStatus.{ENTRY_POINT_STATUS}))
}
```

## Variables
Same as Java variant.
